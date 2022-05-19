package net.gnomecraft.skylark.mixin;

import net.minecraft.structure.StructureSet;
import net.minecraft.util.registry.*;
import net.minecraft.world.biome.BiomeKeys;
import net.minecraft.world.biome.source.BiomeSource;
import net.minecraft.world.biome.source.FixedBiomeSource;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;

import java.util.Optional;

@Mixin(ChunkGenerator.class)
public abstract class NullChunkGeneration {
    @Shadow private Registry<StructureSet> field_37053;
    @Shadow private BiomeSource populationSource;
    @Shadow private BiomeSource biomeSource;
    @Shadow protected Optional<RegistryEntryList<StructureSet>> field_37054;
    @Shadow private long field_37261;

    @ModifyVariable(method = "<init>(Lnet/minecraft/util/registry/Registry;Ljava/util/Optional;Lnet/minecraft/world/biome/source/BiomeSource;Lnet/minecraft/world/biome/source/BiomeSource;J)V",
            at = @At("LOAD"),
            argsOnly = true
    )
    private Optional<RegistryEntryList<StructureSet>> skylark$emptyChunkGeneratorStructureSet(Optional<RegistryEntryList<StructureSet>> optional) {
        return Optional.of(RegistryEntryList.of());
    }

/*
    @ModifyVariable(method = "<init>(Lnet/minecraft/util/registry/Registry;Ljava/util/Optional;Lnet/minecraft/world/biome/source/BiomeSource;Lnet/minecraft/world/biome/source/BiomeSource;J)V",
            at = @At(value = "LOAD"),
            ordinal = 0,
            argsOnly = true
    )
    private BiomeSource skylark$voidPopulationSource(BiomeSource argv2) {
        return new FixedBiomeSource(BuiltinRegistries.BIOME.entryOf(BiomeKeys.THE_VOID));
    }
*/

/*
    @Inject(method = "<init>(Lnet/minecraft/util/registry/Registry;Ljava/util/Optional;Lnet/minecraft/world/biome/source/BiomeSource;Lnet/minecraft/world/biome/source/BiomeSource;J)V",
            at = @At(value = "RETURN", shift = At.Shift.BY, by = -2)
    )
    void skylark$initChunkGenerator(Registry<StructureSet> arg, Optional<RegistryEntryList<StructureSet>> optional, BiomeSource arg2, BiomeSource arg3, long l, CallbackInfo ci) {
        //this.field_37053 = arg;
        //this.populationSource = arg2;
        //this.biomeSource = arg3;
        this.field_37054 = Optional.of(RegistryEntryList.of());
        //this.field_37261 = l;
        @ModifyVariable()
    }
*/
}
