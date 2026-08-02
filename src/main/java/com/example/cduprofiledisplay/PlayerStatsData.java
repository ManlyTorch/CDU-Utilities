package com.example.cduprofiledisplay;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Plain data holder for everything the stats screen needs to render.
 * Build it from the "player_stats" object returned by
 * https://api.playcdu.co/users/uuid/?uuid=<uuid-no-dashes> via {@link #fromJson}.
 */
public record PlayerStatsData(
        UUID uuid,
        String username,
        String playtime,              // e.g. "96d, 4h, 43m, 43s"
        String lastJoinedServerName,  // e.g. "Society: Sunlit Valley"
        boolean discordLinked,
        long discordId,
        int uniqueServers,

        long blocksBroken,
        int deaths,
        long jumps,
        int mobsKilled,
        int playersKilled,
        int votes,

        long distanceTotal,
        long distanceClimbed,
        long distanceCrouched,
        long distanceFallen,
        long distanceFlown,
        long distanceSprinted,
        long distanceSwum,
        long distanceWalked,

        List<Achievement> achievements
) {
    /** One award/achievement badge. imgUrl points at playcdu's CDN; may be null. */
    public record Achievement(String title, String description, String imgUrl) {
    }

    /**
     * Parses the "player_stats" object from the playcdu API response.
     * uuid/usernameFallback come from whatever resolved them (tab list or Mojang API),
     * used only if the API response happens to omit those fields.
     */
    public static PlayerStatsData fromJson(UUID uuid, String usernameFallback, JsonObject o) {
        String username = getString(o, "username", usernameFallback);
        String playtime = getString(o, "playtime", "Unknown");
        String lastServer = getString(o, "lastjoinedservername", "Unknown");
        String discordId = getString(o, "discordid", null);

        List<Achievement> achievements = new ArrayList<>();
        Set<String> seenTitles = new HashSet<>();
        if (o.has("player_awards") && o.get("player_awards").isJsonArray()) {
            JsonArray awards = o.getAsJsonArray("player_awards");
            for (JsonElement el : awards) {
                if (!el.isJsonObject()) continue;
                JsonObject award = el.getAsJsonObject();
                String name = getString(award, "award_name", "Unknown Award");
                // the API sometimes lists the same badge more than once; only show it once
                if (!seenTitles.add(name)) continue;
                String desc = getString(award, "award_description", "");
                String imgUrl = getString(award, "img_url", null);
                achievements.add(new Achievement(name, desc, imgUrl));
            }
        }

        return new PlayerStatsData(
                uuid,
                username,
                playtime,
                lastServer,
                discordId != null && !discordId.isBlank(),
                getLong(o, "discordid", 0),
                getInt(o, "uniqueserversseenon", 0),

                getLong(o, "mc_blocksbroken", 0),
                getInt(o, "mc_deaths", 0),
                getLong(o, "mc_jumps", 0),
                getInt(o, "mc_mobskilled", 0),
                getInt(o, "mc_playerskilled", 0),
                getInt(o, "votes", 0),

                getLong(o, "mc_distance", 0),
                getLong(o, "mc_distanceclimbed", 0),
                getLong(o, "mc_distancecrouched", 0),
                getLong(o, "mc_distancefallen", 0),
                getLong(o, "mc_distanceflown", 0),
                getLong(o, "mc_distancesprinted", 0),
                getLong(o, "mc_distanceswum", 0),
                getLong(o, "mc_distancewalked", 0),

                achievements
        );
    }

    private static String getString(JsonObject o, String key, String fallback) {
        return o.has(key) && !o.get(key).isJsonNull() ? o.get(key).getAsString() : fallback;
    }

    private static long getLong(JsonObject o, String key, long fallback) {
        return o.has(key) && !o.get(key).isJsonNull() ? o.get(key).getAsLong() : fallback;
    }

    private static int getInt(JsonObject o, String key, int fallback) {
        return o.has(key) && !o.get(key).isJsonNull() ? o.get(key).getAsInt() : fallback;
    }
}
