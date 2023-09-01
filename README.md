![icon](./src/main/resources/assets/skylark/icon.png)

# Skylark
Generation and utility mod for Fabric sky block games.

You do not need to ask me in order to use this mod in a modpack or to use
this code in your own mod.  However, I would love to hear about it so I can
check it out.

See [the wiki](https://github.com/gniftygnome/skylark/wiki) for configuration and details.

## What does Skylark do today?
Currently implemented features:
* Disable all Overworld generation.
* Preserve the distribution of biomes.
* Allow generation of any starting feature (trees, etc.), or
  * At a configurable height, and
  * Randomized from a configured list, which
  * Defaults to (most of) the Minecraft trees
* Allow generation of most structures.
  * There is no ground layer, so
  * Many vanilla structures will have some defects.
  * Some vanilla structures override the height (-63 is common).
  * A few custom structures are available; see the wiki.
* Generate a single chest at the spawn point with configurable items.
* Spawn the player on the generated feature or structure.
* Option to distribute multiple players either:
  * All at (0, 0) on a shared spawn feature, or
  * In a ring of configurable radius centered on (0, 0)
* Track and persist player default spawn location individually.

## What might Skylark do eventually?
Currently planned features:
* Maybe add bonus things specific to finding different biomes?
* Add a mechanism to put players into teams and spawn teams together.

I will generally add any features I find useful for sky block play but which
are not already available via other Fabric mods.  Suggestions are welcome.
