package com.example.cduprofiledisplay;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mojang.blaze3d.platform.NativeImage;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;

import java.io.*;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.*;

public final class RemoteBadgeTextures {

    public record LoadedTexture(ResourceLocation location, int width, int height) {};

    private enum State { LOADING, READY, FAILED };

    private static final ExecutorService DOWNLOAD_POOL = Executors.newFixedThreadPool(4, r -> {
        Thread t = new Thread(r, "stats-badge-downloader");
        t.setDaemon(true);
        return t;
    });

    private static final Map<String, State> STATE = new ConcurrentHashMap<>();
    private static final Map<String, LoadedTexture> TEXTURES = new ConcurrentHashMap<>();

    private static final Map<String, String> CACHE_INDEX = new ConcurrentHashMap<>();

    private static final Gson GSON = new Gson();

    private static final Path CACHE_DIR = Minecraft.getInstance().gameDirectory.toPath()
        .resolve("config")
        .resolve("cduprofiledisplay")
        .resolve("badge_cache");

    private static final Path INDEX_FILE = CACHE_DIR.resolve("index.json");

    static {loadIndex();};

    private RemoteBadgeTextures() {};

    public static LoadedTexture getOrRequest(String imgUrl) {
        if (imgUrl == null || imgUrl.isBlank()) return null;

        State state = STATE.get(imgUrl);
        if (state == State.READY) return TEXTURES.get(imgUrl);
        if (state != null) return null;

        STATE.put(imgUrl, State.LOADING);
        CompletableFuture
            .supplyAsync(() -> loadCached(imgUrl), DOWNLOAD_POOL)
            .thenAccept(image -> {
                if (image == null) image = download(imgUrl);
                if (image == null) {STATE.put(imgUrl, State.FAILED); return;}

                saveCached(imgUrl, image);
                NativeImage finalImage = image;
                Minecraft.getInstance().execute(() -> {
                        DynamicTexture texture = new DynamicTexture(finalImage);
                        ResourceLocation location = Minecraft.getInstance()
                            .getTextureManager()
                            .register("stats_badge_" + Integer.toHexString(imgUrl.hashCode()), texture);
                        TEXTURES.put(imgUrl, new LoadedTexture(
                            location,
                            finalImage.getWidth(),
                            finalImage.getHeight()
                        ));
                        STATE.put(imgUrl, State.READY);
                    }
                );
            }
        );
        return null;
    };

    private static NativeImage loadCached(String imgUrl) {
        try {
            String file = CACHE_INDEX.get(imgUrl);
            if (file == null) return null;

            Path path = CACHE_DIR.resolve(file);
            if (!Files.exists(path)) return null;

            try (InputStream in = Files.newInputStream(path)) {return NativeImage.read(in);}
        } catch (Exception ignored) {return null;}
    };

    private static void saveCached(String imgUrl, NativeImage image) {
        try {
            Files.createDirectories(CACHE_DIR);
    
            String filename = Integer.toHexString(imgUrl.hashCode()) + ".png";
            Path path = CACHE_DIR.resolve(filename);
    
            image.writeToFile(path.toFile());
    
            CACHE_INDEX.put(imgUrl, filename);
            saveIndex();
        } catch (Exception ignored) {};
    };

    private static void loadIndex() {
        try {
            if (!Files.exists(INDEX_FILE)) return;
            Type type = new TypeToken<Map<String, String>>() {}.getType();

            try (Reader reader = Files.newBufferedReader(INDEX_FILE)) {
                Map<String, String> loaded = GSON.fromJson(reader, type);
                if (loaded != null) CACHE_INDEX.putAll(loaded);
            };
        } catch (Exception ignored) {};
    };

    private static synchronized void saveIndex() {
        try {
            Files.createDirectories(CACHE_DIR);
            try (Writer writer = Files.newBufferedWriter(INDEX_FILE)) {GSON.toJson(CACHE_INDEX, writer);}
        } catch (Exception ignored) {}
    };

    private static NativeImage download(String imgUrl) {
        try {
            HttpURLConnection conn = (HttpURLConnection) new URL(imgUrl).openConnection();

            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(8000);
            try (InputStream in = conn.getInputStream()) {return NativeImage.read(in);}
            finally {conn.disconnect();}
        } catch (IOException e) {return null;}
    };
};