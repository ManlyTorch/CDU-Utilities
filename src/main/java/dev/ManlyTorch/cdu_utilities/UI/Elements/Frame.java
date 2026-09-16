package dev.ManlyTorch.cdu_utilities.UI.Elements;

import dev.ManlyTorch.cdu_utilities.Lib.BindableEvent;
import dev.ManlyTorch.cdu_utilities.UI.Types.*;

import net.minecraft.client.gui.GuiGraphics;

import java.util.List;
import java.util.ArrayList;

public class Frame {
    protected List<Frame> children = new ArrayList<Frame>();
    public List<Frame> renderOrder = new ArrayList<Frame>();
    public Vector2 anchorPoint = new Vector2();
    public Vector2 minSize = new Vector2();
    public Vector2 maxSize = new Vector2(99999999,99999999);
    public UDim2 size = new UDim2();
    public UDim2 position = new UDim2();
    public int backgroundColor = 0x7f141414;
    public int borderColor = 0xff3c3c3c;
    // public int borderPixelSize = 1;
    public int zIndex = 0;
    protected Frame parent;
    public UIScreen screen;
    public boolean clicked = false;
    public boolean automaticSize = false;
    public Vector2 absolutePosition = new Vector2();
    public Vector2 absoluteSize = new Vector2();
    public BindableEvent<RenderParams> MouseHovered = new BindableEvent<>();

    public record RenderParams(GuiGraphics gg, int x, int y) {}

    public Frame getParent() {
        return parent;
    };
    public List<Frame> getChildren() {
        return children;
    };

    public void updateRenderOrder() {
        renderOrder.sort((a, b) -> a.zIndex - b.zIndex);
    };
    
    public void addChild(Frame child) {
        children.add(child);
        child.parent = this;
        child.screen = screen;
        renderOrder.add(child);
        updateRenderOrder();
        child.updateCalculations();
    };
    public void removeChild(Frame child) {
        children.remove(child);
        renderOrder.remove(child);
        child.screen = null;
    };

    public void updateCalculations() {
        int xs = (int)clamp((size.x.offset + size.x.scale * parent.absoluteSize.x), minSize.x, maxSize.x);
        int x = (int)position.x.offset + (int)(position.x.scale * parent.absoluteSize.x + parent.absolutePosition.x) - (int)(anchorPoint.x * xs);
        int ys = (int)clamp((size.y.offset + size.y.scale * parent.absoluteSize.y), minSize.y, maxSize.y);
        int y = (int)position.y.offset + (int)(position.y.scale * parent.absoluteSize.y + parent.absolutePosition.y) - (int)(anchorPoint.y * ys);
        absolutePosition.x = x; absolutePosition.y = y;
        absoluteSize .x = xs; absoluteSize.y = ys;
        for (Frame child : children) {
            child.updateCalculations();
        };
    };
    public void updateRootCalc() {
        int xs = (int)clamp((size.x.offset + size.x.scale * screen.width), minSize.x, maxSize.x);
        int x = (int)position.x.offset + (int)(position.x.scale * screen.width) - (int)(anchorPoint.x * xs);
        int ys = (int)clamp((size.y.offset + size.y.scale * screen.height), minSize.y, maxSize.y);
        int y = (int)position.y.offset + (int)(position.y.scale * screen.height) - (int)(anchorPoint.y * ys);
        absolutePosition.x = x; absolutePosition.y = y;
        absoluteSize.x = xs; absoluteSize.y = ys;
        for (Frame child : children) {
            child.updateCalculations();
        };
    };

    public boolean isHovering(double mouseX, double mouseY) {
        return (mouseX >= absolutePosition.x && mouseX <= absolutePosition.x + absoluteSize.x && mouseY >= absolutePosition.y && mouseY <= absolutePosition.y + absoluteSize.y);
    };

    public int getXSize() {
        if (parent != null) {
            return (int)clamp((size.x.offset + size.x.scale * parent.getXSize()), minSize.x, maxSize.x);
        } else {
            return (int)clamp((size.x.offset + size.x.scale * screen.width), minSize.x, maxSize.x);
        }
    }
    public int getYSize() {
        if (parent != null) {
            return (int)clamp((size.y.offset + size.y.scale * parent.getYSize()), minSize.y, maxSize.y);
        } else {
            return (int)clamp((size.y.offset + size.y.scale * screen.height), minSize.y, maxSize.y);
        }
    }

    public void renderBackground(GuiGraphics gg, int mouseX, int mouseY, float partialTick, int backgroundColor) {
        // int xs = getXSize();
        // int x = (int)position.x.offset + (int)(position.x.scale * screen.width) - (int)(anchorPoint.x * xs);
        // int ys = getYSize();
        // int y = (int)position.y.offset + (int)(position.y.scale * screen.height) - (int)(anchorPoint.y * ys);
        // gg.fill(x, y, x + xs, y + ys, backgroundColor);
        // gg.renderOutline(x, y, xs, ys, borderColor);
        int xCorner = (int)absolutePosition.x + (int)absoluteSize.x;
        int yCorner = (int)absolutePosition.y + (int)absoluteSize.y;
        gg.fill((int)absolutePosition.x, (int)absolutePosition.y, xCorner, yCorner, backgroundColor);
        gg.renderOutline((int)absolutePosition.x, (int)absolutePosition.y, (int)absoluteSize.x, (int)absoluteSize.y, borderColor);
    }

    public void render(GuiGraphics gg, int mouseX, int mouseY, float partialTick) {
        renderBackground(gg, mouseX, mouseY, partialTick, backgroundColor);
        if (isHovering(mouseX, mouseY)) MouseHovered.fire(new RenderParams(gg, mouseX, mouseY));
        renderChildren(gg, mouseX, mouseY, partialTick);
    };

    public void renderChildren(GuiGraphics gg, int mouseX, int mouseY, float partialTick) {
        for (Frame child : children) { child.render(gg, mouseX, mouseY, partialTick); }
    }

    public int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    };
    public double clamp (double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    };

    public Frame setParent(Frame parent) {
        if (this.parent != null) { this.parent.removeChild(this); }
        if (parent != null) { parent.addChild(this); }
        else { this.parent = null; };
        return this;
    };
    public Frame setAnchorPoint(Vector2 v2) { this.anchorPoint = v2; return this; }
    public Frame setMinSize(Vector2 v2) { this.minSize = v2; return this; }
    public Frame setMaxSize(Vector2 v2) { this.maxSize = v2; return this; }
    public Frame setSize(UDim2 size) { this.size = size; return this; }
    public Frame setAutomaticSize(boolean b) { this.automaticSize = b; return this; }
    public Frame setPosition(UDim2 pos) { this.position = pos; return this; }
    public Frame setBackgroundColor(int c) { this.backgroundColor = c; return this; }
    public Frame setBorderColor(int c) { this.borderColor = c; return this; }
    public Frame setZIndex(int zIndex) { this.zIndex = zIndex; return this; }
    // public Frame setBorderPixelSize(int borderPixelSize) { this.borderPixelSize = borderPixelSize; return this; }
};