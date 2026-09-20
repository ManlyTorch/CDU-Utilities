package dev.ManlyTorch.cdu_utilities.UI.Elements;

import dev.ManlyTorch.cdu_utilities.Lib.BindableEvent;
import dev.ManlyTorch.cdu_utilities.UI.Enums.TextAlignment;
import dev.ManlyTorch.cdu_utilities.UI.Types.UDim2;
import dev.ManlyTorch.cdu_utilities.UI.Types.Vector2;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

public class TextButton extends TextLabel {
    public int hoverColor = 0x7f2f2f2f;
    public BindableEvent<RenderParams> MouseButton1Clicked = new BindableEvent<>();

    public TextButton() {
        text = Component.literal("TextButton");
    }

    @Override
    public void render(GuiGraphics gg, int mouseX, int mouseY, float partialTick) {
        boolean isHovered = isHovering(mouseX, mouseY);
        renderBackground(gg, mouseX, mouseY, partialTick, isHovered ? backgroundColor : hoverColor);
        this.renderText(gg, mouseX, mouseY, partialTick);
        if (this.clicked) { this.clicked = false; MouseButton1Clicked.fire(new RenderParams(gg, mouseX, mouseY)); }
        if (isHovered) MouseHovered.fire(new RenderParams(gg, mouseX, mouseY));
        renderChildren(gg, mouseX, mouseY, partialTick);
    };

    public TextButton setParent(Frame parent) { super.setParent(parent); return this; }
    public TextButton setHoverColor(int c) { this.hoverColor = c; return this; }
    public TextButton setText(Component comp) { this.text = comp; if (automaticSize) this.updateCalculations(); return this; }
    public TextButton setTextColor(int c) { this.textColor = c; return this; }
    public TextButton setTextXAlignment(TextAlignment alignment) { this.textXAlignment = alignment; return this; }
    public TextButton setTextYAlignment(TextAlignment alignment) { this.textYAlignment = alignment; return this; }
    public TextButton setTextShadow(boolean enabled) { this.textShadow = enabled; return this; }
    public TextButton setTextPadding(Vector2 v2) { this.textPadding = v2; return this; }
    public TextButton setAnchorPoint(Vector2 v2) { this.anchorPoint = v2; return this; }
    public TextButton setMinSize(Vector2 v2) { this.minSize = v2; return this; }
    public TextButton setMaxSize(Vector2 v2) { this.maxSize = v2; return this; }
    public TextButton setSize(UDim2 size) { this.size = size; return this; }
    public TextButton setAutomaticSize(boolean b) { this.automaticSize = b; return this; }
    public TextButton setPosition(UDim2 pos) { this.position = pos; return this; }
    public TextButton setBackgroundColor(int c) { this.backgroundColor = c; return this; }
    public TextButton setBorderColor(int c) { this.borderColor = c; return this; }
    public TextButton setZIndex(int zIndex) { this.zIndex = zIndex; return this; }
};