package dev.ManlyTorch.cdu_utilities;

import dev.ManlyTorch.cdu_utilities.Lib.HTTPService;
import dev.ManlyTorch.cdu_utilities.Lib.ImageCacher;
import dev.ManlyTorch.cdu_utilities.Lib.ImageCacher.LoadedTexture;
import dev.ManlyTorch.cdu_utilities.UI.Elements.*;
import dev.ManlyTorch.cdu_utilities.UI.Types.*;
import dev.ManlyTorch.cdu_utilities.UI.Enums.*;
import dev.ManlyTorch.cdu_utilities.UI.Elements.ImageLabel.BlitOptions;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonElement;
import com.mojang.authlib.GameProfile;

import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/*TODO:
- Make it FUNCTION
- Awards
*/

public class PlayerStatsUI {
    // private static final Logger LOGGER = LogUtils.getLogger();
    private static final int DISCORD_LINKED_COLOR = 0xff57f287;
    private static final int COLOR_TEXT_MUTED = 0xffafafaf;
    private static final int DIVIDER_COLOR = 0xff2a2a30;
    private static final int AWARD_COLOR = 0xff2e2e2e;
    private static final int BLANK = 0x00000000;
    private static final int PADDING = 12;
    private static final int AWARD_GAP = 6;
    private static final int HEAD_SIZE = 64;
    private static final int ROW_HEIGHT = 14;
    private static final int AWARD_SIZE = 28;
    private static final int AWARDS_PER_ROW = 10;
    private static final int STATS_WIDTH = 360;
    private static final int COPIED_COOLDOWN = 5;
    private static Screen returnScreen;
    public static Long discordId;
    public static boolean discordLinked;
    public static Frame root;
    public static ImageLabel playerImage;
    public static TextButton closeButton;
    public static TextButton discordLabel;
    public static TextLabel lastSeenLabel;
    public static TextLabel usernameLabel;
    public static JsonObject playerStats;
    public static Map<String, ImageLabel> awardLabels = new HashMap<>();
    public static Map<String, StatLabels> statLabels = new HashMap<>();
    public static List<StatRow> statRows = List.of(
        new StatRow("Playtime", "playtime"),
        new StatRow("Unique Servers", "uniqueserversseenon"),
        new StatRow("Blocks Broken", "mc_blocksbroken"),
        new StatRow("Deaths", "mc_deaths"),
        new StatRow("Jumps", "mc_jumps"),
        new StatRow("Mobs Killed", "mc_mobskilled"),
        new StatRow("Players Killed", "mc_playerskilled"),
        new StatRow("Votes", "votes"),
        new StatRow("Distance (Total)", "mc_distance"),
        new StatRow("Climbed", "mc_distanceclimbed"),
        new StatRow("Crouched", "mc_distancecrouched"),
        new StatRow("Fallen", "mc_distancefallen"),
        new StatRow("Flown", "mc_distanceflown"),
        new StatRow("Sprinted", "mc_distancesprinted"),
        new StatRow("Swum", "mc_distanceswum"),
        new StatRow("Walked", "mc_distancewalked")
    );
    public record StatRow(String idx, String val) {}

    private static UIScreen screen = new UIScreen("CDUStatsDisplay");

    private PlayerStatsUI() { loadCache(); buildLayout(); }
    public static PlayerStatsUI getInstance() { return classObj; }

    private static final int AWARD_GAPSIZE = AWARD_GAP + AWARD_SIZE;

