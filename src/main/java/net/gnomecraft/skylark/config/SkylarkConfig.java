package net.gnomecraft.skylark.config;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.Comment;
import net.gnomecraft.skylark.Skylark;

import java.util.ArrayList;
import java.util.List;

// Configuration file definition.
@SuppressWarnings("unused")
@Config(name = Skylark.modId)
public class SkylarkConfig implements ConfigData {
    @Comment("What do you want as a spawn platform (randomly selected from list)?")
    public ArrayList<String> spawnPlatform = new ArrayList<>(List.of(
            "acacia",
            "azalea_tree",
            "birch",
            "dark_oak",
            "fancy_oak",
            "fancy_oak_bees",
            "jungle_tree",
            "mega_jungle_tree",
            "mega_pine",
            "mega_spruce",
            "oak",
            "pine",
            "spruce",
            "super_birch_bees"
    ));

    @Comment("If true, place new players each in their own team; otherwise in the default team.")
    public boolean separateTeams = false;

    @Comment("Space teams out around the origin at what radius?  (Zero for shared spawn point.)")
    public long spawnRingRadius = 0;

    @Comment("Place the spawn platform at what Y level?")
    public int spawnHeight = 120;

    @Comment("If starterChest has any items listed, a chest will be placed at each spawn point.")
    public ArrayList<String> starterChest = new ArrayList<>(27);
}
