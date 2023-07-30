package net.gnomecraft.skylark.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.world.biome.source.util.MultiNoiseUtil;
import net.minecraft.world.gen.chunk.ChunkGeneratorSettings;
import net.minecraft.world.gen.chunk.GenerationShapeConfig;
import net.minecraft.world.gen.noise.NoiseRouter;
import net.minecraft.world.gen.surfacebuilder.MaterialRules;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(ChunkGeneratorSettings.class)
public abstract class NullChunkGeneratorSettings {
    @Shadow
    @Mutable
    @Final
    private BlockState defaultBlock;

    @Shadow
    @Mutable
    @Final
    private BlockState defaultFluid;

    @Shadow
    @Mutable
    @Final
    private int seaLevel;

    @Shadow
    @Mutable
    @Final
    private boolean aquifers;

    @Shadow
    @Mutable
    @Final
    private boolean oreVeins;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void skylark$voidSurfaceSettings(GenerationShapeConfig generationShapeConfig, BlockState defaultBlock, BlockState defaultFluid, NoiseRouter noiseRouter, MaterialRules.MaterialRule surfaceRule, List<MultiNoiseUtil.NoiseHypercube> spawnTarget, int seaLevel, boolean mobGenerationDisabled, boolean aquifers, boolean oreVeins, boolean usesLegacyRandom, CallbackInfo ci) {
        // TODO: This is a hacky way to detect the Overworld;
        //       It's going to bite me at some point but that's then and this is now...
        if (seaLevel > 48) {
            this.defaultBlock = Blocks.AIR.getDefaultState();
            this.defaultFluid = Blocks.AIR.getDefaultState();
            this.seaLevel = -80;
            this.aquifers = false;
            this.oreVeins = false;
        }
    }
}
