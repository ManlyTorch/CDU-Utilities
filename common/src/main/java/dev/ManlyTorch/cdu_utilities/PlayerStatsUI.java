package dev.ManlyTorch.cdu_utilities;

import dev.ManlyTorch.cdu_utilities.Lib.*;
import dev.ManlyTorch.cdu_utilities.Lib.ImageCacher.LoadedTexture;
import dev.ManlyTorch.cdu_utilities.UI.Elements.*;
import dev.ManlyTorch.cdu_utilities.UI.Types.*;
import dev.ManlyTorch.cdu_utilities.UI.Enums.*;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonElement;

import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.Locale;
import java.util.UUID;

public class PlayerStatsUI {
    private static final int DISCORD_LINKED_COLOR = 0xff57f287;
    private static final int COLOR_TEXT_MUTED = 0xffafafaf;
    private static final int USERNAME_COLOR = 0xff55ffff;
    private static final int DIVIDER_COLOR = 0xff2a2a30;
    private static final int LBSPOT_COLOR = 0xff55ffff;
    private static final int AWARD_COLOR = 0xff2e2e2e;
    private static final int BLANK = 0x00000000;
    private static final int PADDING = 12;
    private static final int AWARD_GAP = 6;
    private static final int ROW_HEIGHT = 14;
    private static final int AWARD_SIZE = 28;
    private static final int AWARDS_PER_ROW = 10;
    private static final int STATS_WIDTH = 360;
    private static final int COPIED_COOLDOWN = 5;
    private static final int BOOSTER_SIZE = 14;

    private static final int AWARD_GAPSIZE = AWARD_GAP + AWARD_SIZE;
    private static final int blockTop = PADDING + 16 + 8;
    private static final int blockBottom = blockTop + 16 * ROW_HEIGHT;
    private static final int awardsY = blockBottom + 10;

    private static final ZoneId timezone = ZoneId.systemDefault();

    public static Long discordId;
    public static boolean discordLinked;
    public static Frame root;

    private static UIScreen screen = new UIScreen("CDUStatsDisplay");
    private static Screen returnScreen;
    public static TextButton closeButton;
    public static TextLabel lastSeenDate;
    public static TextButton discordLabel;
    public static TextLabel lastSeenLabel;
    public static TextLabel usernameLabel;
    public static SkinDisplay playerDisplay;
    public static ImageLabel serverBoosterImage;

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
    public static Map<String, String> lbStats = Map.ofEntries(
        Map.entry("playtime", "playtime"),
        Map.entry("mc_blocksbroken", "mc_blocks_broken"),
        Map.entry("mc_deaths", "mc_deaths"),
        Map.entry("mc_distance", "mc_distance"),
        Map.entry("mc_distanceclimbed", "mc_distance_climbed"),
        Map.entry("mc_distancecrouched", "mc_distance_crouched"),
        Map.entry("mc_distancefallen", "mc_distance_fallen"),
        Map.entry("mc_distanceflown", "mc_distance_flown"),
        Map.entry("mc_distancesprinted", "mc_distance_sprinted"),
        Map.entry("mc_distanceswum", "mc_distance_swum"),
        Map.entry("mc_distancewalked", "mc_distance_walked"),
        Map.entry("mc_jumps", "mc_jumps"),
        Map.entry("mc_mobskilled", "mc_mobs_killed"),
        Map.entry("mc_playerskilled", "mc_players_killed")
    );
    public static final List<String> rainbowUUIDs = List.of(
        "4772296d-7c7e-4744-9a78-953d76dd8ac0",
        "ac09fc69-61d0-4a36-bf33-e9f8e8f98cae",
        "59edeebc-4bd2-44e2-bb14-c8cc1d131530",
        "1418475b-1029-4a9a-af78-fbf5d59dfee0",
        "39bddbb3-e4ca-4be1-9b37-7ec36082817b"
    );
    public static final Map<Integer, String> daySuffixs = Map.of(
        1, "st",
        2, "nd",
        3, "rd"
    );
    public static final Map<Integer, Integer> lbColors = Map.of(
        2, 0xff515151,
        3, 0xffaa6428
    );

    public record StatRow(String idx, String val) {}
    public record StatLabels(TextLabel nameLabel, TextLabel statLBLabel, TextLabel valueLabel) {};

    public static final MutableComponent fetching = Component.literal("Fetching stats for ").withStyle(ChatFormatting.GRAY);

    private PlayerStatsUI() { buildRoot(); }
    public static PlayerStatsUI getInstance() { return classObj; }

