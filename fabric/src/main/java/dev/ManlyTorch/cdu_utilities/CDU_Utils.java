package dev.ManlyTorch.cdu_utilities;

import dev.ManlyTorch.cdu_utilities.PlayerStatsUI;

import com.mojang.logging.LogUtils;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class CDU_Utils implements ClientModInitializer {
    private static final Logger LOGGER = LogUtils.getLogger();

    @Override
    public void onInitializeClient() {
        LOGGER.info("Loaded", "cduutils");
        PlayerStatsUI.updateLeaderboards(new ArrayList<>());
        StatsCommand.register();
        KeyBindingHelper.registerKeyBinding(AutoSell.SELL_KEY);
        KeyBindingHelper.registerKeyBinding(AutoSell.BULK_SELL_KEY);
        ClientTickEvents.END_CLIENT_TICK.register(mc -> AutoSell.tick());
    }
}