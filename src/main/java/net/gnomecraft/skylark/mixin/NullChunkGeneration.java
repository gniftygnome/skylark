package net.gnomecraft.skylark.mixin;

import com.mojang.datafixers.util.Pair;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.StructureManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.*;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.feature.ConfiguredStructureFeature;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(ChunkGenerator.class)
public abstract class NullChunkGeneration {
    @Inject(method = "setStructureStarts",
            at = @At("HEAD"),
            cancellable = true,
            locals = LocalCapture.NO_CAPTURE
    )
    private void skylark$skipOverworldStructureStarts(DynamicRegistryManager registryManager, StructureAccessor world, Chunk chunk, StructureManager structureManager, long worldSeed, CallbackInfo ci) {
        // Relies on an access widener for StructureAccessor.world.
        // Also, this approach causes very slow locate times for overworld structures (but see below).
        if (((ServerWorld) world.world).getRegistryKey().equals(World.OVERWORLD)) {
            ci.cancel();
        }
    }

    @Inject(method = "locateStructure",
            at = @At("HEAD"),
            cancellable = true,
            locals = LocalCapture.NO_CAPTURE
    )
    private void skylark$abortOverworldLocateStructure(ServerWorld arg2, RegistryEntryList<ConfiguredStructureFeature<?, ?>> arg22, BlockPos center, int radius, boolean skipExistingChunks, CallbackInfoReturnable<Pair<BlockPos, RegistryEntry<ConfiguredStructureFeature<?, ?>>>> cir) {
        if (arg2.getRegistryKey().equals(World.OVERWORLD)) {
            cir.setReturnValue(null);
        }
    }

/*  NOTE: This method works great, except it disables generation in the Nether and the End too.  :(
    @ModifyVariable(method = "<init>(Lnet/minecraft/util/registry/Registry;Ljava/util/Optional;Lnet/minecraft/world/biome/source/BiomeSource;Lnet/minecraft/world/biome/source/BiomeSource;J)V",
            at = @At("LOAD"),
            argsOnly = true
    )
    // Inspection claims this method must be static.  Make it static and the mixin will fail to apply...
    private Optional<RegistryEntryList<StructureSet>> skylark$emptyChunkGeneratorStructureSet(Optional<RegistryEntryList<StructureSet>> optional) {
        return Optional.of(RegistryEntryList.of());
    }
*/
}