    public static void buildRoot() {
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
        TextLabel lastSeenTxtLabel = new TextLabel()
            .setText(Component.literal("Last seen on:"))
            .setTextColor(COLOR_TEXT_MUTED)
            .setTextXAlignment(TextAlignment.LEFT)
            .setPosition(UDim2.fromOffset(PADDING, PADDING))
            .setAutomaticSize(true)
            .setBackgroundColor(BLANK)
            .setBorderColor(BLANK)
            .setParent(root);
        
        lastSeenLabel = new TextLabel()
            .setTextXAlignment(TextAlignment.LEFT)
            .setPosition(UDim2.fromScale(1, .5))
            .setAnchorPoint(new Vector2(0, .5))
            .setAutomaticSize(true)
            .setBackgroundColor(BLANK)
            .setBorderColor(BLANK)
            .setParent(lastSeenTxtLabel);
        
        lastSeenDate = new TextLabel()
            .setTextXAlignment(TextAlignment.LEFT)
            .setPosition(UDim2.fromScale(1, .5))
            .setAnchorPoint(new Vector2(0, .5))
            .setAutomaticSize(true)
            .setBackgroundColor(BLANK)
            .setBorderColor(BLANK)
            .setParent(lastSeenLabel);

        @SuppressWarnings("unused")
        Frame topDivider = new Frame()
            .setPosition(UDim2.fromOffset(PADDING, PADDING + 16))
            .setSize(new UDim2(1, -PADDING * 2, 0, 1))
            .setBackgroundColor(DIVIDER_COLOR)
            .setBorderColor(BLANK)
            .setParent(root);

        buildUser();
        buildAwards();
        buildStatRows();
    }
    
    public static void buildUser() {
        playerDisplay = new SkinDisplay()
            .setPosition(UDim2.fromOffset(PADDING, blockTop))
            .setSize(new UDim2(0.4, -PADDING*2 - 4, 0, statRows.size() * ROW_HEIGHT - PADDING))
            .setBackgroundColor(BLANK)
            .setParent(root);

        serverBoosterImage = new ImageLabel()
            .setImgURL("boosterIcon")
            .setPosition(new UDim2(1, 4, .5))
            .setAnchorPoint(new Vector2(0, .5))
            .setSize(UDim2.fromOffset(BOOSTER_SIZE, BOOSTER_SIZE))
            .setBackgroundColor(BLANK)
            .setBorderColor(BLANK);
        ImageCacher.putTexture("boosterIcon", new LoadedTexture(ResourceLocation.tryParse("ui:booster.png"), 250, 250));

        usernameLabel = new TextLabel()
            .setText(Component.literal("Username"))
            .setTextColor(USERNAME_COLOR)
            .setPosition(new UDim2(.5, 0, 1, 5))
            .setAnchorPoint(new Vector2(.5))
            .setAutomaticSize(true)
            .setBackgroundColor(BLANK)
            .setBorderColor(BLANK)
            .setParent(playerDisplay);

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
        List<Long> decayTime = new ArrayList<>();
        decayTime.add(0L);
        discordLabel.MouseHovered.onEvent(renderParams -> {
            if (discordLinked == false) return;
            Long curTime = System.currentTimeMillis();
            Component text = Component.literal((curTime <= decayTime.get(0) ? "Copied " + discordId : "Copy " + discordId));
            renderParams.gg().renderTooltip(screen.getFont(), text, renderParams.x(), renderParams.y());
        });
        discordLabel.MouseButton1Clicked.onEvent(renderParams -> {
            if (discordLinked == false) return;
            Minecraft.getInstance().keyboardHandler.setClipboard(String.valueOf(discordId));
            decayTime.set(0, System.currentTimeMillis() + COPIED_COOLDOWN*1000);
        });
    }
    
    public static void buildAwards() {
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
    }

    public static void buildStatRows() {
        int row = 0;
        UDim2 statSize = new UDim2(.6, -PADDING, 0, ROW_HEIGHT);
        for (StatRow statRow : statRows) {
            int statY = blockTop + row * ROW_HEIGHT;
            TextLabel statLabel = new TextLabel()
                .setText(Component.literal(statRow.idx()))
                .setTextColor(COLOR_TEXT_MUTED)
                .setTextXAlignment(TextAlignment.LEFT)
                .setPosition(new UDim2(.4, 0, 0, statY))
                .setSize(statSize)
                .setBackgroundColor(BLANK)
                .setBorderColor(BLANK)
                .setParent(root);
            TextLabel statValue = new TextLabel()
                .setText(Component.literal("NULL"))
                .setTextXAlignment(TextAlignment.RIGHT)
                .setPosition(new UDim2(1, -PADDING, 0, statY))
                .setAutomaticSize(true)
                .setBackgroundColor(BLANK)
                .setBorderColor(BLANK)
                .setParent(root);
            TextLabel statLBLabel = new TextLabel()
                .setText(Component.literal("#? "))
                .setTextXAlignment(TextAlignment.RIGHT)
                .setPosition(UDim2.fromScale(-1, .5))
                .setAnchorPoint(new Vector2(0, .5))
                .setAutomaticSize(true)
                .setBackgroundColor(BLANK)
                .setBorderColor(BLANK)
                .setParent(statValue);
            row++;
            statLabels.put(statRow.idx(), new StatLabels(statLabel, statLBLabel, statValue));
        }
    }

