package net.gnomecraft.skylark.mixin;

import com.mojang.datafixers.util.Pair;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.StructureTemplateManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.*;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.noise.NoiseConfig;
import net.minecraft.world.gen.structure.Structure;
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
    private void skylark$skipOverworldStructureStarts(DynamicRegistryManager registryManager, NoiseConfig noiseConfig, StructureAccessor structureAccessor, Chunk chunk, StructureTemplateManager structureTemplateManager, long seed, CallbackInfo ci) {
        // Relies on an access widener for StructureAccessor.world.
        // Also, this approach causes very slow locate times for overworld structures (but see below).
        if (((ServerWorld) structureAccessor.world).getRegistryKey().equals(World.OVERWORLD)) {
            ci.cancel();
        }
    }

    @Inject(method = "locateStructure(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/util/registry/RegistryEntryList;Lnet/minecraft/util/math/BlockPos;IZ)Lcom/mojang/datafixers/util/Pair;",
            at = @At("HEAD"),
            cancellable = true,
            locals = LocalCapture.NO_CAPTURE
    )
    private void skylark$abortOverworldLocateStructure(ServerWorld world, RegistryEntryList<Structure> structures, BlockPos center, int radius, boolean skipReferencedStructures, CallbackInfoReturnable<Pair<BlockPos, RegistryEntry<Structure>>> cir) {
        if (world.getRegistryKey().equals(World.OVERWORLD)) {
            cir.setReturnValue(null);
        }
    }

/*  NOTE: This (1.18.2) method works great, except it disables generation in the Nether and the End too.  :(
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
