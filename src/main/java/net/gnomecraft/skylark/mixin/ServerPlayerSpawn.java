package net.gnomecraft.skylark.mixin;

import com.mojang.authlib.GameProfile;
import net.gnomecraft.skylark.Skylark;
import net.gnomecraft.skylark.spawn.SetupSpawnPoint;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.world.Heightmap;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerSpawn extends PlayerEntity {
    public ServerPlayerSpawn(World world, BlockPos pos, float yaw, GameProfile profile) {
        super(world, pos, yaw, profile);
    }

    @Redirect(method = "moveToSpawn",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/server/world/ServerWorld;getSpawnPos()Lnet/minecraft/util/math/BlockPos;"
            ))
    public BlockPos skylark$getSpawnPos(ServerWorld world) {
        // Only in the Overworld and only if the players aren't all just using the shared central platform.
        if (world.getRegistryKey().equals(World.OVERWORLD) && Skylark.getConfig().spawnRingRadius > 0) {
            // Get the player's personal/team spawn point coordinates.
            BlockPos spawnPos = Skylark.STATE.getPlayerSpawnPos(world, this);
            WorldChunk spawnChunk = world.getChunk(ChunkSectionPos.getSectionCoord(spawnPos.getX()), ChunkSectionPos.getSectionCoord(spawnPos.getZ()));

            // Generate a team spawn platform if there's nothing there already.
            if (spawnChunk.sampleHeightmap(Heightmap.Type.MOTION_BLOCKING, spawnPos.getX() & 0xF, spawnPos.getZ() & 0xF) < -63) {
                SetupSpawnPoint.generatePlatform(world, spawnPos, spawnChunk);
            }

            // Always override the global spawn position in the Overworld.
            return spawnPos.withY(spawnChunk.sampleHeightmap(Heightmap.Type.MOTION_BLOCKING, spawnPos.getX() & 0xF, spawnPos.getZ() & 0xF) + 1);
        }

        // Pass through to the real getSpawnPos() in other dimensions.
        return world.getSpawnPos();
    }
}
