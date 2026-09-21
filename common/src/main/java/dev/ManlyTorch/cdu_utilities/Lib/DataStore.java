package dev.ManlyTorch.cdu_utilities.Lib;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import net.minecraft.client.Minecraft;

import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class DataStore {
    public static class AHEntry {
        public int price; public int amount; public int listingTime;
        public AHEntry(int price, int amount, int listingTime) { this.price = price; this.amount = amount; this.listingTime = listingTime; }
    }

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Type MAP_TYPE = new TypeToken<HashMap<String, HashMap<String, AHEntry>>>() {}.getType();
    private static Map<String, Map<String, AHEntry>> sellData = new HashMap<>();
    private static Path savePath;
    private static boolean loaded = false;
    private DataStore() {};

    private static void init() {
        if (loaded) return;
        loaded = true;
        savePath = Minecraft.getInstance().gameDirectory.toPath().resolve("config").resolve("CDU-Utilities").resolve("AutoSellItems.json");
        try {
            if (Files.exists(savePath)) {
                try (Reader r = Files.newBufferedReader(savePath)) {
                    Map<String, Map<String, AHEntry>> data = GSON.fromJson(r, MAP_TYPE);
                    if (data != null) sellData = data;
                }
            }
        } catch (Exception ignored) {};
        sellData.putIfAbsent("bulk", new HashMap<>());
        sellData.putIfAbsent("normal", new HashMap<>());
    };

    public static void save() {
        init();
        try {
            Files.createDirectories(savePath.getParent());
            try (Writer w = Files.newBufferedWriter(savePath)) { GSON.toJson(sellData, w); }
        } catch (Exception ignored) {};
    };

    public static void put(String type, String itemId, AHEntry entry) { init(); sellData.get(type).put(itemId, entry); save(); }
    public static AHEntry get(String type, String itemId) { init(); return sellData.get(type).get(itemId); }
    public static boolean has(String type, String itemId) { init(); return sellData.get(type).containsKey(itemId); }
    public static Map<String, AHEntry> getAll(String type) { init(); return sellData.get(type); }
}