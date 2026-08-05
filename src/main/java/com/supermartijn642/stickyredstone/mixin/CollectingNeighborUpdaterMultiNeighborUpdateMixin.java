package com.supermartijn642.stickyredstone.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.world.flag.FeatureFlag;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.redstone.CollectingNeighborUpdater;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Created 03/08/2026 by SuperMartijn642
 */
@Mixin(CollectingNeighborUpdater.MultiNeighborUpdate.class)
public class CollectingNeighborUpdaterMultiNeighborUpdateMixin {

    @ModifyExpressionValue(
        method = "runNext",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/flag/FeatureFlagSet;contains(Lnet/minecraft/world/flag/FeatureFlag;)Z"
        )
    )
    private boolean runNext(boolean original) {
        return false;
    }
}
