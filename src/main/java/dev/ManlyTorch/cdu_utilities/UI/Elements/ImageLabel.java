package dev.ManlyTorch.cdu_utilities.UI.Elements;

import dev.ManlyTorch.cdu_utilities.Lib.*;
import dev.ManlyTorch.cdu_utilities.UI.Types.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;

import java.util.List;
import java.util.ArrayList;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

public class ImageLabel extends Frame {
    public String imgURL;
    public String tCache;
    public ImageCacher.LoadedTexture backupImg;
    public List<BlitOptions> blits = new ArrayList<>();

    public record BlitOptions(float u, float v, int uW, int uH, int tW, int tH) {}

    @Override
    public void render(GuiGraphics gg, int mouseX, int mouseY, float partialTick) {
        renderBackground(gg, mouseX, mouseY, partialTick, backgroundColor);
        renderImage(gg, mouseX, mouseY, partialTick);
        if (isHovering(mouseX, mouseY)) MouseHovered.fire(new RenderParams(gg, mouseX, mouseY));
        renderChildren(gg, mouseX, mouseY, partialTick);
    }

    public void renderImage(GuiGraphics gg, int mouseX, int mouseY, float partialTick) {
        if (imgURL == null || tCache == null) return;
        ImageCacher.LoadedTexture texture = ImageCacher.fetchImage(tCache, imgURL);
        if (texture == null) {
            if (backupImg == null) return;
            texture = backupImg;
        };
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        int width = texture.width(); int height = texture.height();
        int xPos = (int)absolutePosition.x; int yPos = (int)absolutePosition.y;
        int xSize = (int)absoluteSize.x; int ySize = (int)absoluteSize.y;
        if (blits.size() == 0) { gg.blit(texture.location(), xPos, yPos, xSize, ySize, 0, 0, width, height, width, height); return; }
        for (BlitOptions blitOptions : blits) {
            gg.blit(texture.location(),
            xPos, yPos,
            xSize, ySize,
            blitOptions.u(), blitOptions.v(),
            blitOptions.uW(), blitOptions.uH(),
            blitOptions.tW(), blitOptions.tH());
        }
    }

    public ImageLabel addBlitOption(BlitOptions blitOption) { blits.add(blitOption); return this; }
    public ImageLabel setParent(Frame parent) { super.setParent(parent); return this; }
    public ImageLabel setTCache(String tCache) { this.tCache = tCache; return this; }
    public ImageLabel setImgURL(String imgURL) { this.imgURL = imgURL; return this; }
    public ImageLabel setAnchorPoint(Vector2 v2) { this.anchorPoint = v2; return this; }
    public ImageLabel setMinSize(Vector2 v2) { this.minSize = v2; return this; }
    public ImageLabel setMaxSize(Vector2 v2) { this.maxSize = v2; return this; }
    public ImageLabel setSize(UDim2 size) { this.size = size; return this; }
    public ImageLabel setAutomaticSize(boolean b) { this.automaticSize = b; return this; }
    public ImageLabel setPosition(UDim2 pos) { this.position = pos; return this; }
    public ImageLabel setBackgroundColor(int c) { this.backgroundColor = c; return this; }
    public ImageLabel setBorderColor(int c) { this.borderColor = c; return this; }
    public ImageLabel setZIndex(int zIndex) { this.zIndex = zIndex; return this; }
}