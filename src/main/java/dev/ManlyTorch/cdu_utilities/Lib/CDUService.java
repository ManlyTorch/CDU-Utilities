package dev.ManlyTorch.cdu_utilities.Lib;

import com.google.gson.JsonObject;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public class CDUService {
    private static final long STATS_CACHE_MS = 300_000;
    private static final ConcurrentHashMap<String, CacheEntry> STATS_CACHE = new ConcurrentHashMap<>();
    private record CacheEntry(JsonObject data, long timestamp) {};
    private static final MutableComponent noStats = Component.literal("Couldn't load stats for ").withStyle(ChatFormatting.RED);
    private static final MutableComponent user = Component.literal("User ").withStyle(ChatFormatting.RED);
    private static final MutableComponent notPlayed = Component.literal(" hasn't played CDU.").withStyle(ChatFormatting.RED);

    public static JsonObject getPlayerData(String uuid, String username) {
        CacheEntry cached = STATS_CACHE.get(uuid);
        if (cached != null && System.currentTimeMillis() - cached.timestamp() < STATS_CACHE_MS) return cached.data().deepCopy();
        try {
            JsonObject stats = HTTPService.getJson("api.playcdu.co/users/uuid/?uuid=" + uuid);
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
        } catch (IOException e) { e.printStackTrace(); }
        return null;
    };
}
