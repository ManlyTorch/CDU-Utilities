package dev.ManlyTorch.cdu_utilities;

import com.mojang.logging.LogUtils;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.slf4j.Logger;

@Mod("cduutils")
public class CDU_Utils {
    private static final Logger LOGGER = LogUtils.getLogger();

    public CDU_Utils() {
        LOGGER.info("Loaded", "cduutils");
        if (FMLEnvironment.dist != Dist.CLIENT) return;
        FMLJavaModLoadingContext.get().getModEventBus()
            .addListener((RegisterKeyMappingsEvent e) -> { e.register(AutoSell.SELL_KEY); e.register(AutoSell.BULK_SELL_KEY); });
        MinecraftForge.EVENT_BUS
            .addListener((TickEvent.ClientTickEvent e) -> { if (e.phase == TickEvent.Phase.END) AutoSell.tick(); });
    }
}