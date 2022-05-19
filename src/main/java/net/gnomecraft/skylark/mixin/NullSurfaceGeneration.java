package net.gnomecraft.skylark.mixin;

import net.minecraft.block.Blocks;
import net.minecraft.world.gen.surfacebuilder.MaterialRules;
import net.minecraft.world.gen.surfacebuilder.VanillaSurfaceRules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(VanillaSurfaceRules.class)
public class NullSurfaceGeneration {
    @Inject(method = "createDefaultRule",
            at = @At(value = "RETURN"),
            cancellable = true,
            locals = LocalCapture.NO_CAPTURE
    )
    private static void skylark$nullifyDefaultOverworldSurfaceRule(boolean surface, boolean bedrockRoof, boolean bedrockFloor, CallbackInfoReturnable<MaterialRules.MaterialRule> cir) {
        MaterialRules.MaterialRule nullMaterialRule = MaterialRules.block(Blocks.AIR.getDefaultState());
        cir.setReturnValue(nullMaterialRule);
    }
}
