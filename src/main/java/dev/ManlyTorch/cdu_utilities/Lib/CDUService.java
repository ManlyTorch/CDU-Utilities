package dev.ManlyTorch.cdu_utilities.Lib;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.MutableComponent;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class CDUService {
    private static final long STATS_CACHE_MS = 300_000;
    private static final ConcurrentHashMap<String, CacheEntry> STATS_CACHE = new ConcurrentHashMap<>();
    private static final MutableComponent noStats = Component.literal("Couldn't load stats for ").withStyle(ChatFormatting.RED);
    private static final MutableComponent user = Component.literal("User ").withStyle(ChatFormatting.RED);
    private static final MutableComponent notPlayed = Component.literal(" hasn't played CDU.").withStyle(ChatFormatting.RED);
    private static final Map<String, Map<String, Integer>> leaderboards = new HashMap<>();

    private record CacheEntry(JsonObject data, long timestamp) {};

    public static JsonObject getPlayerData(String uuid, String username) {
        CacheEntry cached = STATS_CACHE.get(uuid);
        if (cached != null && System.currentTimeMillis() - cached.timestamp() < STATS_CACHE_MS) return cached.data().deepCopy();
        JsonObject stats = HTTPService.getJson("api.playcdu.co/users/uuid/?uuid=" + uuid);
        if (stats == null) return null;
        Component userComp = Component.literal(username).withStyle(ChatFormatting.WHITE);
        LocalPlayer plyr = Minecraft.getInstance().player;
        if (stats == null) { plyr.displayClientMessage(noStats.copy().append(userComp), true); return null; }
        else if (!stats.has("player_stats")) {
            if (stats.has("details") && stats.get("details").getAsString().equals("'NoneType' object has no attribute 'get'")) {
                plyr.displayClientMessage(user.copy().append(userComp).append(notPlayed), true);
                return null;
            } else System.err.println("CDU API returned unexpected JSON: " + stats.toString());
        }
        stats = stats.get("player_stats").getAsJsonObject();
        STATS_CACHE.put(uuid, new CacheEntry(stats, System.currentTimeMillis()));
        return stats.deepCopy();
    };

    public static Integer getLBSpot(String uuid, String statCategory) {
        Map<String, Integer> leaderboard = getLeaderboard(statCategory);
        return leaderboard.get(uuid);
    };

    public static Map<String, Integer> getLeaderboard(String statCategory) {
        if (leaderboards.containsKey(statCategory)) return leaderboards.get(statCategory);
        Map<String, Integer> builtLB = new HashMap<>();
        String categoryURL = "craftdownunder.co/api/leaderboards?category=" + statCategory;
        JsonObject top50 = HTTPService.getJson(categoryURL + "&page=1");
        JsonObject top100 = HTTPService.getJson(categoryURL + "&page=2");
        for (JsonArray leaderboard : List.of(top50.getAsJsonArray("items"), top100.getAsJsonArray("items"))) {
            for (JsonElement element : leaderboard) {
                JsonObject lbSpot = element.getAsJsonObject();
                builtLB.put(lbSpot.get("uuid").getAsString(), lbSpot.get("rank").getAsInt());
            }
        }
        leaderboards.put(statCategory, builtLB);
        return builtLB;
    }
}
