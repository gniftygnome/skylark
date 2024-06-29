package net.gnomecraft.skylark.util;

import net.minecraft.server.network.SpawnLocating;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@SuppressWarnings("UnnecessaryReturnStatement")
public class LandLocator {
    LandLocator() {
        return;
    }

    /*
     * This method is kind of like a specialized version of the search in
     * the middle of vanilla's ServerPlayerEntity.getWorldSpawnPos() method.
     *
     * It exists because vanilla's version is not broken out into a separate
     * function, and we need to find a real spawn point for that same method
     * before it reaches its own version of the calculation.
     */
    public static @NotNull BlockPos refineSpawnPos(@NotNull ServerWorld world, @NotNull BlockPos target) {
        // First check the original spawn column for land.
        BlockPos spawnPos = SpawnLocating.findOverworldSpawn(world, target.getX(), target.getZ());
        if (spawnPos != null) {
            return spawnPos;
        }

        // Next check an expanding square of columns around the original spawn point.
        for (int distance = 1; distance < 32; ++distance) {
            for (int rotation = -distance; rotation < distance; ++rotation) {
                for (Direction direction : List.of(Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST)) {
                    Direction side = direction.rotateYClockwise();
                    spawnPos = SpawnLocating.findOverworldSpawn(world,
                            target.getX() + direction.getOffsetX() * distance + side.getOffsetX() * rotation,
                            target.getZ() + direction.getOffsetZ() * distance + side.getOffsetZ() * rotation);
                    if (spawnPos != null) {
                        return spawnPos;
                    }
                }
            }
        }

        // Fall back to the original spawn point if no land is found.
        return target;
    }
}
