package net.gnomecraft.skylark.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.gnomecraft.skylark.Skylark;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(PlayerManager.class)
public class PlayerManagerRespawn {
    @WrapOperation(method = "respawnPlayer",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/server/network/ServerPlayerEntity;getServerWorld()Lnet/minecraft/server/world/ServerWorld;"
            ))
    protected ServerWorld skylark$respawnPlatform(ServerPlayerEntity player, Operation<ServerWorld> original, @Local boolean alive) {
        ServerWorld world = original.call(player);
        RegistryKey<World> worldKey = world.getRegistryKey();

        if (!alive && (worldKey == null || worldKey.equals(World.OVERWORLD))) {
            ServerWorld overworld = world.getServer().getOverworld();

            // Get the player's team spawn point coordinates.
            BlockPos spawnPos = Skylark.STATE.getPlayerSpawnPos(overworld, player);

            // Make sure there is a spawn platform and it is targeted by the spawn coordinates.
            spawnPos = Skylark.STATE.preparePlayerSpawn(spawnPos);

            // Relocate the player a bit if they're missing the platform.
            BlockPos oldSpawnPos = player.getSpawnPointPosition();
            if (oldSpawnPos == null || spawnPos.getSquaredDistance(oldSpawnPos.withY(spawnPos.getY())) > 1d) {
                player.setSpawnPoint(World.OVERWORLD, spawnPos, player.getSpawnAngle(), player.isSpawnForced(), false);
            }
        }

        return world;
    }
}