    public static void buildLayout() {
        int blockTop = PADDING + 16 + 8;
        int blockBottom = blockTop + 16 * ROW_HEIGHT;
        int awardsY = blockBottom + 10;

        root = new Frame()
            .setPosition(UDim2.fromScale(0.5, 0.5))
            .setAnchorPoint(new Vector2(0.5, 0.5))
            .setSize(UDim2.fromOffset(STATS_WIDTH, 300));
        screen.setRootFrame(root);

        int closeSize = 20;
        closeButton = new TextButton()
            .setText(Component.literal("X"))
            .setPosition(new UDim2(1, -closeSize - 6, 0, 6))
            .setSize(UDim2.fromOffset(closeSize, closeSize))
            .setBackgroundColor(BLANK)
            .setParent(root);
        closeButton.MouseButton1Clicked.onEvent(data -> close());

        @SuppressWarnings("unused")
        TextLabel lastSeenDisplay = new TextLabel()
            .setText(Component.literal("Last seen"))
            .setTextColor(COLOR_TEXT_MUTED)
            .setTextXAlignment(TextAlignment.LEFT)
            .setPosition(UDim2.fromOffset(PADDING, PADDING))
            .setSize(new UDim2(0.25, 0, 0, 16))
            .setBackgroundColor(BLANK)
            .setBorderColor(BLANK)
            .setParent(root);
        
        lastSeenLabel = new TextLabel()
            .setTextXAlignment(TextAlignment.RIGHT)
            .setPosition(UDim2.fromOffset(PADDING, PADDING))
            .setSize(new UDim2(1, -PADDING - closeSize - 10, 0, 16))
            .setBackgroundColor(BLANK)
            .setBorderColor(BLANK)
            .setParent(root);

        @SuppressWarnings("unused")
        Frame topDivider = new Frame()
            .setPosition(UDim2.fromOffset(PADDING, PADDING + 16))
            .setSize(new UDim2(1, -PADDING * 2, 0, 1))
            .setBackgroundColor(DIVIDER_COLOR)
            .setBorderColor(BLANK)
            .setParent(root);

        playerImage = new ImageLabel()
            .setTCache("skins")
            .addBlitOption(new BlitOptions(8f, 8f, 8, 8, 64, 64))
            .addBlitOption(new BlitOptions(40f, 8f, 8, 8, 64, 64))
            .setPosition(UDim2.fromOffset(PADDING, blockTop))
            .setSize(UDim2.fromOffset(HEAD_SIZE, HEAD_SIZE))
            .setBackgroundColor(BLANK)
            .setParent(root);

        usernameLabel = new TextLabel()
            .setText(Component.literal("Username"))
            .setTextColor(0xff55ffff)
            .setPosition(new UDim2(.5, 0, 1, 5))
            .setAnchorPoint(new Vector2(.5))
            .setAutomaticSize(true)
            .setBackgroundColor(BLANK)
            .setBorderColor(BLANK)
            .setParent(playerImage);

        discordLabel = new TextButton()
            .setText(Component.literal("Discord Not Linked"))
            .setTextColor(COLOR_TEXT_MUTED)
            .setPosition(new UDim2(.5, 0, 1, 5))
            .setAnchorPoint(new Vector2(.5))
            .setAutomaticSize(true)
            .setBackgroundColor(BLANK)
            .setBorderColor(BLANK)
            .setHoverColor(BLANK)
            .setParent(usernameLabel);
        float[] decayTime = { System.currentTimeMillis() };
        discordLabel.MouseHovered.onEvent(renderParams -> {
            if (discordLinked == false) return;
            float curTime = System.currentTimeMillis();
            Component text = Component.literal(curTime <= decayTime[0] ? "Copied " + discordId : "Copy " + discordId);
            renderParams.gg().renderTooltip(screen.getFont(), text, renderParams.x(), renderParams.y());
        });
        discordLabel.MouseButton1Clicked.onEvent(renderParams -> {
            if (discordLinked == false) return;
            Minecraft.getInstance().keyboardHandler.setClipboard(String.valueOf(discordId));
            decayTime[0] = System.currentTimeMillis() + COPIED_COOLDOWN*1000;
        });

        @SuppressWarnings("unused")
        TextLabel awardsHeader = new TextLabel()
            .setText(Component.literal("Awards"))
            .setTextXAlignment(TextAlignment.CENTER)
            .setPosition(new UDim2(.5, 0, 0, awardsY - 6))
            .setAnchorPoint(new Vector2(.5))
            .setSize(UDim2.fromOffset(100, 12))
            .setBackgroundColor(BLANK)
            .setBorderColor(BLANK)
            .setParent(root);
        
        @SuppressWarnings("unused")
        Frame awardsDivider = new Frame()
            .setBackgroundColor(DIVIDER_COLOR)
            .setBorderColor(BLANK)
            .setPosition(UDim2.fromOffset(PADDING, awardsY + 12))
            .setSize(new UDim2(1, -PADDING * 2, 0, 1))
            .setParent(root);

        int statsX = PADDING + HEAD_SIZE + PADDING * 2;
        int row = 0;
        UDim2 statSize = new UDim2(1, -statsX - PADDING, 0, ROW_HEIGHT);
        for (StatRow statRow : statRows) {
            UDim2 rowPos = UDim2.fromOffset(statsX, blockTop + row * ROW_HEIGHT);
            TextLabel statLabel = new TextLabel()
                .setText(Component.literal(statRow.idx()))
                .setTextColor(COLOR_TEXT_MUTED)
                .setTextXAlignment(TextAlignment.LEFT)
                .setPosition(rowPos)
                .setSize(statSize)
                .setBackgroundColor(BLANK)
                .setBorderColor(BLANK)
                .setParent(root);
            TextLabel statValue = new TextLabel()
                .setText(Component.literal("NULL"))
                .setTextXAlignment(TextAlignment.RIGHT)
                .setPosition(rowPos)
                .setSize(statSize)
                .setBackgroundColor(BLANK)
                .setBorderColor(BLANK)
                .setParent(root);
            row++;
            statLabels.put(statRow.idx(), new StatLabels(statLabel, statValue));
        }
    }

