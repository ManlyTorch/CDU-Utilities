package dev.ManlyTorch.cdu_utilities.Lib;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.MutableComponent;

import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.Base64;
import java.util.concurrent.ConcurrentHashMap;

import java.io.File;

public final class MojangService {
    public static final Map<String, String> SKIN_URLS = new ConcurrentHashMap<>();
    public static final Map<String, Boolean> SKIN_SLIM = new ConcurrentHashMap<>();
    private static final File SLIM_FILE = new File("config/CDU-Utilities/slimIndex.json");
    private static final File UUID_FILE = new File("config/CDU-Utilities/cdu_usercache.json");
    private static final Map<String, String> UUID_CACHE = new HashMap<>();
    public static final MutableComponent user = Component.literal("User ").withStyle(ChatFormatting.RED);
    public static final MutableComponent notExist = Component.literal(" does not exist.").withStyle(ChatFormatting.RED);

    private MojangService() { loadCache(); loadSlimCache(); }

    public static String getSkin(String uuid) {
        if (uuid == null) return null;
        uuid = uuid.replace("-", "");
        String url = SKIN_URLS.get(uuid);
        if (url != null) return url;
        fetchSkin(uuid);
        return SKIN_URLS.get(uuid);
    };

    public static boolean isSlim(String uuid) {
        if (uuid == null) return false;
        uuid = uuid.replace("-", "");
        Boolean cached = SKIN_SLIM.get(uuid);
        if (cached != null) return cached;
        fetchSkin(uuid);
        saveSlimCache();
        return SKIN_SLIM.get(uuid);
    };

    private static void fetchSkin(String uuid) {
        try {
            JsonObject profile = HTTPService.getJson("sessionserver.mojang.com/session/minecraft/profile/" + uuid);
            if (!profile.has("properties")) return;
            for (JsonElement element : profile.getAsJsonArray("properties")) {
                JsonObject property = element.getAsJsonObject();
                JsonElement name = property.get("name");
                if (name == null || name.getAsString().equals("textures") == false) continue;
                if (!property.has("value")) continue;
                String decoded = new String(Base64.getDecoder().decode(property.get("value").getAsString()));
                JsonObject textures = JsonParser.parseString(decoded).getAsJsonObject();
                if (!textures.has("textures")) return;
                JsonObject textureObject = textures.getAsJsonObject("textures");
                if (!textureObject.has("SKIN")) return;
                JsonObject skin = textureObject.getAsJsonObject("SKIN");
                String resolvedUrl = skin.get("url").getAsString();
                SKIN_URLS.put(uuid, resolvedUrl);
                ImageCacher.fetchImage("skins", resolvedUrl);
                if (!skin.has("metadata")) { SKIN_SLIM.put(uuid, false); return; }
                JsonObject metadata = skin.getAsJsonObject("metadata");
                if (!metadata.has("model")) return;
                SKIN_SLIM.put(uuid, metadata.get("model").getAsString().equalsIgnoreCase("slim"));
            }
        } catch (Exception ignored) {}
    };
    
    public static UUID UUIDFromString(String strUUID) {
        return UUID.fromString(strUUID.replaceFirst( "(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})", "$1-$2-$3-$4-$5"));
    }

    public static String getUUIDfromName(String username) {
        String cached = UUID_CACHE.get(username);
        if (cached != null) return cached;
        Minecraft mc = Minecraft.getInstance();
        if (mc.getConnection() != null) {
            for (PlayerInfo info : mc.getConnection().getOnlinePlayers()) {
                GameProfile prof = info.getProfile();
                String name = prof.getName();
                if (!name.equalsIgnoreCase(username)) continue;
                String uuid = prof.getId().toString().replace("-", "");
                UUID_CACHE.put(name.toLowerCase(), uuid);
                return uuid;
            };
        };
        String uuid = fetchUUID(username);
        if (uuid == null) {
            Component userComp = Component.literal(username).withStyle(ChatFormatting.WHITE);
            mc.player.displayClientMessage(user.copy().append(userComp).append(notExist), true);
        };
        return uuid;
    };

    public static String fetchUUID(String username) {
        try {
            String uuid = HTTPService.getJson("api.mojang.com/users/profiles/minecraft/" + username).get("id").getAsString();
            UUID_CACHE.put(username, uuid); saveCache(); return uuid;
        } catch (Exception e) {
            throw new RuntimeException("Couldn't resolve a UUID for '" + username + "': " + e.getMessage(), e);
        }
    };

    private static void loadCache() {
        if (!UUID_FILE.exists()) return;
        try (FileReader reader = new FileReader(UUID_FILE)) {
            JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
            for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
                UUID_CACHE.put(entry.getKey(), entry.getValue().getAsString());
            };
        } catch (Exception ignored) {};
    };

    private static void loadSlimCache() {
        if (!SLIM_FILE.exists()) return;
        try (FileReader reader = new FileReader(SLIM_FILE)) {
            JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
            for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
                SKIN_SLIM.put(entry.getKey(), entry.getValue().getAsBoolean());
            };
        } catch (Exception ignored) {};
    };

    private static void saveSlimCache() {
        try {
            File parent = SLIM_FILE.getParentFile();
            if (parent != null && !parent.exists()) parent.mkdirs();
            try (FileWriter writer = new FileWriter(SLIM_FILE)) {
                JsonObject json = new JsonObject();
                for (Map.Entry<String, Boolean> entry : SKIN_SLIM.entrySet()) {
                    json.addProperty(entry.getKey(), entry.getValue());
                };
                writer.write(json.toString());
            }
        } catch (Exception ignored) {};
    };

    private static void saveCache() {
        try (FileWriter writer = new FileWriter(UUID_FILE)) {
            JsonObject json = new JsonObject();
            for (Map.Entry<String, String> entry : UUID_CACHE.entrySet()) {
                json.addProperty(entry.getKey(), entry.getValue().toString());
            };
            writer.write(json.toString());
        } catch (Exception ignored) {};
    };
}