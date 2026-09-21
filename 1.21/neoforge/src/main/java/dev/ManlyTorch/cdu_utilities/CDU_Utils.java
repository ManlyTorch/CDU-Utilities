package dev.ManlyTorch.cdu_utilities;

import com.mojang.logging.LogUtils;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import org.slf4j.Logger;

@Mod("cduutils")
public class CDU_Utils {
    private static final Logger LOGGER = LogUtils.getLogger();

    public CDU_Utils(IEventBus modBus) {
        LOGGER.info("Loaded", "cduutils");
        if (FMLEnvironment.dist != Dist.CLIENT) return;
        NeoForge.EVENT_BUS.addListener(StatsCommand::register);
        modBus.addListener((RegisterKeyMappingsEvent e) -> { e.register(AutoSell.SELL_KEY); e.register(AutoSell.BULK_SELL_KEY); });
        NeoForge.EVENT_BUS.addListener((ClientTickEvent.Post e) -> AutoSell.tick());
    }
}