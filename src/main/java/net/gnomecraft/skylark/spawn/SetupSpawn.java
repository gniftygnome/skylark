package net.gnomecraft.skylark.spawn;

import net.gnomecraft.skylark.Skylark;
import net.minecraft.block.Block;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.BuiltinRegistries;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.TreeConfiguredFeatures;

public class SetupSpawn {

    public static void sharedPlatform(ServerWorld world, BlockPos spawnPos, Chunk spawnChunk) {
        ChunkGenerator chunkGenerator = world.getChunkManager().getChunkGenerator();
        Identifier configSpawnPlatform = Skylark.getConfig().spawnPlatform;
        if(BuiltinRegistries.CONFIGURED_STRUCTURE_FEATURE.containsId(configSpawnPlatform)) {
            Skylark.LOGGER.warn("Spawn platform structures are currently unsupported!");
        }
        if (Registry.BLOCK.getOrEmpty(configSpawnPlatform).isPresent()) {
            Block spawnBlock = Registry.BLOCK.get(configSpawnPlatform);
            world.setBlockState(spawnPos, spawnBlock.getDefaultState());
            Skylark.LOGGER.info("Spawn platform from config is a block: " + spawnBlock);
        } else {
            ConfiguredFeature<?, ?> spawnFeature;
            if ((spawnFeature = BuiltinRegistries.CONFIGURED_FEATURE.get(configSpawnPlatform)) != null) {
                Skylark.LOGGER.info("Spawn platform from config is a feature: " + spawnFeature);
            } else {
                spawnFeature = TreeConfiguredFeatures.MEGA_SPRUCE.value();
                Skylark.LOGGER.info("Spawn platform is default feature: " + spawnFeature);
            }
            spawnFeature.generate(world, chunkGenerator, world.random, spawnPos);
        }
    }
}
