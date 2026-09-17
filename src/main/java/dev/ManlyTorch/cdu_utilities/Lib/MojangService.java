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
import java.util.concurrent.ConcurrentHashMap;

import java.io.File;

public final class MojangService {
    public static final Map<String, String> SKIN_URLS = new ConcurrentHashMap<>();
    private static final File UUID_FILE = new File("cdu_usercache.json");
    private static final Map<String, String> UUID_CACHE = new HashMap<>();
    public static final MutableComponent user = Component.literal("User ").withStyle(ChatFormatting.RED);
    public static final MutableComponent notExist = Component.literal(" does not exist.").withStyle(ChatFormatting.RED);

    private MojangService() { loadCache(); }

    public static String getSkin(String uuid, String username) {
        if (uuid == null) return null;
        String url = SKIN_URLS.get(uuid);
        if (url != null) return url;
        String resolvedUrl = fetchSkinURL(uuid, username);
        if (resolvedUrl == null) return null;
        SKIN_URLS.put(uuid, resolvedUrl);
        ImageCacher.fetchImage("skins", resolvedUrl);
        return resolvedUrl;
    };

    private static String fetchSkinURL(String strUUID, String username) {
        try {
            Minecraft mc = Minecraft.getInstance();
            GameProfile profile = new GameProfile(UUIDFromString(strUUID), username);
            profile = mc.getMinecraftSessionService().fillProfileProperties(profile, false);
            Map<?, ?> textures = mc.getMinecraftSessionService().getTextures(profile, false);
            Object skin = textures.values().stream().findFirst().orElse(null);
            if (skin == null) return null;
            return skin.getClass().getMethod("getUrl").invoke(skin).toString();
        } catch (Exception ignored) {
            return null;
        }
    }

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