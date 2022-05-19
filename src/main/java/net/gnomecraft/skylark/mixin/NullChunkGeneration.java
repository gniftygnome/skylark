package net.gnomecraft.skylark.mixin;

import net.minecraft.structure.StructureSet;
import net.minecraft.util.registry.*;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;

import java.util.Optional;

@Mixin(ChunkGenerator.class)
public abstract class NullChunkGeneration {
    @ModifyVariable(method = "<init>(Lnet/minecraft/util/registry/Registry;Ljava/util/Optional;Lnet/minecraft/world/biome/source/BiomeSource;Lnet/minecraft/world/biome/source/BiomeSource;J)V",
            at = @At("LOAD"),
            argsOnly = true
    )
    // Inspection claims this method must be static.  Make it static and the mixin will fail to apply...
    private Optional<RegistryEntryList<StructureSet>> skylark$emptyChunkGeneratorStructureSet(Optional<RegistryEntryList<StructureSet>> optional) {
        return Optional.of(RegistryEntryList.of());
    }

}
