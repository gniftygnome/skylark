package net.gnomecraft.skylark.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.gnomecraft.skylark.Skylark;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
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
                    target = "Lnet/minecraft/server/MinecraftServer;getWorld(Lnet/minecraft/registry/RegistryKey;)Lnet/minecraft/server/world/ServerWorld;"
            ))
    protected ServerWorld skylark$respawnPlatform(MinecraftServer server, RegistryKey<World> worldKey, Operation<ServerWorld> original, ServerPlayerEntity player, boolean alive) {
        if (!alive && (worldKey == null || worldKey.equals(World.OVERWORLD))) {
            ServerWorld overworld = server.getOverworld();

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

        return original.call(server, worldKey);
    }
}
