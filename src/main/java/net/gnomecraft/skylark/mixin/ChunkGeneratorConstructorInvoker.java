package net.gnomecraft.skylark.mixin;

import net.minecraft.structure.StructureSet;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryEntryList;
import net.minecraft.world.biome.source.BiomeSource;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.Optional;

@Mixin(ChunkGenerator.class)
public interface ChunkGeneratorConstructorInvoker {
    @Invoker("<init>")
    static ChunkGenerator callChunkGenerator(Registry<StructureSet> arg, Optional<RegistryEntryList<StructureSet>> optional, BiomeSource arg2, BiomeSource arg3, long l) {
        // fake out the compiler
        return null;
    }
}