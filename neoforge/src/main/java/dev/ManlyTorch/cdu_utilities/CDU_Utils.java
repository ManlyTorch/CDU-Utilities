package dev.ManlyTorch.cdu_utilities;

import com.mojang.logging.LogUtils;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;

@Mod("cduutils")
public class CDU_Utils {
    private static final Logger LOGGER = LogUtils.getLogger();

    public CDU_Utils() {
        LOGGER.info("Loaded", "cduutils");
        if (FMLEnvironment.dist == Dist.CLIENT) NeoForge.EVENT_BUS.addListener(StatsCommand::register);
    }
}