package dev.ManlyTorch.cdu_utilities.Lib;

import dev.ManlyTorch.cdu_utilities.UI.Types.ColorConverter;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextColor;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class CDUService {
    private static final long STATS_CACHE_MS = 60_000;
    private static final Map<String, Long> CACHED_AT = new ConcurrentHashMap<>();
    private static final MutableComponent noStats = Component.literal("Couldn't load stats for ").withStyle(ChatFormatting.RED);
    private static final MutableComponent user = Component.literal("User ").withStyle(ChatFormatting.RED);
    private static final MutableComponent notPlayed = Component.literal(" hasn't played CDU.").withStyle(ChatFormatting.RED);
    private static final Map<String, Map<String, Map<String, Long>>> lbCache = new HashMap<>();
    private static final Map<String, List<Map<String, Long>>> lbListCache = new HashMap<>();
    private static final Map<String, Map<String, Object>> STATS_CACHE = new ConcurrentHashMap<>();
    private static final Map<String, Component> prefixes = new ConcurrentHashMap<>();
    private static final List<Map<String, Object>> playtimeRanks = new ArrayList<>();
    private static final List<String> lbSearchStats = List.of("money_pit_total", "total_balance");
    private static final List<String> STATS_LIST = List.of(
        "playtime", "mc_blocks_broken", "mc_mobs_killed",
        "mc_players_killed", "mc_deaths", "mc_jumps",
        "mc_distance", "mc_distance_climbed", "mc_distance_crouched",
        "mc_distance_fallen", "mc_distance_flown", "mc_distance_sprinted",
        "mc_distance_swum", "mc_distance_walked", "total_balance",
        "money_pit_total", "overall_rank"
    );

    private static final String lbStatURL = "craftdownunder.co/auth/public/leaderboards/player?uuid=";
    private static final String lbURL = "craftdownunder.co/api/leaderboards?category=";
    private static final String perksURL = "craftdownunder.co/api/playtime-perks";
    private static final String prefixStrURL = "craftdownunder.co/api/prefixes";
    private static final String statsURL = "api.playcdu.co/users/uuid/?uuid=";

    public static Map<String, Object> preloadPlayerData(String uuid, String inptUserName) {
        Long cacheTime = CACHED_AT.get(uuid);
        if (cacheTime != null && System.currentTimeMillis() - cacheTime < STATS_CACHE_MS) return STATS_CACHE.get(uuid);
        Map<String, JsonObject> httpResponses = new ConcurrentHashMap<>();
        Thread sT = runThread(() -> httpResponses.put("stats", HTTPService.getJson(statsURL + uuid)));
        Thread lbT = runThread(() -> httpResponses.put("lbStats", HTTPService.getJson(lbStatURL + uuid)));
        try { sT.join(); lbT.join(); } catch (Exception e) { return null; }
        JsonObject playerStats = httpResponses.get("stats");
        JsonObject lbStats = httpResponses.get("lbStats");
        Component userComp = Component.literal(inptUserName).withStyle(ChatFormatting.WHITE);
        LocalPlayer plyr = Minecraft.getInstance().player;
        if (playerStats == null) { plyr.displayClientMessage(noStats.copy().append(userComp), true); return null; }
        else if (!playerStats.has("player_stats")) {
            JsonObject details = playerStats.get("details").getAsJsonObject();
            if (!details.isJsonNull() && details.getAsString().equals("'NoneType' object has no attribute 'get'")) {
                plyr.displayClientMessage(user.copy().append(userComp).append(notPlayed), true);
                return null;
            } else System.err.println("CDU API returned unexpected JSON: " + playerStats.toString());
        }
        playerStats = playerStats.get("player_stats").getAsJsonObject();
        List<Map<String, String>> awards = new ArrayList<>();
        Map<String, Object> stats = new ConcurrentHashMap<>();
        List<Thread> threads = new ArrayList<>();
        String username = playerStats.get("username").getAsString();
        long votes = getLong(playerStats, "votes", 0L); long playtime = 0L;
        for (String statName : STATS_LIST) {
            stats.put(statName, Map.of("value", "Unknown"));
            String plyrStatName = getPlayerStat(statName);
            JsonElement elVal = playerStats.get(plyrStatName);
            Map<String, Map<String, Long>> leaderboard = lbCache.get(statName);
            if (leaderboard != null && leaderboard.containsKey(uuid)) {
                Map<String, Long> cache = lbCache.get(statName).get(uuid);
                long value = cache.get("value");
                if (elVal != null && !elVal.isJsonNull()) {
                    value = statName.equals("playtime") ? playtimeToSeconds(elVal.getAsString()) : elVal.getAsLong();
                }
                if (statName.equals("playtime")) playtime = value;
                stats.put(statName, Map.of("rank", cache.get("rank"), "value", value));
                continue;
            }
            if (lbStats != null && statName.equals("overall_rank")) {
                if (lbStats.get("data") == null || lbStats.get("data").isJsonNull()) continue;
                JsonObject data = lbStats.get("data").getAsJsonObject();
                if (data == null || data.get("overallRank").isJsonNull()) continue;
                int page = (int)Math.ceil(data.get("overallRank").getAsDouble()/50d);
                threads.add(runThread(() -> {
                    JsonObject objLb = HTTPService.getJson(lbURL + statName + "&page=" + page);
                    if (objLb == null) return;
                    JsonObject lbSpot = getUser(objLb.getAsJsonArray("items"), uuid);
                    if (lbSpot == null) return;
                    stats.put(statName, Map.of("value", getLong(lbSpot, "value", 0L)));
                }));
                continue;
            }
            if (lbSearchStats.contains(statName)) {
                threads.add(runThread(() -> {
                    JsonObject objLb = HTTPService.getJson(lbURL + statName + "&search=" + username);
                    if (objLb == null) return;
                    JsonObject lbSpot = getUser(objLb.getAsJsonArray("items"), uuid);
                    if (lbSpot == null) return;
                    stats.put(statName, Map.of("value", getLong(lbSpot, "value", 0L)));
                }));
                continue;
            }
            if (elVal == null || elVal.isJsonNull()) continue;
            long value = statName.equals("playtime") ? playtimeToSeconds(elVal.getAsString()) : elVal.getAsLong();
            List<Map<String, Long>> listLB = lbListCache.get(statName);
            if (statName.equals("playtime")) playtime = value;
            if (listLB == null || listLB.size() <= 0 || listLB.get(listLB.size()-1).get("value") < value) {
                threads.add(runThread(() -> {
                    preloadLeaderboard(statName, true);
                    Map<String, Map<String, Long>> leaderboardCache = lbCache.get(statName);
                    if (leaderboardCache != null && leaderboardCache.containsKey(uuid)) {
                        stats.put(statName, Map.of("rank", leaderboardCache.get(uuid).get("rank"), "value", value));
                    }
                }));
            } else stats.put(statName, Map.of("value", value));
        }
        for (JsonElement element : playerStats.getAsJsonArray("player_awards")) {
            if (!element.isJsonObject()) continue;
            JsonObject award = element.getAsJsonObject();
            awards.add(Map.of(
                "award_name", award.get("award_name").getAsString(),
                "award_description", award.get("award_description").getAsString(),
                "img_url", award.get("img_url").getAsString()
            ));
        }
        String prefix = getRankPrefix(lbStats);
        if (prefix != null && prefix != "") {
            stats.put("prefix", prefixes.get(prefix));
        } else { // get playtime rank
            for (int i = playtimeRanks.size() - 1; i >= 0; i--) { Map<String, Object> rankReq = playtimeRanks.get(i);
                if (votes > (long)rankReq.get("votes") && playtime > (long)rankReq.get("playtime")) {
                    stats.put("prefix", (Component)rankReq.get("txt"));
                    break;
                }
            }
        }
        stats.put("username", username);
        stats.put("uniqueserversseenon", Map.of("value",getLong(playerStats, "uniqueserversseenon", 0L)));
        stats.put("votes", Map.of("value",votes));
        stats.put("lastjoinedtime", getString(playerStats, "lastjoinedtime", "Unknown"));
        stats.put("firstjoinedtime", getString(playerStats, "firstjoinedtime", "Unknown"));
        stats.put("isserverbooster", getBoolean(playerStats, "isserverbooster", false));
        stats.put("lastjoinedservername", getString(playerStats, "lastjoinedservername", "Unknown"));
        stats.put("discordid", getLong(playerStats, "discordid", 0L));
        stats.put("player_awards", awards);
        for (Thread t : threads) try { t.join(); } catch (Exception e) { return null; }
        STATS_CACHE.put(uuid, stats);
        return stats;
    }

    public static void preloadLeaderboard(String statCategory, boolean force) {
        if (lbCache.containsKey(statCategory) && !force) return;
        Map<String, Map<String, Long>> leaderboard = new ConcurrentHashMap<>();
        Map<String, JsonObject> top = new ConcurrentHashMap<>();
        List<Map<String, Long>> listLeaderboard = new ArrayList<>();
        Thread t50Thread = runThread(() -> {
            JsonObject top50 = HTTPService.getJson(lbURL + statCategory + "&page=1");
            if (top50 == null || top50.isJsonNull()) { sleep(3000); top50 = HTTPService.getJson(lbURL + statCategory + "&page=1"); }
            if (top50 != null) top.put("50", top50);
        });
        Thread t100Thread = runThread(() -> {
            JsonObject top100 = HTTPService.getJson(lbURL + statCategory + "&page=2");
            if (top100 == null || top100.isJsonNull()) { sleep(3000); top100 = HTTPService.getJson(lbURL + statCategory + "&page=2"); }
            if (top100 != null) top.put("100", top100);
        });
        try { t50Thread.join(); t100Thread.join(); } catch (Exception e) { return; }
        if (!top.containsKey("50")) return;
        for (JsonArray jsonLb : List.of(top.get("50").getAsJsonArray("items"), top.get("100").getAsJsonArray("items"))) {
            for (JsonElement element : jsonLb) { JsonObject obj = element.getAsJsonObject();
                Map<String, Long> mapElement = Map.of(
                    "rank", obj.get("rank").getAsLong(),
                    "value", obj.get("value").getAsLong()
                );
                listLeaderboard.add(mapElement);
                leaderboard.put(obj.get("uuid").getAsString(), mapElement);
            }
        }
        lbListCache.put(statCategory, listLeaderboard);
        lbCache.put(statCategory, leaderboard);
        return;
    }

    public static void preloadPrefixes() {
        Map<String, Object> httpResp = new ConcurrentHashMap<>();
        Thread perkT = runThread(() -> { JsonArray perks = HTTPService.getJsonArray(perksURL);
            if (perks == null || perks.isJsonNull()) perks = HTTPService.getJsonArray(perksURL);
            if (perks != null) httpResp.put("perks", perks);
        });
        Thread prefixStrT = runThread(() -> { JsonObject prefixStrs = HTTPService.getJson(prefixStrURL);
            if (prefixStrs == null || prefixStrs.isJsonNull()) { sleep(3000); prefixStrs = HTTPService.getJson(prefixStrURL); }
            if (prefixStrs != null) httpResp.put("prefixes", prefixStrs);
        });
        try { perkT.join(); prefixStrT.join(); } catch (Exception e) { return; }
        if (!httpResp.containsKey("prefixes") || !httpResp.containsKey("perks")) { return; }
        JsonObject prefixesObj = (JsonObject)httpResp.get("prefixes");
        for (JsonElement element : prefixesObj.getAsJsonArray("prefixes")) {
            JsonObject prefixData = element.getAsJsonObject();
            JsonElement prefixEl = prefixData.get("prefix_json");
            Component prefixComp;
            if (!prefixEl.isJsonArray()) prefixComp = constructPrefix(prefixEl.getAsJsonObject().getAsJsonArray("extra"));
            else prefixComp = constructPrefix(prefixEl.getAsJsonArray());
            prefixes.put(prefixData.get("name").getAsString(), prefixComp);
        }
        JsonArray perks = (JsonArray)httpResp.get("perks");
        long ptReq = 0;
        for (int i = perks.size() - 1; i >= 0; i--) { JsonObject obj = perks.get(i).getAsJsonObject();
            JsonObject req = obj.get("requirements").getAsJsonObject();
            long hours = 0L; String playtime = getString(req, "playtime", "0 hrs");
            long votes = getLong(req, "votes", 0L);
            if (playtime.equals("*")) hours = ptReq;
            else hours = Long.parseLong(playtime.replace(",", "").replace(" hrs", "").trim());
            String prefixName = getPrefixName(getString(obj, "rank", ""));
            Map<String, Object> rankReq = new HashMap<>();
            rankReq.put("name", prefixName);
            rankReq.put("txt", prefixes.get(prefixName));
            rankReq.put("votes", votes);
            rankReq.put("playtime", hours*3600L);
            playtimeRanks.add(rankReq);
        }
    }

    public static String getRankPrefix(JsonObject obj) {
        if (obj == null) return null;
        JsonElement dataEl = obj.get("data");
        if (dataEl == null || dataEl.isJsonNull()) return null;
        JsonElement bannerEl = dataEl.getAsJsonObject().get("banner");
        if (bannerEl == null || bannerEl.isJsonNull()) return null;
        return getString(bannerEl.getAsJsonObject(), "key", "");
    }
    public static String getPrefixName(String str) {
        int plus = str.length() - str.replaceAll("\\+*$", "").length();
        return str.toLowerCase().replaceAll("\\+*$", "") + (plus <= 0 ? "" : plus);
    }
    public static Component constructPrefix(JsonArray arry) {
        MutableComponent str = Component.literal("");
        for (JsonElement el : arry) {
            if (el.isJsonPrimitive()) { str.append(Component.literal(el.getAsString())); continue; }
            JsonObject obj = el.getAsJsonObject();
            String text = getString(obj, "text", "");
            int color = ColorConverter.fromString(getString(obj, "color", "ffffff"));
            boolean bold = getBoolean(obj, "bold", false);
            boolean italic = getBoolean(obj, "italic", false);
            boolean obfuscated = getBoolean(obj, "obfuscated", false);
            str.append(Component.literal(text).withStyle(style ->
                style.withColor(TextColor.fromRgb(color)).withBold(bold).withItalic(italic)
                .withObfuscated(obfuscated)
            ));
        }
        return str;
    }

    public static long getLong(JsonObject obj, String idx, long defaultVal) { JsonElement el = obj.get(idx);
        return (el == null || el.isJsonNull()) ? defaultVal : el.getAsLong();
    }
    public static String getString(JsonObject obj, String idx, String defaultVal) { JsonElement el = obj.get(idx);
        return (el == null || el.isJsonNull()) ? defaultVal : el.getAsString();
    }
    public static boolean getBoolean(JsonObject obj, String idx, boolean defaultVal) { JsonElement el = obj.get(idx);
        return (el == null || el.isJsonNull()) ? defaultVal : el.getAsBoolean();
    }

    public static Map<String, Map<String, Long>> getLeaderboard(String statCategory) {
        return lbCache.get(statCategory);
    }

    public static JsonObject getUser(JsonArray leaderboard, String uuid) {
        if (leaderboard == null) return null;
        for (JsonElement position : leaderboard) { JsonObject lbPos = position.getAsJsonObject();
            if (!getString(lbPos, "uuid", "").equals(uuid)) continue;
            return lbPos;
        }
        return null;
    }

    public static Thread createThread(Runnable callback) {
        Thread t = new Thread(callback);
        t.setUncaughtExceptionHandler((thread, ex) -> { System.err.println(ex.getMessage());; });
        return t;
    };
    public static Thread runThread(Runnable callback) { Thread t = createThread(callback); t.start(); return t; }
    public static void sleep(int ms) { try { Thread.sleep(ms); } catch (InterruptedException e) { return; } }
    
    public static String getPlayerStat(String stat) {
        if (stat.chars().filter(c -> c == '_').count() >= 2) {
            int idx = stat.lastIndexOf('_');
            return stat.substring(0, idx) + stat.substring(idx+1);
        }
        return stat;
    }
    public static long playtimeToSeconds(String playtime) {
        long total = 0;
        for (String part : playtime.split(",")) {
            part = part.trim();
            if (part.isEmpty()) continue;
            long value = Long.parseLong(part.substring(0, part.length() - 1));
            switch (part.charAt(part.length() - 1)) {
                case 'd': total += value * 86400; break;
                case 'h': total += value * 3600; break;
                case 'm': total += value * 60; break;
                case 's': total += value; break;
            }
        }
        return total;
    }
}
