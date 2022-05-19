package net.gnomecraft.skylark.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.registry.BuiltinRegistries;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.ChunkRegion;
import net.minecraft.world.biome.source.BiomeAccess;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.AquiferSampler;
import net.minecraft.world.gen.chunk.NoiseChunkGenerator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(NoiseChunkGenerator.class)
public abstract class NullNoiseChunkGeneration {
    @Redirect(method = "<init>(Lnet/minecraft/util/registry/Registry;Lnet/minecraft/util/registry/Registry;Lnet/minecraft/world/biome/source/BiomeSource;Lnet/minecraft/world/biome/source/BiomeSource;JLnet/minecraft/util/registry/RegistryEntry;)V",
            at = @At(value = "NEW", args = "class=net.minecraft.world.gen.chunk.AquiferSampler$FluidLevel",
                    target = "Lnet/minecraft/world/gen/chunk/AquiferSampler$FluidLevel;<init>(ILnet/minecraft/block/BlockState;)V"
            )
    )
    private AquiferSampler.FluidLevel skylark$getVoidFluidLevel(int y, BlockState state) {
        return new AquiferSampler.FluidLevel(64, Blocks.AIR.getDefaultState());
    }

    @Inject(method = "carve(Lnet/minecraft/world/ChunkRegion;JLnet/minecraft/world/biome/source/BiomeAccess;Lnet/minecraft/world/gen/StructureAccessor;Lnet/minecraft/world/chunk/Chunk;Lnet/minecraft/world/gen/GenerationStep$Carver;)V",
            at = @At("HEAD"),
            cancellable = true,
            locals = LocalCapture.NO_CAPTURE
    )
    public void skylark$cancelCarvers(ChunkRegion chunkRegion, long seed, BiomeAccess biomeAccess, StructureAccessor structureAccessor, Chunk chunk, GenerationStep.Carver generationStep, CallbackInfo ci) {
//        if(chunkRegion.getDimension().equals(DimensionType.OVERWORLD_ID)) {
            ci.cancel();
//        }
    }

/*
    @ModifyVariable(method = "<init>(Lnet/minecraft/util/registry/Registry;Lnet/minecraft/util/registry/Registry;Lnet/minecraft/world/biome/source/BiomeSource;Lnet/minecraft/world/biome/source/BiomeSource;JLnet/minecraft/util/registry/RegistryEntry;)V",
            at = @At("LOAD"),
            argsOnly = true
    )
    private Optional<RegistryEntryList<StructureSet>> skylark$emptyChunkGeneratorStructureSet(Optional<RegistryEntryList<StructureSet>> optional) {
        return Optional.of(RegistryEntryList.of());
    }
*/
}
