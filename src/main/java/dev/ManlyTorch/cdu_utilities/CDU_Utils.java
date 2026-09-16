package dev.ManlyTorch.cdu_utilities;

import com.mojang.logging.LogUtils;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;

@Mod("cduutils")
public class CDU_Utils {
    private static final Logger LOGGER = LogUtils.getLogger();

    public CDU_Utils() {
        LOGGER.info("Loaded", "cduutils");
    }
}