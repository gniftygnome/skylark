package net.gnomecraft.skylark.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.authlib.GameProfile;
import net.gnomecraft.skylark.Skylark;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerSpawn extends PlayerEntity {
    public ServerPlayerSpawn(World world, BlockPos pos, float yaw, GameProfile gameProfile) {
        super(world, pos, yaw, gameProfile);
    }

    @WrapOperation(method = "moveToSpawn",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/server/world/ServerWorld;getSpawnPos()Lnet/minecraft/util/math/BlockPos;"
            ))
    public BlockPos skylark$getSpawnPos(ServerWorld world, Operation<BlockPos> original) {
        // Only in the Overworld.
        if (world.getRegistryKey().equals(World.OVERWORLD)) {
            // Stow the world for later use.  =)
            Skylark.STATE.init(world);

            // Only if the players aren't all just using the shared central platform.
            if (Skylark.getConfig().spawnRingRadius > 0) {
                // Get the player's personal/team spawn point coordinates.
                BlockPos spawnPos = Skylark.STATE.getPlayerSpawnPos(world, this);

                // Make sure there is a spawn platform and it is targeted by the spawn coordinates.
                return Skylark.STATE.preparePlayerSpawn(spawnPos);
            }
        }

        // Pass through to the real getSpawnPos() in other dimensions.
        return original.call(world);
    }
}
