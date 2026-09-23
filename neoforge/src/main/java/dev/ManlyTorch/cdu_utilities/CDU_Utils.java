package dev.ManlyTorch.cdu_utilities;

import dev.ManlyTorch.cdu_utilities.PlayerStatsUI;

import com.mojang.logging.LogUtils;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.TickEvent;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;

@Mod("cduutils")
public class CDU_Utils {
    private static final Logger LOGGER = LogUtils.getLogger();

    public CDU_Utils(IEventBus modBus) {
        LOGGER.info("Loaded", "cduutils");
        PlayerStatsUI.updateLeaderboards(new ArrayList<>());
        if (FMLEnvironment.dist != Dist.CLIENT) return;
        NeoForge.EVENT_BUS.addListener(StatsCommand::register);
        modBus.addListener((RegisterKeyMappingsEvent e) -> { e.register(AutoSell.SELL_KEY); e.register(AutoSell.BULK_SELL_KEY); });
        NeoForge.EVENT_BUS.addListener((TickEvent.ClientTickEvent e) -> { if (e.phase == TickEvent.Phase.END) AutoSell.tick(); });
    }
}