    public static void loadPlayerStats(String username, Screen prevScreen) {
        returnScreen = prevScreen;
        Minecraft mc = Minecraft.getInstance();
        Component userComp = Component.literal(username).withStyle(ChatFormatting.WHITE);
        mc.player.displayClientMessage(fetching.copy().append(userComp), true);
        runThread(() -> {
            String uuid = MojangService.getUUIDfromName(username.toLowerCase());
            if (uuid == null) return;
            String[] skinURL = new String[1];
            JsonObject[] playerStats = new JsonObject[1];
            List<Thread> threads = new ArrayList<>();
            // SKIN
            threads.add(runThread(() -> skinURL[0] = MojangService.getSkin(uuid)));
            // CDU
            for (String statCategory : lbStats.values()) { threads.add(runThread(() -> CDUService.getLeaderboard(statCategory))); }
            threads.add(runThread(() -> {
                playerStats[0] = CDUService.getPlayerData(uuid, username);
                List<Thread> awardThreads = new ArrayList<>();
                for (JsonElement element : playerStats[0].getAsJsonArray("player_awards")) {
                   String imgUrl = element.getAsJsonObject().get("img_url").getAsString();
                   awardThreads.add(runThread(() -> ImageCacher.fetchImage("awards", imgUrl)));
                }
                for (Thread t : awardThreads) {
                   try { t.join(); }
                   catch (InterruptedException e) { Thread.currentThread().interrupt(); return; }
                }
            }));
            for (Thread t : threads) {
               try { t.join(); }
               catch (InterruptedException e) { Thread.currentThread().interrupt(); return; }
            }
            if (playerStats[0] == null || skinURL[0] == null) return;
            updateUI(playerStats[0], playerStats[0].get("username").getAsString(), MojangService.UUIDFromString(uuid), skinURL[0]);
            mc.execute(() -> mc.setScreen(screen));
        });
    };

    public static Thread createThread(Runnable callback) {
        Thread t = new Thread(callback);
        t.setUncaughtExceptionHandler((thread, ex) -> { ex.printStackTrace(); });
        return t;
    };
    public static Thread runThread(Runnable callback) { Thread t = createThread(callback); t.start(); return t; }

    private static void updateUI(JsonObject playerStats, String username, UUID uuid, String skin_url) {
        // time
        Instant utcInstant = Instant.parse("2026-09-17T03:09:53.000Z");
        ZonedDateTime localTime = utcInstant.atZone(timezone);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(" 'on' MM/dd/yyyy", Locale.ENGLISH);
        Component formatted = Component.literal(localTime.format(formatter));

        // misc
        usernameLabel.setText(Component.literal(username)).setRainbowText(rainbowUUIDs.contains(uuid.toString()));
        serverBoosterImage.setParent(playerStats.get("isserverbooster").getAsBoolean() ? usernameLabel : null);
        lastSeenLabel.setText(Component.literal(" " + playerStats.get("lastjoinedservername").getAsString()));
        lastSeenDate.setText(formatted);

        discordId = playerStats.get("discordid").isJsonNull() ? null : playerStats.get("discordid").getAsLong();
        discordLinked = discordId != null;
        int discordColor = discordLinked ? DISCORD_LINKED_COLOR : COLOR_TEXT_MUTED;
        discordLabel.setTextColor(discordColor);
        discordLabel.setText(Component.literal(discordLinked ? "Discord Linked" : "Discord Not Linked")
            .withStyle(style -> style.withUnderlined(discordLinked)));
        
        playerDisplay.setSkinURL(skin_url)
            .setUsername(username)
            .setUUID(uuid)
            .createFakePlayer();

        // stats
        for (StatRow statRow : statRows) {
            String displayName = statRow.idx(); String stat = statRow.val(); JsonElement element = playerStats.get(stat);
            String strVal = displayName == "Playtime" ? element.getAsString() : formatNumber(element.getAsLong());
            StatLabels labels = statLabels.get(displayName);
            labels.valueLabel().setText(Component.literal(strVal));
            TextLabel lbLabel = labels.statLBLabel();
            lbLabel.setParent(null);
            if (lbStats.get(stat) == null) continue;
            Integer lbSpot = CDUService.getLBSpot(uuid.toString(), lbStats.get(stat));
            if (lbSpot == null) continue;
            lbLabel.setParent(labels.valueLabel()).setText(Component.literal("#" + lbSpot + " ")).setRainbowText(lbSpot == 1)
                .setTextColor(lbColors.get(lbSpot) != null ? lbColors.get(lbSpot) : LBSPOT_COLOR);
        }

        // awards
        int awardY = PADDING + (16 * ROW_HEIGHT) + AWARD_GAP + 46;
        int curX = PADDING;
        int lastRow = 0;
        int curAward = 0;
        List<String> nonDuplicateAwards = new ArrayList<>();
        for (JsonElement element : playerStats.getAsJsonArray("player_awards")) {
            if (!element.isJsonObject()) continue;
            JsonObject award = element.getAsJsonObject();
            String awardName = award.get("award_name").getAsString();
            if (awardName.equals("None") || nonDuplicateAwards.contains(awardName)) continue;
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
        screen.recalculate();
    }

    public static void close() {
        Minecraft mc = Minecraft.getInstance();
        mc.setScreen(returnScreen instanceof ChatScreen ? null : returnScreen);
    };

    private static String formatNumber(long value) { return String.format(Locale.US, "%,d", value);};

    private static PlayerStatsUI classObj = new PlayerStatsUI();
}