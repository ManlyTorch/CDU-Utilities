package dev.ManlyTorch.cdu_utilities.UI.Elements;

import dev.ManlyTorch.cdu_utilities.UI.Enums.*;
import dev.ManlyTorch.cdu_utilities.UI.Types.*;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;

import java.awt.Color;

public class TextLabel extends Frame {
    public int textColor = 0xffffffff;
    public Component text = Component.literal("TextLabel");
    public TextAlignment textXAlignment = TextAlignment.CENTER;
    public TextAlignment textYAlignment = TextAlignment.CENTER;
    public boolean textShadow = false;
    public boolean rainbowText = false;
    public float speed = 0.4f;
    public float depth = 0.08f;
    public Vector2 textPadding = new Vector2();
    
    @Override
    public void render(GuiGraphics gg, int mouseX, int mouseY, float partialTick) {
        renderBackground(gg, mouseX, mouseY, partialTick, backgroundColor);
        renderText(gg, mouseX, mouseY, partialTick);
        if (isHovering(mouseX, mouseY)) MouseHovered.fire(new RenderParams(gg, mouseX, mouseY));
        renderChildren(gg, mouseX, mouseY, partialTick);
    }
    public void renderText(GuiGraphics gg, int mouseX, int mouseY, float partialTick) {
        Font font = screen.getFont();
        if (font == null) return;
        int width = font.width(text); int height = font.lineHeight;
        int x = (int)absolutePosition.x; int y = (int)absolutePosition.y;
        if (textXAlignment.equals(TextAlignment.CENTER)) { x += absoluteSize.x/2+textPadding.x-width/2+1; }
        else if (textXAlignment.equals(TextAlignment.RIGHT)) { x += absoluteSize.x-width+textPadding.x; };
        if (textYAlignment.equals(TextAlignment.CENTER)) { y += absoluteSize.y/2+textPadding.y-height/2+1; }
        else if (textYAlignment.equals(TextAlignment.BOTTOM)) { y += absoluteSize.y-height+textPadding.y; };
        if (rainbowText) drawRainbowGradientText(gg, font, text.getString(), x, y, depth, speed);
        else gg.drawString(font, text, x, y, textColor, textShadow);
    };
    private void drawRainbowGradientText(GuiGraphics gg, Font font, String text, int x, int y, float depth, float speed) {
        float time = (System.currentTimeMillis() % 100000L) / 1000f;
        int cursorX = x;
        int charHeight = font.lineHeight;
        int slices = 6;

        for (int i = 0; i < text.length(); i++) {
            String s = String.valueOf(text.charAt(i));
            float baseHue = ((i * depth) + (time * speed)) % 1.0f;
            for (int slice = 0; slice < slices; slice++) {
                float sliceT = slice / (float) (slices - 1);
                float hue = (baseHue + sliceT * 0.08f) % 1.0f;
                int color = 0xFF000000 | Color.HSBtoRGB(hue, 1.0f, 1.0f);

                int sliceYStart = y + (slice * charHeight) / slices;
                int sliceYEnd = y + ((slice + 1) * charHeight) / slices;

                gg.enableScissor(cursorX, sliceYStart, cursorX + font.width(s) + 1, sliceYEnd);
                gg.drawString(font, s, cursorX, y, color, true);
                gg.disableScissor();
            }

            cursorX += font.width(s);
        }
    }

    @Override
    public void updateCalculations() {
        int xs; int ys;
        if (automaticSize && this.screen.getFont() != null) {
            Font font = this.screen.getFont();
            xs = (int)clamp(font.width(text), minSize.x, maxSize.x);
            ys = (int)clamp(font.lineHeight, minSize.y, maxSize.y);
        } else {
            xs = (int)clamp((size.x.offset + size.x.scale * parent.absoluteSize.x), minSize.x, maxSize.x);
            ys = (int)clamp((size.y.offset + size.y.scale * parent.absoluteSize.y), minSize.y, maxSize.y);
        }
        int x = (int)position.x.offset + (int)(position.x.scale * parent.absoluteSize.x + parent.absolutePosition.x) - (int)(anchorPoint.x * xs);
        int y = (int)position.y.offset + (int)(position.y.scale * parent.absoluteSize.y + parent.absolutePosition.y) - (int)(anchorPoint.y * ys);
        absolutePosition.x = x; absolutePosition.y = y;
        absoluteSize .x = xs; absoluteSize.y = ys;
        for (Frame child : children) {
            child.updateCalculations();
        };
    };

    public TextLabel setParent(Frame parent) { super.setParent(parent); return this; }
    public TextLabel setText(Component comp) { this.text = comp; if (automaticSize) this.updateCalculations(); return this; }
    public TextLabel setTextColor(int c) { this.textColor = c; return this; }
    public TextLabel setTextXAlignment(TextAlignment alignment) { this.textXAlignment = alignment; return this; }
    public TextLabel setTextYAlignment(TextAlignment alignment) { this.textYAlignment = alignment; return this; }
    public TextLabel setTextShadow(boolean enabled) { this.textShadow = enabled; return this; }
    public TextLabel setTextPadding(Vector2 v2) { this.textPadding = v2; return this; }
    public TextLabel setAnchorPoint(Vector2 v2) { this.anchorPoint = v2; return this; }
    public TextLabel setMinSize(Vector2 v2) { this.minSize = v2; return this; }
    public TextLabel setMaxSize(Vector2 v2) { this.maxSize = v2; return this; }
    public TextLabel setSize(UDim2 size) { this.size = size; return this; }
    public TextLabel setAutomaticSize(boolean b) { this.automaticSize = b; return this; }
    public TextLabel setPosition(UDim2 pos) { this.position = pos; return this; }
    public TextLabel setBackgroundColor(int c) { this.backgroundColor = c; return this; }
    public TextLabel setBorderColor(int c) { this.borderColor = c; return this; }
    public TextLabel setZIndex(int zIndex) { this.zIndex = zIndex; return this; }
}
