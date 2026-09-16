package dev.ManlyTorch.cdu_utilities.Lib;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mojang.blaze3d.platform.NativeImage;
import org.jetbrains.annotations.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.*;

public final class ImageCacher {
    public record LoadedTexture(ResourceLocation location, int width, int height) {};
    private enum State { LOADING, READY, FAILED };
    private static final ExecutorService DOWNLOAD_POOL = Executors.newFixedThreadPool(4, r -> {
        Thread t = new Thread(r, "stats-badge-downloader");
        t.setDaemon(true);
        return t;
    });
    private static final Gson GSON = new Gson();
    private static final Map<String, State> STATE = new ConcurrentHashMap<>();
    private static final Map<String, LoadedTexture> TEXTURES = new ConcurrentHashMap<>();
    private static final Map<String, Map<String, String>> CACHE_INDEX = new ConcurrentHashMap<>();
    private static final Path CACHE_DIR = Minecraft.getInstance().gameDirectory.toPath()
        .resolve("config")
        .resolve("CDU-Utilities");
    private static final Path INDEX_FILE = CACHE_DIR.resolve("index.json");
    static {loadIndex();};
    private ImageCacher() {};

    public static LoadedTexture fetchImage(@Nullable String tCache, String imgUrl) {
        if (imgUrl == null || imgUrl.isBlank()) return null;
        State state = STATE.get(imgUrl);
        if (state == State.READY) return TEXTURES.get(imgUrl);
        if (state != null) return null;
        String targetCache = tCache != null ? tCache : "NULL";

        STATE.put(imgUrl, State.LOADING);
        NativeImage image = loadCached(targetCache, imgUrl);
        if (image == null) image = HTTPService.getImage(imgUrl);
        if (image == null) {STATE.put(imgUrl, State.FAILED); return null;}
        saveCached(targetCache, imgUrl, image);
        NativeImage finalImage = image;
        DynamicTexture texture = new DynamicTexture(finalImage);
        ResourceLocation location = Minecraft.getInstance().getTextureManager()
            .register(targetCache + Integer.toHexString(imgUrl.hashCode()), texture);
        TEXTURES.put(imgUrl, new LoadedTexture(location, finalImage.getWidth(), finalImage.getHeight()));
        STATE.put(imgUrl, State.READY);
        return TEXTURES.get(imgUrl);
    };

    private static NativeImage loadCached(String targetCache, String imgUrl) {
        try {
            Map<String, String> TARGET_CACHE = CACHE_INDEX.get(targetCache);
            if (TARGET_CACHE == null) return null;
            String file = TARGET_CACHE.get(imgUrl);
            if (file == null) return null;
            Path TCACHE_DIR = CACHE_DIR.resolve(targetCache);
            Path path = TCACHE_DIR.resolve(file);
            if (!Files.exists(path)) return null;
            try (InputStream in = Files.newInputStream(path)) {return NativeImage.read(in);}
        } catch (Exception ignored) {return null;}
    };

    private static void saveCached(@Nullable String targetCache, String imgUrl, NativeImage image) {
        try {
            Path TCACHE_DIR = CACHE_DIR.resolve(targetCache);
            Files.createDirectories(TCACHE_DIR);
            String filename = Integer.toHexString(imgUrl.hashCode()) + ".png";
            Path path = TCACHE_DIR.resolve(filename);
            image.writeToFile(path.toFile());
            Map<String, String> TARGET_CACHE = CACHE_INDEX.computeIfAbsent(targetCache, k -> new ConcurrentHashMap<>());
            TARGET_CACHE.put(imgUrl, filename);
            saveIndex();
        } catch (Exception ignored) {};
    };

    private static void loadIndex() {
        try {
            if (!Files.exists(INDEX_FILE)) return;
            Type type = new TypeToken<Map<String, Map<String, String>>>() {}.getType();
            try (Reader reader = Files.newBufferedReader(INDEX_FILE)) {
                Map<String, Map<String, String>> loaded = GSON.fromJson(reader, type);
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
};