package net.gnomecraft.skylark.mixin;

import net.minecraft.block.Blocks;
import net.minecraft.util.registry.BuiltinRegistries;
import net.minecraft.world.biome.source.util.VanillaBiomeParameters;
import net.minecraft.world.gen.chunk.ChunkGeneratorSettings;
import net.minecraft.world.gen.densityfunction.DensityFunctions;
import net.minecraft.world.gen.surfacebuilder.VanillaSurfaceRules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(ChunkGeneratorSettings.class)
public abstract class NullChunkGeneratorSettings {
    @Inject(method = "createSurfaceSettings",
            at = @At("RETURN"),
            cancellable = true,
            locals = LocalCapture.CAPTURE_FAILHARD
    )
    private static void skylark$voidSurfaceSettings(boolean amplified, boolean largeBiomes, CallbackInfoReturnable<ChunkGeneratorSettings> cir) {
        cir.setReturnValue(new ChunkGeneratorSettings(cir.getReturnValue().generationShapeConfig(), Blocks.AIR.getDefaultState(), Blocks.AIR.getDefaultState(), DensityFunctions.createSurfaceNoiseRouter(BuiltinRegistries.DENSITY_FUNCTION, false, false), VanillaSurfaceRules.createOverworldSurfaceRule(), new VanillaBiomeParameters().getSpawnSuitabilityNoises(), -80, false, false, false, false));
    }

    @Inject(method = "hasAquifers",
            at = @At("HEAD"),
            cancellable = true,
            locals = LocalCapture.NO_CAPTURE
    )
    private void skylark$voidAquifers(CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(false);
    }

    @Inject(method = "oreVeins",
            at = @At("HEAD"),
            cancellable = true,
            locals = LocalCapture.NO_CAPTURE
    )
    private void skylark$voidOreVeins(CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(false);
    }
}
