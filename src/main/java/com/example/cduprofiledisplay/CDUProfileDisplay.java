package com.example.cduprofiledisplay;

import com.mojang.logging.LogUtils;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;

@Mod("cduprofiledisplay")
public class CDUProfileDisplay {
    private static final Logger LOGGER = LogUtils.getLogger();

    public CDUProfileDisplay() {
        LOGGER.info("[{}] Loaded — /stats <username> and clickable-name profile lookups are ready.", "cduprofiledisplay");

        // Nothing else to register here yet. If you later add blocks, items,
        // block entities, etc., that's where a DeferredRegister + the mod
        // event bus (injected via the constructor, or FMLJavaModLoadingContext
        // .get().getModEventBus() on older codebases) would be wired up.
    }
}