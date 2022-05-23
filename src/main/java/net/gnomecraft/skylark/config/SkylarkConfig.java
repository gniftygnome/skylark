package net.gnomecraft.skylark.config;

import blue.endless.jankson.Comment;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import net.gnomecraft.skylark.Skylark;
import net.minecraft.util.Identifier;

// Configuration file definition.
@SuppressWarnings("unused")
@Config(name = Skylark.modId)
public class SkylarkConfig implements ConfigData {
    @Comment("What do you want as a spawn platform?")
    public Identifier spawnPlatform = new Identifier("minecraft:mega_spruce");
}
