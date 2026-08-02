package com.example.cduprofiledisplay;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Locale;

public class PlayerStatsScreen extends Screen {

    // ---- layout constants ----
    private static final int PANEL_WIDTH = 360;
    private static final int PANEL_MIN_HEIGHT = 260;
    private static final int PADDING = 12;
    private static final int HEAD_SIZE = 64;
    private static final int ROW_HEIGHT = 14;
    private static final int BADGE_SIZE = 24;
    private static final int BADGE_GAP = 6;

    private static final int COLOR_PANEL_BG = 0xE6101014;
    private static final int COLOR_PANEL_BORDER = 0xFF3C3C46;
    private static final int COLOR_TEXT_MUTED = 0xFFAFAFAF;
    private static final int COLOR_TEXT = 0xFFFFFFFF;
    private static final int COLOR_DIVIDER = 0xFF2A2A30;

    private final PlayerStatsData data;
    @Nullable
    private final Screen parent;

    private int panelX;
    private int panelY;
    private int panelHeight;
    private List<Integer> discordBounds;

    private Component usernameComponent;
    private Component discordStatus;
    private boolean linked;

    private final Component DiscordCopyToolTip = Component.literal("Copied Discord User ID!")
        .withStyle(ChatFormatting.GREEN);

    private int hoveredBadgeIndex = -1;

    public PlayerStatsScreen(@Nullable Screen parent, PlayerStatsData data) {
        super(Component.literal(data.username()));
        this.parent = parent;
        this.data = data;
    };

    @Override
    protected void init() {
        super.init();

        this.usernameComponent = Component.literal(data.username())
            .withStyle(style -> style
                .withColor(ChatFormatting.AQUA));
        this.linked = data.discordLinked();
        this.discordStatus = Component.literal(linked ? "Discord Linked" : "Discord Not Linked")
            .withStyle(style -> style.withUnderlined(linked));

        int rows = countStatRows();
        int perRow = badgesPerRow();
        int awardRows = (int) Math.ceil(data.achievements().size() / (double) perRow);
        int computedHeight = PADDING * 3 + 16
            + Math.max(HEAD_SIZE + 24, rows * ROW_HEIGHT) + 20
            + awardRows * (BADGE_SIZE + BADGE_GAP)
            + PADDING;
        panelHeight = Math.max(PANEL_MIN_HEIGHT, computedHeight);

        panelX = (this.width - PANEL_WIDTH) / 2;
        panelY = (this.height - panelHeight) / 2;

        int closeSize = 20;
        this.addRenderableWidget(
            new OutlineButton(panelX + PANEL_WIDTH - closeSize - 6, panelY + 6, closeSize, closeSize,
                Component.literal("X"), b -> onClose()
            )
        );
    };

    @Override
    public void onClose() {
        if (parent instanceof net.minecraft.client.gui.screens.ChatScreen) {
            this.minecraft.setScreen(null);
        } else {
            this.minecraft.setScreen(parent);
        };
    };

    @Override
    public boolean isPauseScreen() {return false;};

