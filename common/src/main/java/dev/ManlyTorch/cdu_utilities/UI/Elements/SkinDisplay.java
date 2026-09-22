package dev.ManlyTorch.cdu_utilities.UI.Elements;

import dev.ManlyTorch.cdu_utilities.UI.*;
import dev.ManlyTorch.cdu_utilities.Lib.*;
import dev.ManlyTorch.cdu_utilities.UI.Types.*;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;

import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;

import java.util.Base64;
import java.util.UUID;

public class SkinDisplay extends Frame {
    public UUID uuid;
    public String skinURL;
    public String username = "Player";
    private RemotePlayer fakePlyr;
    private float xRot = 180f;
    private float yRot = 0f;
    private boolean dragging = false;
    private double lastMouseX;
    private double lastMouseY;
    private static final float PIVOT_HEIGHT = 0.9f;

    private GameProfile buildProfile() {
        GameProfile profile = new GameProfile(uuid, username);
        if (skinURL != null && !skinURL.isEmpty()) {
            String texturesJson = """
                {"textures":{"SKIN":{"url":"%s"}}}
                """.formatted(skinURL);
            String encoded = Base64.getEncoder().encodeToString(texturesJson.getBytes());
            profile.getProperties().put("textures", new Property("textures", encoded));
        }
        return profile;
    }

    public SkinDisplay createFakePlayer() {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) return this;
        fakePlyr = new FakeSkinPlayer(level, buildProfile(), ImageCacher.fetchImage("skins", skinURL).location());
        fakePlyr.setPos(0, -5000, 0);
        return this;
    }

    private void handleDrag(int mouseX, int mouseY) {
        if (!clicked && !rightClicked) return;
        boolean mouseDown = isMBHeld(1) | isMBHeld(0);
        if (mouseDown && isHovering(mouseX, mouseY) && !dragging) {
            dragging = true; lastMouseX = mouseX; lastMouseY = mouseY;
        } else if (!mouseDown) {
            clicked = false; rightClicked = false; dragging = false;
        }
        if (dragging) {
            double dx = mouseX - lastMouseX; double dy = mouseY - lastMouseY;
            float facing = (float) Math.signum(Math.cos(Math.toRadians(yRot)));
            xRot -= (float) dx * facing; yRot += (float) dy;
            lastMouseX = mouseX; lastMouseY = mouseY;
        }
    }

    public boolean isMBHeld(int button) {
        long window = Minecraft.getInstance().getWindow().getWindow();
        return GLFW.glfwGetMouseButton(window, button) == 1;
    }

    @Override
    public void render(GuiGraphics gg, int mouseX, int mouseY, float partialTick) {
        renderBackground(gg, mouseX, mouseY, partialTick, backgroundColor);
        if (isHovering(mouseX, mouseY)) MouseHovered.fire(new RenderParams(gg, mouseX, mouseY));
        handleDrag(mouseX, mouseY);
        renderPlayer(gg);
        renderChildren(gg, mouseX, mouseY, partialTick);
    }

    public void renderPlayer(GuiGraphics gg) {
        if (fakePlyr == null) return;
        float entityHeight = fakePlyr.getBbHeight();
        int x = (int) (absolutePosition.x + absoluteSize.x / 2.0);
        int centerY = (int) (absolutePosition.y + absoluteSize.y / 2.0);
        int scale = (int) (absoluteSize.y / entityHeight / 1.3);
        int y = (int) (centerY + (scale * entityHeight) / 2.0);

        gg.enableScissor(
            (int) absolutePosition.x, (int) absolutePosition.y,
            (int) (absolutePosition.x + absoluteSize.x), (int) (absolutePosition.y + absoluteSize.y)
        );

        Vector3f light0 = new Vector3f(0.0f, 0.0f, 1.0f);
        Vector3f light1 = new Vector3f(0.0f, 0.0f, 1.0f);
        RenderSystem.setShaderLights(light0, light1);
        Quaternionf baseFacing = new Quaternionf().rotateY((float) Math.PI);
        Quaternionf userRotation = new Quaternionf().rotateX((float) Math.toRadians(-yRot)).rotateY((float) Math.toRadians(-xRot));
        gg.pose().pushPose(); gg.pose().translate(x, y, 50.0); gg.pose().scale(scale, -scale, scale);
        gg.pose().translate(0, PIVOT_HEIGHT, 0); gg.pose().mulPose(baseFacing); gg.pose().mulPose(userRotation); gg.pose().translate(0, -PIVOT_HEIGHT, 0);
        EntityRenderDispatcher dispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
        boolean shouldRenderHitboxes = dispatcher.shouldRenderHitBoxes();
        dispatcher.setRenderHitBoxes(false);
        dispatcher.render(fakePlyr, 0.0, 0.0, 0.0, 0.0f, 1.0f, gg.pose(), gg.bufferSource(), LightTexture.FULL_BRIGHT);
        gg.flush(); gg.pose().popPose();
        dispatcher.setRenderHitBoxes(shouldRenderHitboxes);

        gg.disableScissor();
    }

    public SkinDisplay setUUID(UUID uuid) { this.uuid = uuid; return this; }
    public SkinDisplay setSkinURL(String imgURL) { this.skinURL = imgURL; return this; }
    public SkinDisplay setUsername(String username) { this.username = username; return this; }
    public SkinDisplay setParent(Frame parent) { super.setParent(parent); return this; }
    public SkinDisplay setAnchorPoint(Vector2 v2) { this.anchorPoint = v2; return this; }
    public SkinDisplay setMinSize(Vector2 v2) { this.minSize = v2; return this; }
    public SkinDisplay setMaxSize(Vector2 v2) { this.maxSize = v2; return this; }
    public SkinDisplay setSize(UDim2 size) { this.size = size; return this; }
    public SkinDisplay setAutomaticSize(boolean b) { this.automaticSize = b; return this; }
    public SkinDisplay setPosition(UDim2 pos) { this.position = pos; return this; }
    public SkinDisplay setBackgroundColor(int c) { this.backgroundColor = c; return this; }
    public SkinDisplay setBorderColor(int c) { this.borderColor = c; return this; }
    public SkinDisplay setZIndex(int zIndex) { this.zIndex = zIndex; return this; }
}