    public static void loadPlayerStats(String username, Screen prevScreen) {
        returnScreen = prevScreen;
        Minecraft mc = Minecraft.getInstance();
        Thread t = new Thread(() -> { threadedPlayerStats(mc, username); });
        t.setUncaughtExceptionHandler((thread, ex) -> { ex.printStackTrace(); });
        t.start();
    }

    private static void threadedPlayerStats(Minecraft mc, String username) {
        Component msg = Component.literal("Fetching stats for ").withStyle(ChatFormatting.GRAY)
            .append(Component.literal(username).withStyle(ChatFormatting.WHITE));
        mc.player.displayClientMessage(msg, true);
        String uuid = resolveUUID(username.toLowerCase());
        if (uuid == null) {
            mc.player.displayClientMessage(
                Component.literal("User ").withStyle(ChatFormatting.RED)
                    .append(Component.literal(username).withStyle(ChatFormatting.WHITE)
                        .append(Component.literal(" does not exist.").withStyle(ChatFormatting.RED))),
                false
            );
            return;
        }

        // check if the user has played or get req failed
        JsonObject fetchedData = fetchCDUData(uuid, username);
        if (fetchedData == null) {
            mc.player.displayClientMessage(Component.literal("Couldn't load stats for ").withStyle(ChatFormatting.RED)
            .append(Component.literal(username).withStyle(ChatFormatting.WHITE)), true);
            return;
        }
        if (!fetchedData.has("player_stats")) {
            if (fetchedData.has("details")
                && fetchedData.get("details").getAsString().equals("'NoneType' object has no attribute 'get'")) {
                Component message = Component.literal("User ").withStyle(ChatFormatting.RED)
                    .append(Component.literal(username).withStyle(ChatFormatting.WHITE)
                        .append(Component.literal(" hasn't played CDU.").withStyle(ChatFormatting.RED)));
                mc.player.displayClientMessage(message, true);
                return;
            } else System.err.println("CDU API returned unexpected JSON: " + fetchedData.toString());
        }
        JsonObject playerStats = fetchedData.getAsJsonObject("player_stats");
        JsonArray awards = playerStats.getAsJsonArray("player_awards");
        String skin_url = SkinManager.getSkin(uuid, username);
        for (JsonElement element : awards) {
            JsonObject award = element.getAsJsonObject();
            String imgUrl = award.get("img_url").getAsString();
            ImageCacher.fetchImage("awards", imgUrl);
        }

        // Update UI inside main thread
        mc.execute(() -> {
            // misc
            usernameLabel.setText(Component.literal(playerStats.get("username").getAsString()));
            lastSeenLabel.setText(Component.literal(playerStats.get("lastjoinedservername").getAsString()));

            discordId = playerStats.get("discordid").getAsLong();
            discordLinked = discordId != null;
            int discordColor = discordLinked ? DISCORD_LINKED_COLOR : COLOR_TEXT_MUTED;
            discordLabel.setTextColor(discordColor);
            discordLabel.setText(Component.literal(discordLinked ? "Discord Linked" : "Discord Not Linked")
                .withStyle(style -> style.withUnderlined(discordLinked)));

            if (skin_url != null) {
                playerImage.imgURL = skin_url;
            } else {
                playerImage.backupImg = new LoadedTexture(DefaultPlayerSkin.getDefaultSkin(UUID.fromString(playerStats.get("uuid").getAsString())), 64, 64);
            }

            // stats
            for (StatRow statRow : statRows) {
                String strVal = statRow.idx() == "Playtime" ? playerStats.get(statRow.val()).getAsString() : formatNumber(playerStats.get(statRow.val()).getAsLong());
                statLabels.get(statRow.idx()).valueLabel().setText(Component.literal(strVal));
            }

            // awards
            int awardY = PADDING + (16 * ROW_HEIGHT) + AWARD_GAP + 46;
            int curX = PADDING;
            int lastRow = 0;
            int curAward = 0;
            List<String> nonDuplicateAwards = new ArrayList<>();
            for (JsonElement element : awards) {
                if (!element.isJsonObject()) continue;
                JsonObject award = element.getAsJsonObject();
                String awardName = award.get("award_name").getAsString();
                if (nonDuplicateAwards.contains(awardName)) continue;
                nonDuplicateAwards.add(awardName);
                ImageLabel awardDisplay = awardLabels.get(awardName);
                if (awardDisplay == null) {
                    Component desc = Component.literal(award.get("award_description").getAsString());
                    String imgUrl = award.get("img_url").getAsString();
                    awardDisplay = new ImageLabel()
                        .setImgURL(imgUrl)
                        .setTCache("awards")
                        .setBackgroundColor(AWARD_COLOR)
                        .setSize(UDim2.fromOffset(AWARD_SIZE, AWARD_SIZE));
                    awardLabels.put(awardName, awardDisplay);
                    awardDisplay.MouseHovered.onEvent(renderParams -> {
                        renderParams.gg().renderTooltip(screen.getFont(), desc, renderParams.x(), renderParams.y());
                    });
                }
                int curRow = (int)Math.floor(curAward / AWARDS_PER_ROW);
                if (curRow != lastRow) { curX = PADDING; lastRow = curRow; }
                if (curAward - curRow * AWARDS_PER_ROW == 5) curX += 2;
                awardDisplay
                    .setPosition(UDim2.fromOffset(curX, awardY + curRow * AWARD_GAPSIZE))
                    .setParent(root);
                curX += AWARD_GAPSIZE;
                curAward++;
            }

            for (Map.Entry<String, ImageLabel> entry : awardLabels.entrySet()) {
                ImageLabel label = entry.getValue();
                String awardName = entry.getKey();
                if (nonDuplicateAwards.contains(awardName) == false) label.setParent(null);
            }

            int curRow = (int)Math.floor((nonDuplicateAwards.size()-1) / AWARDS_PER_ROW);
            int computedHeight = awardY + AWARD_SIZE + PADDING + curRow * AWARD_GAPSIZE;
            root.setSize(UDim2.fromOffset(STATS_WIDTH, computedHeight));

            mc.setScreen(screen);
            screen.recalculate();
        });
    }

