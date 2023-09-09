package net.gnomecraft.skylark.mixin;

import net.gnomecraft.skylark.Skylark;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerTask;
import net.minecraft.server.WorldGenerationProgressListener;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.thread.ReentrantThreadExecutor;
import net.minecraft.world.World;
import net.minecraft.world.level.ServerWorldProperties;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Map;

@Mixin(MinecraftServer.class)
public abstract class ReplaceSetupSpawn extends ReentrantThreadExecutor<ServerTask> {
    public ReplaceSetupSpawn(String string) {
        super(string);
    }

    @Shadow
    @Final
    private Map<RegistryKey<World>, ServerWorld> worlds;

    @Inject(method = "createWorlds",
            at = @At(value = "FIELD",
                    target = "Lnet/minecraft/server/MinecraftServer;worlds:Ljava/util/Map;",
                    opcode = Opcodes.GETFIELD,
                    shift = At.Shift.BY,
                    by = 2
            ),
            locals = LocalCapture.NO_CAPTURE
    )
    protected void skylark$acquireOverworld(WorldGenerationProgressListener worldGenerationProgressListener, CallbackInfo ci) {
        ServerWorld overworld = worlds.get(World.OVERWORLD);
        if (overworld != null) {
            // Stow the world for later use.  =)
            Skylark.STATE.init(overworld);
        }
    }

    @Inject(method = "setupSpawn",
            at = @At("HEAD"),
            cancellable = true,
            locals = LocalCapture.NO_CAPTURE
    )
    private static void skylark$replaceSetupSpawn(ServerWorld world, ServerWorldProperties worldProperties, boolean bonusChest, boolean debugWorld, CallbackInfo ci) {
        if (world.getRegistryKey().equals(World.OVERWORLD)) {
            // Stow the world for later use.  =)
            Skylark.STATE.init(world);

            // Defined position for single spawn location.
            BlockPos spawnPos = new BlockPos(0, Skylark.getConfig().spawnHeight, 0);

            // Make sure there is a spawn platform and it is targeted by the spawn coordinates.
            spawnPos = Skylark.STATE.preparePlayerSpawn(spawnPos);

            // Set global player spawn.
            worldProperties.setSpawnPos(spawnPos, 0.0f);

            ci.cancel();
        }
    }
}
