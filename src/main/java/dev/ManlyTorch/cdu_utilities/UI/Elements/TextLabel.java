package dev.ManlyTorch.cdu_utilities.UI.Elements;

import dev.ManlyTorch.cdu_utilities.UI.Enums.*;
import dev.ManlyTorch.cdu_utilities.UI.Types.*;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;

public class TextLabel extends Frame {
    public int textColor = 0xffffffff;
    public Component text = Component.literal("TextLabel");
    public TextAlignment textXAlignment = TextAlignment.CENTER;
    public TextAlignment textYAlignment = TextAlignment.CENTER;
    public boolean textShadow = false;
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
        gg.drawString(font, text, x, y, textColor, textShadow);
    };

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
