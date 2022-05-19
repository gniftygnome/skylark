package net.gnomecraft.skylark.mixin;

import net.minecraft.server.dedicated.ServerPropertiesHandler;
import net.minecraft.structure.StructureSet;
import net.minecraft.util.registry.DynamicRegistryManager;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.dimension.DimensionOptions;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.gen.GeneratorOptions;
import net.minecraft.world.gen.chunk.FlatChunkGenerator;
import net.minecraft.world.gen.chunk.FlatChunkGeneratorConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Random;

@Mixin(GeneratorOptions.class)
public class NullWorldGeneration {
    @Inject(method = "fromProperties",
            at = @At(value = "HEAD"),
            cancellable = true,
            locals = LocalCapture.NO_CAPTURE
    )
    private static void skylark$nullifyOverworldGeneration(DynamicRegistryManager registryManager, ServerPropertiesHandler.WorldGenProperties worldGenProperties, CallbackInfoReturnable<GeneratorOptions> cir) {
        long seed = GeneratorOptions.parseSeed(worldGenProperties.levelSeed()).orElse(new Random().nextLong());
        Registry<DimensionType> dimensionTypes = registryManager.get(Registry.DIMENSION_TYPE_KEY);
        Registry<Biome> biomes = registryManager.get(Registry.BIOME_KEY);
        Registry<StructureSet> structureSets = registryManager.get(Registry.STRUCTURE_SET_KEY);
        Registry<DimensionOptions> dimensionOptions = DimensionType.createDefaultDimensionOptions(registryManager, seed);
        GeneratorOptions nullGeneratorOptions = new GeneratorOptions(
                seed, false, false,
                GeneratorOptions.getRegistryWithReplacedOverworldGenerator(
                        dimensionTypes,
                        dimensionOptions,
                        new FlatChunkGenerator(
                                structureSets,
                                FlatChunkGeneratorConfig.getDefaultConfig(biomes, structureSets))));;
        cir.setReturnValue(nullGeneratorOptions);
    }
}
