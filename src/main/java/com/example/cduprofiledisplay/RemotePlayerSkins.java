package com.example.cduprofiledisplay;

import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.platform.NativeImage;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class RemotePlayerSkins {
    private enum State {LOADING, READY, FAILED};

    private static final ExecutorService DOWNLOAD_POOL = Executors.newFixedThreadPool(3, r -> {
        Thread t = new Thread(r, "cdu-skin-downloader");
        t.setDaemon(true);
        return t;
    });

    private static final Map<UUID, State> STATE = new ConcurrentHashMap<>();
    private static final Map<UUID, ResourceLocation> TEXTURES = new ConcurrentHashMap<>();

    private static final Path SKIN_DIR = Minecraft.getInstance().gameDirectory.toPath()
        .resolve("config")
        .resolve("cduprofiledisplay")
        .resolve("skins");

    private RemotePlayerSkins() {};

    public static ResourceLocation getOrRequest(UUID uuid, String username) {
        if (uuid == null) return null;

        ResourceLocation existing = TEXTURES.get(uuid);
        if (existing != null) return existing;

        State state = STATE.get(uuid);
        if (state == State.LOADING) return null;
        if (state != null) return null;
        STATE.put(uuid, State.LOADING);
        CompletableFuture
            .supplyAsync(() -> loadSkin(uuid, username), DOWNLOAD_POOL)
            .thenAccept(image -> {
                if (image == null) {STATE.put(uuid, State.FAILED); return;};
                saveSkin(uuid, image);
                Minecraft.getInstance().execute(() -> {
                    DynamicTexture texture = new DynamicTexture(image);
                    ResourceLocation location = Minecraft.getInstance()
                        .getTextureManager()
                        .register("cdu_skin_" + uuid, texture);
                    TEXTURES.put(uuid, location);
                    STATE.put(uuid, State.READY);
                });
            });
        return null;
    };


    private static NativeImage loadSkin(UUID uuid, String username) {
        NativeImage cached = loadCached(uuid);
        if (cached != null) return cached;

        try {
            Minecraft mc = Minecraft.getInstance();
            GameProfile profile = new GameProfile(uuid, username);
            profile = mc.getMinecraftSessionService().fillProfileProperties(profile, false);
            Map<?, ?> textures = mc.getMinecraftSessionService().getTextures(profile, false);
            Object skin = textures.values()
                .stream()
                .findFirst()
                .orElse(null);
            if (skin == null) return null;
            String url = skin.getClass()
                .getMethod("getUrl")
                .invoke(skin)
                .toString();
            return download(url);
        } catch (Exception ignored) {return null;}
    };

    private static NativeImage loadCached(UUID uuid) {
        try {
            Path file = SKIN_DIR.resolve(uuid + ".png");
            if (!Files.exists(file)) return null;
            try (InputStream in = Files.newInputStream(file)) {return NativeImage.read(in);}
        } catch (Exception ignored) {return null;}
    };

    private static void saveSkin(UUID uuid, NativeImage image) {
        try {
            Files.createDirectories(SKIN_DIR);
            image.writeToFile(SKIN_DIR.resolve(uuid + ".png").toFile());
        } catch (Exception ignored) {};
    };

    private static NativeImage download(String url) {
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(8000);
            try (InputStream in = connection.getInputStream()) {
                return NativeImage.read(in);
            } finally {connection.disconnect();}
        } catch (Exception ignored) {return null;}
    };
}