    public static void close() {
        Minecraft mc = Minecraft.getInstance();
        mc.setScreen(returnScreen instanceof ChatScreen ? null : returnScreen);
    };

    public record StatLabels(TextLabel nameLabel, TextLabel valueLabel) {};
    private static String formatNumber(long value) { return String.format(Locale.US, "%,d", value);};

    private static final long STATS_CACHE_MS = 300_000;
    private static final ConcurrentHashMap<String, CacheEntry> STATS_CACHE = new ConcurrentHashMap<>();
    private record CacheEntry(JsonObject data, long timestamp) {};

    public static JsonObject fetchCDUData(String uuid, String username) {
        CacheEntry cached = STATS_CACHE.get(uuid);
        if (cached != null && System.currentTimeMillis() - cached.timestamp() < STATS_CACHE_MS) return cached.data().deepCopy();
        try {
            JsonObject stats = HTTPService.getJson("api.playcdu.co/users/uuid/?uuid=" + uuid);
            STATS_CACHE.put(uuid, new CacheEntry(stats, System.currentTimeMillis()));
            return stats.deepCopy();
        } catch (IOException e) { e.printStackTrace(); }
        return null;
    };

    private static final File UUID_FILE = new File("cdu_usercache.json");
    private static final Map<String, String> UUID_CACHE = new HashMap<>();

    public static String resolveUUID(String username) {
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
            }
        }
        return fetchUUID(username);
    }

    public static String fetchUUID(String username) {
        try {
            String uuid = HTTPService.getJson("api.mojang.com/users/profiles/minecraft/" + username).get("id").getAsString();
            UUID_CACHE.put(username, uuid); saveCache(); return uuid;
        } catch (Exception e) {
            throw new RuntimeException("Couldn't resolve a UUID for '" + username + "': " + e.getMessage(), e);
        }
    }

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

    private static PlayerStatsUI classObj = new PlayerStatsUI();
}