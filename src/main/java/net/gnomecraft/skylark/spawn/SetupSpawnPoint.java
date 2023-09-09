package net.gnomecraft.skylark.spawn;

import net.gnomecraft.skylark.Skylark;
import net.gnomecraft.skylark.config.SkylarkConfig;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.StructureStart;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.*;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.Heightmap;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.TreeConfiguredFeatures;
import net.minecraft.world.gen.structure.Structure;

import java.util.ArrayList;

public class SetupSpawnPoint {

    public static void generatePlatform(ServerWorld world, BlockPos spawnPos, Chunk spawnChunk) {
        ChunkGenerator chunkGenerator = world.getChunkManager().getChunkGenerator();
        Random random = world.getRandom();
        boolean success = false;

        SkylarkConfig config = Skylark.getConfig();
        ArrayList<String> platformList = new ArrayList<>(config.spawnPlatform);

        Registry<ConfiguredFeature<?, ?>> configuredFeatureRegistry = world.getRegistryManager().get(RegistryKeys.CONFIGURED_FEATURE);
        Registry<Structure> structureRegistry = world.getRegistryManager().get(RegistryKeys.STRUCTURE);

        while (platformList.size() > 0) {
            // Yank a random ID from our copy of the config list
            Identifier configSpawnPlatform = new Identifier(platformList.remove(random.nextInt(platformList.size())));

            // Block spawn platform
            if (Registries.BLOCK.getOrEmpty(configSpawnPlatform).isPresent()) {
                Block spawnBlock = Registries.BLOCK.get(configSpawnPlatform);
                Skylark.LOGGER.info("Spawn platform from config is a block: {}", spawnBlock);
                if (world.setBlockState(spawnPos, spawnBlock.getDefaultState())) {
                    success = true;
                    break;
                }
            }

            // Structure spawn platform
            if (structureRegistry.containsId(configSpawnPlatform)) {
                Structure structure = structureRegistry.get(configSpawnPlatform);
                assert structure != null;

                Skylark.LOGGER.info("Spawn platform from config is a structure: {}", configSpawnPlatform);
                StructureStart structureStart = structure.createStructureStart(world.getRegistryManager(), chunkGenerator, chunkGenerator.getBiomeSource(), world.getChunkManager().getNoiseConfig(), world.getStructureTemplateManager(), world.getSeed(), new ChunkPos(spawnPos), 0, world, biome -> true);
                if (structureStart.hasChildren()) {
                    BlockBox boundingBox = structureStart.getBoundingBox();
                    ChunkPos chunkMin = new ChunkPos(ChunkSectionPos.getSectionCoord(boundingBox.getMinX()), ChunkSectionPos.getSectionCoord(boundingBox.getMinZ()));
                    ChunkPos chunkMax = new ChunkPos(ChunkSectionPos.getSectionCoord(boundingBox.getMaxX()), ChunkSectionPos.getSectionCoord(boundingBox.getMaxZ()));
                    ChunkPos.stream(chunkMin, chunkMax).forEach(chunkPos -> structureStart.place(world, world.getStructureAccessor(), chunkGenerator, world.getRandom(), new BlockBox(chunkPos.getStartX(), world.getBottomY(), chunkPos.getStartZ(), chunkPos.getEndX(), world.getTopY(), chunkPos.getEndZ()), chunkPos));
                    success = true;
                    break;
                } else {
                    Skylark.LOGGER.warn("Failed to create Spawn platform structure.");
                }
            }

            // Feature spawn platform
            if (configuredFeatureRegistry.containsId(configSpawnPlatform)) {
                ConfiguredFeature<?, ?> spawnFeature = configuredFeatureRegistry.get(configSpawnPlatform);
                assert (spawnFeature != null);  // Why would somebody add a null feature to the registry?!
                Skylark.LOGGER.info("Spawn platform from config is a feature: {}", spawnFeature);
                if (spawnFeature.generate(world, chunkGenerator, random, spawnPos)) {
                    success = true;
                    break;
                }
            }

            Skylark.LOGGER.warn("Cannot resolve spawn platform from config: {}", configSpawnPlatform);
        }

        // Default spawn platform
        ConfiguredFeature<?, ?> spawnFeature = configuredFeatureRegistry.getOrThrow(TreeConfiguredFeatures.MEGA_SPRUCE);
        if (!success && spawnFeature.generate(world, chunkGenerator, random, spawnPos)) {
            Skylark.LOGGER.info("Spawn platform is default feature: {}", spawnFeature);
            success = true;
        }

        if (!success) {
            Skylark.LOGGER.error("Failed to generate a spawn platform at {}", spawnPos);
        }
    }

    public static void generateSpawnChest(ServerWorld world, BlockPos spawnPos) {
        SkylarkConfig config = Skylark.getConfig();

        // Configurable starter chest
        if (config.starterChest.size() > 0) {
            Skylark.LOGGER.info("Adding spawn chest with {} stacks...", config.starterChest.size());
            BlockPos chestPos = world.getTopPosition(Heightmap.Type.MOTION_BLOCKING, spawnPos);

            world.setBlockState(chestPos, Blocks.CHEST.getDefaultState());
            if (world.getBlockEntity(chestPos) instanceof Inventory inventory) {
                for (int slot = 0; slot < config.starterChest.size() && slot < inventory.size(); ++slot) {
                    String[] splits = config.starterChest.get(slot).split(";");
                    Item item = Registries.ITEM.getOrEmpty(new Identifier(splits[0])).orElse(null);
                    if (item == null) {
                        Skylark.LOGGER.debug("Failed to parse starter chest item for slot {}: {}", slot, splits[0]);
                    } else {
                        int amount = 1;
                        if (splits.length > 1) {
                            try {
                                amount = MathHelper.clamp(Integer.parseInt(splits[splits.length - 1]), 1, item.getMaxCount());
                            } catch (Exception ignored) {}
                        }
                        inventory.setStack(slot, new ItemStack(item, amount));
                    }
                }
            }
        }
    }
}