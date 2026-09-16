package dev.ManlyTorch.cdu_utilities;

import com.mojang.authlib.GameProfile;

import dev.ManlyTorch.cdu_utilities.Lib.ImageCacher;
import net.minecraft.client.Minecraft;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class SkinManager {
    public static final Map<String, String> SKIN_URLS = new ConcurrentHashMap<>();

    private SkinManager() {}

    public static String getSkin(String uuid, String username) {
        if (uuid == null) return null;
        String url = SKIN_URLS.get(uuid);
        if (url != null) return url;
        String resolvedUrl = resolveSkinUrl(uuid, username);
        if (resolvedUrl == null) return null;
        SKIN_URLS.put(uuid, resolvedUrl);
        ImageCacher.fetchImage("skins", resolvedUrl).location();
        return resolvedUrl;
    }

    private static String resolveSkinUrl(String strUUID, String username) {
        try {
            UUID uuid = UUID.fromString(strUUID.replaceFirst(
                "(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})",
                "$1-$2-$3-$4-$5"
            ));
            Minecraft mc = Minecraft.getInstance();
            GameProfile profile = new GameProfile(uuid, username);
            profile = mc.getMinecraftSessionService().fillProfileProperties(profile, false);
            Map<?, ?> textures = mc.getMinecraftSessionService().getTextures(profile, false);
            Object skin = textures.values().stream().findFirst().orElse(null);
            if (skin == null) return null;
            return skin.getClass().getMethod("getUrl").invoke(skin).toString();
        } catch (Exception ignored) {
            return null;
        }
    }
}