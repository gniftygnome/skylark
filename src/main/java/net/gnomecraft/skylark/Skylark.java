package net.gnomecraft.skylark;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Skylark implements ModInitializer {
    public static final String modId = "skylark";
    public static final Logger LOGGER = LoggerFactory.getLogger(modId);

    @Override
    public void onInitialize() {
        LOGGER.info("Skylark: activate.");
        LOGGER.info("  For the blackness of the interstellar void was not the dark of an earthly night,");
        LOGGER.info("  but the absolute black of the absence of all light,");
        LOGGER.info("  beside which the black of platinum dust is pale and gray;");
        LOGGER.info("  and laid upon this velvet were the jewel stars.");
        LOGGER.info("    - \"The Skylark of Space\" by E. E. \"Doc\" Smith and Lee Hawkins Garby");
    }
}
