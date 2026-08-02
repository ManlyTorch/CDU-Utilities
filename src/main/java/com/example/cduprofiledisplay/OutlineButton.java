package com.example.cduprofiledisplay;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

public class OutlineButton extends Button {

    public OutlineButton(int x, int y, int width, int height, Component text, OnPress onPress) {
        super(x, y, width, height, text, onPress, DEFAULT_NARRATION);
    }

    @Override
    public void renderWidget(GuiGraphics gg, int mouseX, int mouseY, float partialTick) {
        boolean hovered = isHovered();

        int color = hovered ? 0xFFFFFFFF : 0xFF2A2A30;
        gg.renderOutline(
                this.getX(),
                this.getY(),
                this.width,
                this.height,
                color
        );
        gg.drawCenteredString(
                Minecraft.getInstance().font,
                this.getMessage(),
                this.getX() + this.width / 2,
                this.getY() + (this.height - 8) / 2,
                0xFFFFFFFF
        );
    };
};