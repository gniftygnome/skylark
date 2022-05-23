package net.gnomecraft.skylark.mixin;

import net.gnomecraft.skylark.spawn.SetupSpawn;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerTask;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.thread.ReentrantThreadExecutor;
import net.minecraft.world.Heightmap;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.level.ServerWorldProperties;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(MinecraftServer.class)
public abstract class ReplaceSetupSpawn extends ReentrantThreadExecutor<ServerTask> {
    public ReplaceSetupSpawn(String string) {
        super(string);
    }

    @Inject(method = "setupSpawn",
            at = @At("HEAD"),
            cancellable = true,
            locals = LocalCapture.NO_CAPTURE
    )
    private static void replaceSetupSpawn(ServerWorld world, ServerWorldProperties worldProperties, boolean bonusChest, boolean debugWorld, CallbackInfo ci) {
        // Defined position for single spawn location.
        BlockPos spawnPos = new BlockPos(0, 120, 0);
        WorldChunk spawnChunk = world.getChunk(ChunkSectionPos.getSectionCoord(spawnPos.getX()), ChunkSectionPos.getSectionCoord(spawnPos.getZ()));

        // Generate a shred spawn platform.
        SetupSpawn.sharedPlatform(world, spawnPos, spawnChunk);

        // Locate and set player spawn.
        BlockPos adjSpawnPos = spawnPos.withY(spawnChunk.sampleHeightmap(Heightmap.Type.MOTION_BLOCKING, spawnPos.getX() & 0xF, spawnPos.getZ() & 0xF) + 1);
        worldProperties.setSpawnPos(adjSpawnPos, 0.0f);

        ci.cancel();
    }
}
