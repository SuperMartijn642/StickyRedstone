package com.supermartijn642.stickyredstone.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.world.level.redstone.ExperimentalRedstoneUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Created 03/08/2026 by SuperMartijn642
 */
@Mixin(ExperimentalRedstoneUtils.class)
public class ExperimentalRedstoneUtilsMixin {

    @ModifyExpressionValue(
        method = "initialOrientation",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/flag/FeatureFlagSet;contains(Lnet/minecraft/world/flag/FeatureFlag;)Z"
        )
    )
    private static boolean initialOrientation(boolean original){
        return false;
    }
}
