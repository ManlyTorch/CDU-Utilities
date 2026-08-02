package com.example.cduprofiledisplay;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.authlib.GameProfile;
import net.minecraft.client.Minecraft;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.UUID;
import java.util.Map;
import java.util.HashMap;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

public final class MojangApi {
    private MojangApi() {
        loadCache();
    };
    private static final Map<String, UUID> UUID_CACHE = new HashMap<>();
    private static final File CACHE_FILE = new File("cdu_usercache.json");

    private static void loadCache() {
        if (!CACHE_FILE.exists()) return;
        try (FileReader reader = new FileReader(CACHE_FILE)) {
            JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
            for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
                UUID_CACHE.put(
                    entry.getKey(),
                    UUID.fromString(entry.getValue().getAsString())
                );
            };
        } catch (Exception ignored) {};
    };

    private static void saveCache() {
        try (FileWriter writer = new FileWriter(CACHE_FILE)) {
            JsonObject json = new JsonObject();
            for (Map.Entry<String, UUID> entry : UUID_CACHE.entrySet()) {
                json.addProperty(
                        entry.getKey(),
                        entry.getValue().toString()
                );
            };
            writer.write(json.toString());
        } catch (Exception ignored) {};
    };

    public static UUID resolveUuid(String username) throws IOException {
        Minecraft mc = Minecraft.getInstance();
    
        String key = username.toLowerCase(Locale.ROOT);
    
        UUID cached = UUID_CACHE.get(key);
        if (cached != null) return cached;
        if (mc.getConnection() != null) {
            for (var player : mc.getConnection().getOnlinePlayers()) {
                GameProfile profile = player.getProfile();
                if (profile.getName().equalsIgnoreCase(username)) {
                    UUID uuid = profile.getId();
                    UUID_CACHE.put(key, uuid);
                    return uuid;
                };
            };
        };
    
        URL url = new URL("https://api.mojang.com/users/profiles/minecraft/" + username);
    
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(5000);
    
        int code = conn.getResponseCode();

        if (code != 200) {
            conn.disconnect();
            throw new IOException("Could not resolve UUID for username '" + username + "'");
        };
    
        try (InputStreamReader reader = new InputStreamReader(
            conn.getInputStream(),
            StandardCharsets.UTF_8
        )) {
            JsonObject json = JsonParser.parseReader(reader)
                .getAsJsonObject();
    
            String id = json.get("id").getAsString();
    
            UUID uuid = UUID.fromString(id.replaceFirst(
                "(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})",
                "$1-$2-$3-$4-$5"
            ));
            UUID_CACHE.put(key, uuid);

            saveCache();
            return uuid;
        } finally {
            conn.disconnect();
        }
    };
};