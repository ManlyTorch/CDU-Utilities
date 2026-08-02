package com.example.cduprofiledisplay;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import net.minecraft.network.chat.Component;
import java.util.concurrent.ConcurrentHashMap;

public final class PlayCduApi {

    private static final long CACHE_TIME_MS = 300_000;
    private static final ConcurrentHashMap<UUID, CacheEntry> CACHE = new ConcurrentHashMap<>();

    private PlayCduApi() {};
    private record CacheEntry(JsonObject data, long timestamp) {};

    public static JsonObject fetchPlayerStats(Minecraft mc, UUID uuid, String username) throws IOException {
        CacheEntry cached = CACHE.get(uuid);
        if (cached != null && System.currentTimeMillis() - cached.timestamp() < CACHE_TIME_MS) return cached.data().deepCopy();
        JsonObject stats = requestStats(mc, uuid, username);

        CACHE.put(uuid, new CacheEntry(stats, System.currentTimeMillis()));
        return stats.deepCopy();
    };

    private static JsonObject requestStats(Minecraft mc, UUID uuid, String username) throws IOException {
        String noDash = uuid.toString().replace("-", "");

        URL url = new URL("https://api.playcdu.co/users/uuid/?uuid=" + noDash);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "application/json");
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(8000);

        int code = conn.getResponseCode();

        InputStream stream = (code >= 200 && code < 300) ? conn.getInputStream() : conn.getErrorStream();
        try (InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
            JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
            if (code != 200) throw new IOException("playcdu API returned HTTP " + code + ": " + root);

            if (!root.has("player_stats")) {
                if (root.has("details")
                    && root.get("details").getAsString().equals("'NoneType' object has no attribute 'get'")) {
                    Component message = Component.literal("User ")
                        .withStyle(ChatFormatting.RED)
                    .append(Component.literal(username)
                        .withStyle(ChatFormatting.WHITE)
                    .append(Component.literal(" hasn't played CDU.")
                        .withStyle(ChatFormatting.RED)));
                    mc.player.displayClientMessage(message, true);
                } else throw new IOException("playcdu API returned unexpected JSON: " + root);
            };

            return root.getAsJsonObject("player_stats");
        } finally {conn.disconnect();}
    };
};