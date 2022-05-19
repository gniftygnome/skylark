package net.gnomecraft.skylark.mixin;

import net.minecraft.util.registry.DynamicRegistryManager;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(targets = "net.minecraft.client.world.GeneratorType$1")
public class NullWorldSelection {
    @Inject(method = "getChunkGenerator(Lnet/minecraft/util/registry/DynamicRegistryManager;J)Lnet/minecraft/world/gen/chunk/ChunkGenerator;",
            at = @At(value = "HEAD"),
            cancellable = true,
            locals = LocalCapture.NO_CAPTURE
    )
    private void skylark$nullifyOverworldSelection(DynamicRegistryManager registryManager, long seed, CallbackInfoReturnable<ChunkGenerator> cir) {
    }
}