    public boolean insideBounds(double mouseX, double mouseY, List<Integer> bounds) {
        return mouseX >= bounds.get(0) && mouseX < bounds.get(2) && mouseY >= bounds.get(1) && mouseY < bounds.get(3);
    };

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0) return super.mouseClicked(mouseX, mouseY, button);
        if (data.discordLinked() && this.insideBounds(mouseX, mouseY, this.discordBounds)) {
            Minecraft.getInstance().keyboardHandler.setClipboard(String.valueOf(data.discordId()));
            Minecraft.getInstance().player.displayClientMessage(DiscordCopyToolTip, true);
            return true;
        };
        return super.mouseClicked(mouseX, mouseY, button);
    };

    @Override
    public void render(GuiGraphics gg, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(gg);

        gg.fill(panelX, panelY, panelX + PANEL_WIDTH, panelY + panelHeight, COLOR_PANEL_BG);
        gg.renderOutline(panelX, panelY, PANEL_WIDTH, panelHeight, COLOR_PANEL_BORDER);

        int cursorY = panelY + PADDING;

        String lastSeenLabel = "Last seen";
        String lastSeenValue = data.lastJoinedServerName();
        gg.drawString(this.font, lastSeenLabel, panelX + PADDING, cursorY, COLOR_TEXT_MUTED, false);
        int valueWidth = this.font.width(lastSeenValue);
        gg.drawString(this.font, lastSeenValue,
                panelX + PANEL_WIDTH - PADDING - valueWidth - 24, cursorY, COLOR_TEXT, false);
        cursorY += 16;

        gg.hLine(panelX + PADDING, panelX + PANEL_WIDTH - PADDING, cursorY, COLOR_DIVIDER);
        cursorY += 8;

        int blockTop = cursorY;

        int avatarX = panelX + PADDING;
        int avatarY = blockTop;
        renderPlayerHead(gg, avatarX, avatarY, HEAD_SIZE);

        int centerX = avatarX + HEAD_SIZE / 2;
        int nameY = avatarY + HEAD_SIZE + 4;
        gg.drawCenteredString(this.font, usernameComponent, centerX, nameY, COLOR_TEXT);

        int discordColor = this.linked ? 0xFF57F287 : COLOR_TEXT_MUTED;
        gg.drawCenteredString(this.font, discordStatus, centerX, nameY + 11, discordColor);

        int discordWidth = this.font.width(discordStatus);
        this.discordBounds = List.of(
            centerX - discordWidth / 2,
            nameY + 11,
            centerX + discordWidth / 2,
            nameY + 11 + this.font.lineHeight
        );
        
        int statsX = avatarX + HEAD_SIZE + PADDING * 2;
        int statsY = blockTop;
        int statsWidth = panelX + PANEL_WIDTH - PADDING - statsX;
        renderStatRows(gg, statsX, statsY, statsWidth);

        int rows = countStatRows();
        int blockBottom = blockTop + Math.max(HEAD_SIZE + 24, rows * ROW_HEIGHT);

        int awardsY = blockBottom + 10;
        gg.drawString(this.font, Component.literal("Awards"), panelX + PADDING, awardsY, COLOR_TEXT, false);
        gg.hLine(panelX + PADDING, panelX + PANEL_WIDTH - PADDING, awardsY + 12, COLOR_DIVIDER);

        renderAwards(gg, panelX + PADDING, awardsY + 18, mouseX, mouseY);

        super.render(gg, mouseX, mouseY, partialTick);

        if (hoveredBadgeIndex >= 0 && hoveredBadgeIndex < data.achievements().size()) {
            PlayerStatsData.Achievement ach = data.achievements().get(hoveredBadgeIndex);
            List<Component> tooltip = List.of(
                    Component.literal(ach.title()),
                    Component.literal(ach.description()).withStyle(s -> s.withColor(0xAAAAAA))
            );
            gg.renderComponentTooltip(this.font, tooltip, mouseX, mouseY);
        }

        if (data.discordLinked() && this.insideBounds(mouseX, mouseY, discordBounds)) {
            gg.renderTooltip(this.font, Component.literal(String.valueOf(data.discordId())), mouseX, mouseY);
        };
    };
    
    private record StatRow(String label, String value) {};

    private List<StatRow> buildStatRows() {
        return List.of(
                new StatRow("Playtime", data.playtime()),
                new StatRow("Unique Servers", formatNumber(data.uniqueServers())),
                new StatRow("Blocks Broken", formatNumber(data.blocksBroken())),
                new StatRow("Deaths", formatNumber(data.deaths())),
                new StatRow("Jumps", formatNumber(data.jumps())),
                new StatRow("Kills (Mobs)", formatNumber(data.mobsKilled())),
                new StatRow("Kills (Players)", formatNumber(data.playersKilled())),
                new StatRow("Votes", formatNumber(data.votes())),
                new StatRow("Distance (Total)", formatNumber(data.distanceTotal())),
                new StatRow("Climbed", formatNumber(data.distanceClimbed())),
                new StatRow("Crouched", formatNumber(data.distanceCrouched())),
                new StatRow("Fallen", formatNumber(data.distanceFallen())),
                new StatRow("Flown", formatNumber(data.distanceFlown())),
                new StatRow("Sprinted", formatNumber(data.distanceSprinted())),
                new StatRow("Swum", formatNumber(data.distanceSwum())),
                new StatRow("Walked", formatNumber(data.distanceWalked()))
        );
    };

    private int countStatRows() {
        return buildStatRows().size();
    };

    private void renderStatRows(GuiGraphics gg, int x, int y, int width) {
        int row = 0;
        for (StatRow stat : buildStatRows()) {
            int rowY = y + row * ROW_HEIGHT;
            gg.drawString(this.font, stat.label(), x, rowY, COLOR_TEXT_MUTED, false);
            int valW = this.font.width(stat.value());
            gg.drawString(this.font, stat.value(), x + width - valW, rowY, COLOR_TEXT, false);
            row++;
        };
    };

    private static String formatNumber(long value) {
        return String.format(Locale.US, "%,d", value);
    };

    private void renderPlayerHead(GuiGraphics gg, int x, int y, int size) {
        ResourceLocation skin = resolveSkinTexture();
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        gg.blit(skin, x, y, size, size, 8f, 8f, 8, 8, 64, 64);   // base face
        gg.blit(skin, x, y, size, size, 40f, 8f, 8, 8, 64, 64);  // hat overlay
    };

    private ResourceLocation resolveSkinTexture() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.getConnection() != null) {
            PlayerInfo info = mc.getConnection().getPlayerInfo(data.uuid());
            if (info != null) return info.getSkinLocation();
        };
        ResourceLocation cached = RemotePlayerSkins.getOrRequest(data.uuid(), data.username());
        if (cached != null) return cached;

        return DefaultPlayerSkin.getDefaultSkin(data.uuid());
    };

    //private ResourceLocation resolveSkinTexture() {
    //    Minecraft mc = Minecraft.getInstance();
    //    if (mc.getConnection() != null) {
    //        PlayerInfo info = mc.getConnection().getPlayerInfo(data.uuid());
    //        if (info != null) return info.getSkinLocation();
    //    };
    //    if (!skinDir.exists()) skinDir.mkdirs();
    //    File skinFile = new File(skinDir, data.uuid().toString() + ".png");
    //    ResourceLocation location = ResourceLocation.fromNamespaceAndPath("config", "cdu_skins/" + data.uuid());
    //    if (skinFile.exists()) {
    //        return location;
    //    };
    //    try {
    //        GameProfile profile = new GameProfile(data.uuid(), data.username());
    //        profile = mc.getMinecraftSessionService().fillProfileProperties(profile, false);
    //        Map<MinecraftProfileTexture.Type, MinecraftProfileTexture> textures = mc.getMinecraftSessionService().getTextures(profile, false);
    //        MinecraftProfileTexture skin = textures.get(MinecraftProfileTexture.Type.SKIN);
    //        if (skin != null) return mc.getSkinManager().registerTexture(skin, MinecraftProfileTexture.Type.SKIN);
    //    } catch (Exception ignored) {};
    //    
    //    return DefaultPlayerSkin.getDefaultSkin(data.uuid());
    //};

    private int badgesPerRow() {
        int usableWidth = PANEL_WIDTH - PADDING * 2;
        return Math.max(1, usableWidth / (BADGE_SIZE + BADGE_GAP));
    };

    private void renderAwards(GuiGraphics gg, int startX, int startY, int mouseX, int mouseY) {
        hoveredBadgeIndex = -1;
        int perRow = badgesPerRow();
        List<PlayerStatsData.Achievement> list = data.achievements();

        for (int i = 0; i < list.size(); i++) {
            int col = i % perRow;
            int row = i / perRow;
            int x = startX + col * (BADGE_SIZE + BADGE_GAP);
            int y = startY + row * (BADGE_SIZE + BADGE_GAP);

            PlayerStatsData.Achievement ach = list.get(i);

            gg.fill(x, y, x + BADGE_SIZE, y + BADGE_SIZE, 0xFF2E2E38);
            gg.renderOutline(x, y, BADGE_SIZE, BADGE_SIZE, COLOR_PANEL_BORDER);

            renderAchievementIcon(gg, ach, x + 2, y + 2, BADGE_SIZE - 4);

            if (mouseX >= x && mouseX < x + BADGE_SIZE && mouseY >= y && mouseY < y + BADGE_SIZE) {
                hoveredBadgeIndex = i;
            };
        };
    };

    private void renderAchievementIcon(GuiGraphics gg, PlayerStatsData.Achievement ach, int x, int y, int size) {
        RemoteBadgeTextures.LoadedTexture tex = RemoteBadgeTextures.getOrRequest(ach.imgUrl());
        if (tex != null) {
            RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
            gg.blit(tex.location(), x, y, size, size, 0, 0, tex.width(), tex.height(), tex.width(), tex.height());
        } else {
            // loading/failed fallback — small vanilla item so the grid isn't empty
            gg.renderItem(new ItemStack(Items.PAPER), x - 4, y - 4);
        };
    };
};
