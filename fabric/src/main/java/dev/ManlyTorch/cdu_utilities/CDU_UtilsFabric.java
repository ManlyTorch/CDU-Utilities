package dev.ManlyTorch.cdu_utilities;

import com.mojang.logging.LogUtils;
import net.fabricmc.api.ClientModInitializer;
import org.slf4j.Logger;

public class CDU_UtilsFabric implements ClientModInitializer {
    private static final Logger LOGGER = LogUtils.getLogger();

    @Override
    public void onInitializeClient() {
        LOGGER.info("Loaded", "cduutils");
        StatsCommand.register();
    }
}