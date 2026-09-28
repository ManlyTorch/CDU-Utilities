package dev.ManlyTorch.cdu_utilities;

import dev.ManlyTorch.cdu_utilities.Lib.*;
import dev.ManlyTorch.cdu_utilities.UI.Elements.*;
import dev.ManlyTorch.cdu_utilities.UI.Types.*;
import dev.ManlyTorch.cdu_utilities.UI.Enums.*;

import com.mojang.logging.LogUtils;

import org.slf4j.Logger;

import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
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
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerStatsUI {
    private static final Logger LOGGER = LogUtils.getLogger();
    // Colors
    private static final int DISCORD_LINKED_COLOR = 0xff57f287;
    private static final int COLOR_TEXT_MUTED = 0xffafafaf;
    private static final int USERNAME_COLOR = 0xff55ffff;
    private static final int DIVIDER_COLOR = 0xff2a2a30;
    private static final int LBSPOT_COLOR = 0xff55ffff;
    private static final int AWARD_COLOR = 0xff2e2e2e;
    private static final int BLANK = 0x00000000;
    // Size values
    private static final int PADDING = 6;
    private static final int AWARD_GAP = 6;
    private static final int AWARD_SIZE = 33;
    private static final int CLOSE_SIZE = 20;
    private static final int ROOT_WIDTH = 400;
    private static final int BOOSTER_SIZE = 14;
    private static final int STATS_PADDING = 2;
    private static final int AWARDS_PER_ROW = 10;
    private static final int COPIED_COOLDOWN = 5;
    private static final int STAT_INNER_PADDING = 3;
    
    private static final ZoneId timezone = ZoneId.systemDefault();

    public static Long discordId;
    public static boolean discordLinked;
    public static Frame root;
    
    private static UIScreen screen = new UIScreen("CDUStatsDisplay");
    private static Screen returnScreen;
    public static TextButton closeButton;
    public static TextLabel lastJoin;
    public static TextButton discordLabel;
    public static TextLabel lastServer;
    public static TextLabel usernameLabel;
    public static SkinDisplay playerDisplay;
    public static ImageLabel serverBoosterImage;
    public static TextLabel leaderboardHeader;
    public static TextLabel awardsHeader;
    public static TextLabel firstJoin;
    public static TextLabel rankLabel;
    public static Frame awardsDivider;
    public static Frame lbDivider;

    public static final Map<String, ImageLabel> awardLabels = new HashMap<>();
    public static final Map<String, StatLabel> statLabels = new HashMap<>();
    public static final List<StatRow> statRows = List.of(
        new StatRow("Playtime", "playtime", true),
        new StatRow("Blocks Broken", "mc_blocks_broken", true),
        new StatRow("Wealth", "total_balance", true),
        new StatRow("Money Pit", "money_pit_total", true),
        new StatRow("Mobs Killed", "mc_mobs_killed", true),
        new StatRow("Players Killed", "mc_players_killed", true),
        new StatRow("Deaths", "mc_deaths", true),
        new StatRow("Jumps", "mc_jumps", true),
        new StatRow("Distance (Total)", "mc_distance", true),
        new StatRow("Climbed", "mc_distance_climbed", true),
        new StatRow("Crouched", "mc_distance_crouched", true),
        new StatRow("Fallen", "mc_distance_fallen", true),
        new StatRow("Flown", "mc_distance_flown", true),
        new StatRow("Sprinted", "mc_distance_sprinted", true),
        new StatRow("Swum", "mc_distance_swum", true),
        new StatRow("Walked", "mc_distance_walked", true),
        // new StatRow("Donated", "donations", true),
        new StatRow("Servers Explored", "uniqueserversseenon", false),
        new StatRow("Votes", "votes", false),
        new StatRow("Average Rank", "overall_rank", true)
    );
    public static final List<String> rainbowUUIDs = List.of(
        "4772296d-7c7e-4744-9a78-953d76dd8ac0",
        "ac09fc69-61d0-4a36-bf33-e9f8e8f98cae",
        "59edeebc-4bd2-44e2-bb14-c8cc1d131530",
        "1418475b-1029-4a9a-af78-fbf5d59dfee0",
        "39bddbb3-e4ca-4be1-9b37-7ec36082817b"
    );
    public static final Map<Integer, Integer> lbColors = Map.of(
        2, 0xff727272,
        3, 0xffaa6428
    );
    public static final Map<Integer, String> suffixs = Map.of(
        1, "st",
        2, "nd",
        3, "rd",
        21, "st",
        22, "nd",
        23, "rd",
        31, "st"
    );
    public static final List<String> customStatPos = List.of("overall_rank", "uniqueserversseenon", "votes");
    public static final Map<String, Integer> statIconSizes = Map.of(
        "money_pit_total", 488,
        "total_balance", 160,
        "uniqueserversseenon", 48,
        "votes", 34
    );
    public static final Map<String, Float> statRots = Map.of("money_pit_total", 10f);

    private static final int AWARD_GAPSIZE = AWARD_GAP + AWARD_SIZE;
    private static final int STATS_DISPLAY_SIZE = 28;
    private static final int STATS_HEIGHT = STATS_DISPLAY_SIZE + STAT_INNER_PADDING*2;
    private static final int TOTAL_STAT_HEIGHT = 8 * (STATS_HEIGHT + STATS_PADDING)-STATS_PADDING;
    private static final int STATS_TOP = STATS_HEIGHT + PADDING + STATS_PADDING;
    private static final int STATS_BOTTOM = STATS_TOP + TOTAL_STAT_HEIGHT;
    private static final int STATS_TEXT_X = STATS_DISPLAY_SIZE + STAT_INNER_PADDING + STATS_PADDING;
    private static final int DISPLAY_HEIGHT = TOTAL_STAT_HEIGHT - (int)((STATS_HEIGHT + STATS_PADDING)*3.5);
    private static final int DISPLAY_OFFSET = -PADDING*2 - 20;
    private static final int STATS_XOFFSET = DISPLAY_OFFSET + STATS_PADDING + PADDING;
    private static final int STAT_SIZE_XOFFSET = 20-STATS_PADDING*3-PADDING;

    private static final UDim2 STAT_SIZE = new UDim2(.3, STAT_SIZE_XOFFSET, 0, STATS_HEIGHT);

    public record StatRow(String displayName, String statName, Boolean hasLB) {}
    public record StatLabel(Frame rootFrame, TextLabel displayName, TextLabel lbLbl, TextLabel value) {};

    public static final MutableComponent fetching = Component.literal("Fetching stats for ").withStyle(ChatFormatting.GRAY);

    private PlayerStatsUI() { buildRoot(); updateLeaderboards(new ArrayList<>()); }
    public static PlayerStatsUI getInstance() { return classObj; }

    public static void buildRoot() {
        root = new Frame()
            .setPosition(UDim2.fromScale(0.5, 0.5))
            .setAnchorPoint(new Vector2(0.5, 0.5))
            .setSize(UDim2.fromOffset(ROOT_WIDTH, 300));
        screen.setRootFrame(root);

        closeButton = new TextButton()
            .setText(Component.literal("X"))
            .setPosition(new UDim2(1, -CLOSE_SIZE - PADDING, 0, PADDING + STATS_HEIGHT/2 - CLOSE_SIZE/2))
            .setSize(UDim2.fromOffset(CLOSE_SIZE, CLOSE_SIZE))
            .setBackgroundColor(BLANK)
            .setParent(root);
        closeButton.MouseButton1Clicked.onEvent(data -> close());

        buildUser();
        buildAwards();
        buildStats();
    }
    
    public static void buildUser() {
        playerDisplay = new SkinDisplay()
            .setPosition(UDim2.fromOffset(PADDING, PADDING + 28))
            .setSize(new UDim2(0.4, DISPLAY_OFFSET, 0, DISPLAY_HEIGHT))
            .setBackgroundColor(BLANK)
            .setParent(root);

        serverBoosterImage = new ImageLabel()
            .setImgPath("ui:booster.png", 250, 250)
            .setPosition(new UDim2(1, 4, .5))
            .setAnchorPoint(new Vector2(0, .5))
            .setSize(UDim2.fromOffset(BOOSTER_SIZE, BOOSTER_SIZE))
            .setBackgroundColor(BLANK)
            .setBorderColor(BLANK);

        usernameLabel = new TextLabel()
            .setText(Component.literal("Username"))
            .setTextColor(USERNAME_COLOR)
            .setPosition(new UDim2(.5, 0, 0, -2))
            .setAnchorPoint(new Vector2(.5, 1))
            .setAutomaticSize(true)
            .setBackgroundColor(BLANK)
            .setBorderColor(BLANK)
            .setParent(playerDisplay);
        
        rankLabel = new TextLabel()
            .setText(Component.literal("RANK"))
            .setPosition(new UDim2(.5, 0, 0, -24))
            .setAnchorPoint(new Vector2(.5))
            .setAutomaticSize(true)
            .setBackgroundColor(BLANK)
            .setBorderColor(BLANK)
            .setParent(playerDisplay);
        
        Frame serversExplored = buildLabelsForStat(statRows.get(16)).rootFrame()
            .setPosition(new UDim2(0, 0, 1, 2))
            .setSize(new UDim2(1, 0, 0, STATS_HEIGHT))
            .setParent(playerDisplay);
        Frame votes = buildLabelsForStat(statRows.get(17)).rootFrame()
            .setPosition(new UDim2(0, 0, 1, STATS_PADDING))
            .setSize(UDim2.fromScale(1, 1))
            .setParent(serversExplored);

        TextLabel lastServerLbl = buildLabelTemplate("Last Server: ", votes)
            .setPosition(new UDim2(.5, 0, 1, 4));
        TextLabel lastJoinLbl = buildLabelTemplate("Last Joined: ", lastServerLbl);
        TextLabel firstJoinLbl = buildLabelTemplate("First Joined: ", lastJoinLbl);
        lastServer = buildBlank(lastServerLbl);
        lastJoin = buildBlank(lastJoinLbl);
        firstJoin = buildBlank(firstJoinLbl);

        discordLabel = new TextButton()
            .setText(Component.literal("Discord Not Linked"))
            .setTextColor(COLOR_TEXT_MUTED)
            .setPosition(new UDim2(.5, 0, 2, 4))
            .setAnchorPoint(new Vector2(.5))
            .setAutomaticSize(true)
            .setBackgroundColor(BLANK)
            .setBorderColor(BLANK)
            .setHoverColor(BLANK)
            .setParent(firstJoinLbl);

        List<Long> decayTime = new ArrayList<>();
        decayTime.add(0L);
        discordLabel.MouseHovered.onEvent(renderParams -> {
            if (discordLinked == false) return;
            long curTime = System.currentTimeMillis();
            Component text = Component.literal((curTime <= decayTime.get(0) ? "Copied " + discordId : "Copy " + discordId));
            renderParams.gg().renderTooltip(screen.getFont(), text, renderParams.x(), renderParams.y());
        });
        discordLabel.MouseButton1Clicked.onEvent(renderParams -> {
            if (discordLinked == false) return;
            Minecraft.getInstance().keyboardHandler.setClipboard(String.valueOf(discordId));
            decayTime.set(0, System.currentTimeMillis() + COPIED_COOLDOWN*1000);
        });
    }
    
    public static TextLabel buildLabelTemplate(String str, Frame parent) {
        return new TextLabel()
            .setText(Component.literal(str))
            .setTextColor(COLOR_TEXT_MUTED)
            .setTextXAlignment(TextAlignment.LEFT)
            .setTextYAlignment(TextAlignment.TOP)
            .setPosition(new UDim2(.5, 0, 2, 4))
            .setAnchorPoint(new Vector2(.5))
            .setAutomaticSize(true)
            .setBackgroundColor(BLANK)
            .setBorderColor(BLANK)
            .setParent(parent);
    }
    public static TextLabel buildBlank(Frame parent) {
        return new TextLabel()
            .setText(Component.literal("Unknown"))
            .setPosition(new UDim2(.5, 0, 1, 2))
            .setAnchorPoint(new Vector2(.5))
            .setAutomaticSize(true)
            .setBackgroundColor(BLANK)
            .setBorderColor(BLANK)
            .setParent(parent);
    }
    
    public static void buildAwards() {
        awardsHeader = new TextLabel()
            .setText(Component.literal("Awards"))
            .setTextXAlignment(TextAlignment.CENTER)
            .setPosition(new UDim2(.5, 0, 0, STATS_BOTTOM + 4))
            .setAnchorPoint(new Vector2(.5))
            .setSize(UDim2.fromOffset(100, 12))
            .setBackgroundColor(BLANK)
            .setBorderColor(BLANK)
            .setParent(root);
        
        awardsDivider = new Frame()
            .setBackgroundColor(DIVIDER_COLOR)
            .setBorderColor(BLANK)
            .setPosition(UDim2.fromOffset(PADDING, STATS_BOTTOM + 22))
            .setSize(new UDim2(1, -PADDING * 2, 0, 1))
            .setParent(root);
    }

    public static void buildStats() {
        StatLabel overallRank = buildLabelsForStat(statRows.get(18));
        overallRank.rootFrame()
            .setPosition(new UDim2(.4, STATS_XOFFSET, 0, PADDING))
            .setSize(new UDim2(.6, -STATS_XOFFSET - PADDING*2 - CLOSE_SIZE, 0, STATS_HEIGHT));
        int idx = 0;
        for (StatRow statRow : statRows) {
            if (customStatPos.contains(statRow.statName())) continue; idx++;
            boolean isEven = idx%2==1;
            double xScale = .4 + (isEven ? 0 : .3);
            int xOffset = STATS_XOFFSET + (isEven ? 0 : Math.abs(STAT_SIZE_XOFFSET) + STATS_PADDING);
            int yOffset = STATS_TOP + (STATS_HEIGHT + STATS_PADDING) * (int)Math.floor((idx-1)/2);
            buildLabelsForStat(statRow).rootFrame()
                .setPosition(new UDim2(xScale, xOffset, 0, yOffset))
                .setSize(STAT_SIZE);
        }
    }

    public static StatLabel buildLabelsForStat(StatRow statRow) {
        Frame statRoot = new Frame()
            .setBackgroundColor(BLANK)
            .setParent(root);
        int iconSize = statIconSizes.getOrDefault(statRow.statName(), 90);
        @SuppressWarnings("unused")
        ImageLabel displayIcon = new ImageLabel()
            .setImgPath("ui:" + statRow.statName() + ".png", iconSize, iconSize)
            .setRotationStep(statRots.getOrDefault(statRow.statName(), 0f))
            .setPosition(UDim2.fromOffset(STAT_INNER_PADDING, STAT_INNER_PADDING))
            .setSize(UDim2.fromOffset(STATS_DISPLAY_SIZE, STATS_DISPLAY_SIZE))
            .setBackgroundColor(BLANK)
            .setBorderColor(BLANK)
            .setParent(statRoot);
        TextLabel displayName = new TextLabel()
            .setText(Component.literal(statRow.displayName()))
            .setTextColor(COLOR_TEXT_MUTED)
            .setPosition(UDim2.fromOffset(STATS_TEXT_X, STAT_INNER_PADDING+2))
            .setAutomaticSize(true)
            .setBackgroundColor(BLANK)
            .setBorderColor(BLANK)
            .setParent(statRoot);
        TextLabel value = new TextLabel()
            .setText(Component.literal("NULL"))
            .setPosition(new UDim2(0, STATS_TEXT_X, 1, -STAT_INNER_PADDING-2))
            .setAnchorPoint(new Vector2(0, 1))
            .setAutomaticSize(true)
            .setBackgroundColor(BLANK)
            .setBorderColor(BLANK)
            .setParent(statRoot);
        TextLabel lbLbl = new TextLabel();
        if (statRow.hasLB()) {
            lbLbl.setText(Component.literal(" #?"))
                .setPosition(UDim2.fromScale(1))
                .setAutomaticSize(true)
                .setBackgroundColor(BLANK)
                .setBorderColor(BLANK)
                .setParent(value);
        }
        StatLabel statLabel = new StatLabel(statRoot, displayName, lbLbl, value);
        statLabels.put(statRow.statName(), statLabel);
        return statLabel;
    }

    public static void loadPlayerStats(String username, Screen prevScreen) {
        returnScreen = prevScreen;
        Minecraft mc = Minecraft.getInstance();
        Component userComp = Component.literal(username).withStyle(ChatFormatting.WHITE);
        mc.player.displayClientMessage(fetching.copy().append(userComp), true);
        runThread(() -> {
            String uuid = MojangService.getUUIDfromName(username.toLowerCase());
            if (uuid == null) return;
            Map<String, Object> threadData = new ConcurrentHashMap<>();
            List<Thread> threads = new ArrayList<>();
            // SKIN
            threads.add(runThread(() -> threadData.put("skin", MojangService.getSkin(uuid))));
            // CDU
            threads.add(runThread(() -> {
                Map<String, Object> stats = CDUService.preloadPlayerData(uuid, username);
                if (stats == null) return;
                threadData.put("stats", stats);
                List<Thread> awardThreads = new ArrayList<>();
                @SuppressWarnings("unchecked")
                List<Map<String, String>> awards = (List<Map<String, String>>)stats.get("player_awards");
                for (Map<String, String> award : awards) {
                   String imgUrl = award.get("img_url");
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
            @SuppressWarnings("unchecked")
            Map<String, Object> playerStats = (Map<String, Object>) threadData.get("stats");
            String skinURL = (String) threadData.get("skin");
            if (threadData.get("stats") == null || skinURL == null) return;
            updateUI(playerStats, (String)playerStats.get("username"), MojangService.UUIDFromString(uuid), skinURL);
            mc.execute(() -> mc.setScreen(screen));
        });
    };

    public static Thread createThread(Runnable callback) {
        Thread t = new Thread(callback);
        t.setUncaughtExceptionHandler((thread, ex) -> {
            LOGGER.error(ex.getMessage() + "\n" + Arrays.toString(ex.getStackTrace()));
        });
        return t;
    };
    public static Thread runThread(Runnable callback) { Thread t = createThread(callback); t.start(); return t; }

    private static void updateUI(Map<String, Object> playerStats, String username, UUID uuid, String skin_url) {
        updateDateLabel(lastJoin, (String)playerStats.get("lastjoinedtime"));
        updateDateLabel(firstJoin, (String)playerStats.get("firstjoinedtime"));

        usernameLabel.setText(Component.literal(username)).setRainbowText(rainbowUUIDs.contains(uuid.toString()));
        serverBoosterImage.setParent((boolean)playerStats.get("isserverbooster") ? usernameLabel : null);
        lastServer.setText(Component.literal((String)playerStats.get("lastjoinedservername")));
        rankLabel.setText((Component)playerStats.get("prefix"));

        discordId = (long)playerStats.get("discordid");
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
            String statName = statRow.statName();
            StatLabel labels = statLabels.get(statName); String strVal = "Unknown";
            @SuppressWarnings("unchecked")
            Map<String, Long> statData = (Map<String, Long>)playerStats.get(statName);
            Object value = statData.get("value");
            Long lRank = statData.get("rank");
            int rank = lRank != null ? lRank.intValue() : -999;
            if (statRow.hasLB()) {
                TextLabel lbLabel = labels.lbLbl();
                if (rank == -999) lbLabel.setParent(null);
                else lbLabel.setParent(labels.value())
                    .setText(Component.literal(" #" + rank))
                    .setRainbowText(rank == 1)
                    .setTextColor(lbColors.get(rank) != null ? lbColors.get(rank) : LBSPOT_COLOR);
            }
            strVal = value instanceof String ? (String) value :
                statName.equals("playtime") ? Math.ceil((long)value/3600f*10f)/10f + "h" : formatNumber((long)value);
            labels.value().setText(Component.literal(strVal));
        }

        // awards
        int awardY = STATS_BOTTOM + AWARD_GAP + 22;
        int curX = PADDING;
        int lastRow = 0;
        int curAward = 0;
        List<String> nonDuplicateAwards = new ArrayList<>();
        @SuppressWarnings("unchecked")
        List<Map<String, String>> awards = (List<Map<String, String>>)playerStats.get("player_awards");
        for (Map<String, String> award : awards) {
            String awardName = award.get("award_name");
            if (awardName.equals("None") || nonDuplicateAwards.contains(awardName)) continue;
            nonDuplicateAwards.add(awardName);
            ImageLabel awardDisplay = awardLabels.get(awardName);
            if (awardDisplay == null) {
                List<Component> lines = List.of(
                    Component.literal(award.get("award_name")),
                    Component.literal(award.get("award_description"))
                );
                String imgUrl = award.get("img_url");
                awardDisplay = new ImageLabel()
                    .setImgURL(imgUrl)
                    .setTCache("awards")
                    .setBackgroundColor(AWARD_COLOR)
                    .setSize(UDim2.fromOffset(AWARD_SIZE, AWARD_SIZE));
                awardLabels.put(awardName, awardDisplay);
                awardDisplay.MouseHovered.onEvent(renderParams -> {
                    renderParams.gg().renderComponentTooltip(screen.getFont(), lines, renderParams.x(), renderParams.y());
                });
            }
            int curRow = (int)Math.floor(curAward / AWARDS_PER_ROW);
            if (curRow != lastRow) { curX = PADDING; lastRow = curRow; }
            if (curAward - curRow * AWARDS_PER_ROW == 5) curX += 4;
            awardDisplay.setPosition(UDim2.fromOffset(curX, awardY + curRow * AWARD_GAPSIZE))
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
        root.setSize(UDim2.fromOffset(ROOT_WIDTH, computedHeight));
        screen.recalculate();
    }

    public static void close() {
        Minecraft mc = Minecraft.getInstance();
        mc.setScreen(returnScreen instanceof ChatScreen ? null : returnScreen);
    };

    private static boolean init;
    public static void updateLeaderboards(List<Thread> threads) {
        for (StatRow statRow : statRows) { String statCategory = statRow.statName();
            threads.add(runThread(() -> CDUService.preloadLeaderboard(statCategory, true)));
        }
        if (init) return; init = true;
        runThread(() -> {
            CDUService.sleep(5000);
            checkLeaderboards(threads);
            CDUService.preloadPrefixes();
        });
    }

    public static void checkLeaderboards(List<Thread> threads) {
        for (StatRow statRow : statRows) { String statCategory = statRow.statName();
            Map<String, Map<String, Long>> leaderboard = CDUService.getLeaderboard(statCategory);
            if (leaderboard == null || leaderboard.size() <= 0) {
                threads.add(runThread(() -> CDUService.preloadLeaderboard(statCategory, true)));
            }
        }
    }

    public static void updateDateLabel(TextLabel label, String ISO8601) {
        if (ISO8601.equals("Unknown")) { label.setText(Component.literal(ISO8601)); return; }
        ZonedDateTime localTime = Instant.parse(ISO8601).atZone(timezone);
        String suffix = suffixs.getOrDefault(localTime.getDayOfMonth(), "th");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM d", Locale.ENGLISH);
        label.setText(Component.literal(formatter.format(localTime) + suffix + " " + localTime.getYear()));
    }

    private static String formatNumber(long value) { return String.format(Locale.US, "%,d", value);};

    private static PlayerStatsUI classObj = new PlayerStatsUI();
}