package dev.ManlyTorch.cdu_utilities.UI.Elements;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.Font;

import net.minecraft.network.chat.Component;

public class UIScreen extends Screen {
    private Frame rootFrame;
    public boolean pauseScreen;

    public UIScreen(String name) { super(Component.literal(name)); }

    @Override
    public boolean isPauseScreen() {return pauseScreen; }
    @Override
    protected void init() { super.init(); recalculate(); }

    public void setRootFrame(Frame rootFrame) {
        this.rootFrame = rootFrame;
        rootFrame.screen = this;
    };

    public Font getFont() { return font; }
    
    @Override
    public void render(GuiGraphics gg, int mouseX, int mouseY, float partialTick) {
        gg.fillGradient(0, 0, this.width, this.height, 0xc0101010, 0xd0101010);
        rootFrame.render(gg, mouseX, mouseY, partialTick);
    };
    @Override
    public void resize(Minecraft mc, int width, int height) {
        super.resize(mc, width, height);
        recalculate();
    };
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && checkClicked(rootFrame, mouseX, mouseY)) return true;
        else if (button == 1 && checkRightClicked(rootFrame, mouseX, mouseY)) return true;
        return super.mouseClicked(mouseX, mouseY, button);
    };

    private boolean checkRightClicked(Frame frame, double mouseX, double mouseY) {
        for (int i = frame.renderOrder.size() - 1; i >= 0; i--) {
            if (checkRightClicked(frame.renderOrder.get(i), mouseX, mouseY)) return true;
        };
        if (frame.isHovering(mouseX, mouseY)) { frame.rightClicked = true; return true; }
        return false;
    };

    private boolean checkClicked(Frame frame, double mouseX, double mouseY) {
        for (int i = frame.renderOrder.size() - 1; i >= 0; i--) {
            if (checkClicked(frame.renderOrder.get(i), mouseX, mouseY)) return true;
        };
        if (frame.isHovering(mouseX, mouseY)) { frame.clicked = true; return true; }
        return false;
    };

    public void recalculate() {
        if (rootFrame == null) return;
        rootFrame.updateRootCalc();
    };
};