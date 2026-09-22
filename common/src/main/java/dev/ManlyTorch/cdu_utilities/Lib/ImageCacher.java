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
        else if (state != null) return null;
        String targetCache = tCache != null ? tCache : "NULL";
        STATE.put(imgUrl, State.LOADING);
        NativeImage image = loadCached(targetCache, imgUrl);
        if (image == null) image = HTTPService.getImage(imgUrl);
        if (image == null) {STATE.put(imgUrl, State.FAILED); return null;}
        if (tCache == "skins") {
            NativeImage normalized = convertLegacySkinIfNeeded(image);
            if (normalized != image) { image.close(); image = normalized; }
        }
        saveCached(targetCache, imgUrl, image);
        final NativeImage finalImage = image;
        Minecraft.getInstance().execute(() -> {
            DynamicTexture texture = new DynamicTexture(finalImage);
            ResourceLocation location = Minecraft.getInstance().getTextureManager()
                .register(targetCache + Integer.toHexString(imgUrl.hashCode()), texture);
            TEXTURES.put(imgUrl, new LoadedTexture(location, finalImage.getWidth(), finalImage.getHeight()));
            STATE.put(imgUrl, State.READY);
        });
        return null;
    };
    
    private static NativeImage convertLegacySkinIfNeeded(NativeImage src) {
        if (src.getHeight() >= 64) return src;
        NativeImage dst = new NativeImage(NativeImage.Format.RGBA, 64, 64, true);
        dst.fillRect(0, 0, 64, 64, 0);
        for (int x = 0; x < 64; x++) { for (int y = 0; y < 32; y++) { dst.setPixelRGBA(x, y, src.getPixelRGBA(x, y)); } }
        copyMirroredX(src, dst, 0, 16, 16, 48, 16, 16); copyMirroredX(src, dst, 40, 16, 32, 48, 16, 16);
        return dst;
    }

    private static void copyMirroredX(NativeImage src, NativeImage dst, int srcX, int srcY, int dstX, int dstY, int w, int h) {
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                int color = src.getPixelRGBA(srcX + x, srcY + y);
                dst.setPixelRGBA(dstX + (w - 1 - x), dstY + y, color);
            }
        }
    }

    public static void putTexture(String imgUrl, LoadedTexture texture) { STATE.put(imgUrl, State.READY); TEXTURES.put(imgUrl, texture); }

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
        } catch (Exception e) { e.printStackTrace(); return null; }
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