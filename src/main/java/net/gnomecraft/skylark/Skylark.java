package net.gnomecraft.skylark;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.Toml4jConfigSerializer;
import net.fabricmc.api.ModInitializer;
import net.gnomecraft.skylark.config.SkylarkConfig;
import net.gnomecraft.skylark.config.SkylarkState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Skylark implements ModInitializer {
    public static final String modId = "skylark";
    public static final Logger LOGGER = LoggerFactory.getLogger(modId);
    public static final SkylarkState STATE = new SkylarkState();

    @Override
    public void onInitialize() {
        // Register the Ductwork config
        AutoConfig.register(SkylarkConfig.class, Toml4jConfigSerializer::new);

        LOGGER.info("Skylark: activate.");
        LOGGER.info("  For the blackness of the interstellar void was not the dark of an earthly night,");
        LOGGER.info("  but the absolute black of the absence of all light,");
        LOGGER.info("  beside which the black of platinum dust is pale and gray;");
        LOGGER.info("  and laid upon this velvet were the jewel stars.");
        LOGGER.info("    - \"The Skylark of Space\" by E. E. \"Doc\" Smith and Lee Hawkins Garby");
    }

    public static SkylarkConfig getConfig() {
        return AutoConfig.getConfigHolder(SkylarkConfig.class).getConfig();
    }
}
