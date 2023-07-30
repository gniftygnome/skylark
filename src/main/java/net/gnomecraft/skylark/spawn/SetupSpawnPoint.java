package net.gnomecraft.skylark.spawn;

import net.gnomecraft.skylark.Skylark;
import net.minecraft.block.Block;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.*;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.TreeConfiguredFeatures;
import net.minecraft.world.gen.structure.Structure;

import java.util.ArrayList;

public class SetupSpawnPoint {

    public static void generatePlatform(ServerWorld world, BlockPos spawnPos, Chunk spawnChunk) {
        ChunkGenerator chunkGenerator = world.getChunkManager().getChunkGenerator();
        ArrayList<String> platformList = new ArrayList<>(Skylark.getConfig().spawnPlatform);

        Registry<ConfiguredFeature<?,?>> configuredFeatureRegistry = world.getRegistryManager().get(RegistryKeys.CONFIGURED_FEATURE);
        Registry<Structure> structureRegistry = world.getRegistryManager().get(RegistryKeys.STRUCTURE);

        while (platformList.size() > 0) {
            // Yank a random ID from our copy of the config list
            Identifier configSpawnPlatform = new Identifier(platformList.remove(world.getRandom().nextInt(platformList.size())));

            // Block spawn platform
            if (Registries.BLOCK.getOrEmpty(configSpawnPlatform).isPresent()) {
                Block spawnBlock = Registries.BLOCK.get(configSpawnPlatform);
                Skylark.LOGGER.info("Spawn platform from config is a block: {}", spawnBlock);
                if (world.setBlockState(spawnPos, spawnBlock.getDefaultState())) {
                    return;
                }
            }

            // Structure spawn platform
            if (structureRegistry.containsId(configSpawnPlatform)) {
                Skylark.LOGGER.warn("Spawn platform structures are currently unsupported!");
            }

            // Feature spawn platform
            if (configuredFeatureRegistry.containsId(configSpawnPlatform)) {
                ConfiguredFeature<?, ?> spawnFeature = configuredFeatureRegistry.get(configSpawnPlatform);
                assert(spawnFeature != null);  // Why would somebody add a null feature to the registry?!
                Skylark.LOGGER.info("Spawn platform from config is a feature: {}", spawnFeature);
                if (spawnFeature.generate(world, chunkGenerator, world.random, spawnPos)) {
                    return;
                }
            }

            Skylark.LOGGER.warn("Cannot resolve spawn platform from config: {}", configSpawnPlatform);
        }

        // Default spawn platform
        ConfiguredFeature<?, ?> spawnFeature = configuredFeatureRegistry.getOrThrow(TreeConfiguredFeatures.MEGA_SPRUCE);
        Skylark.LOGGER.info("Spawn platform is default feature: {}", spawnFeature);
        if (!spawnFeature.generate(world, chunkGenerator, world.random, spawnPos)) {
            Skylark.LOGGER.error("Failed to generate a spawn platform at {}", spawnPos);
        }
    }
}
