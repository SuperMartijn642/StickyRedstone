package com.supermartijn642.stickyredstone.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.supermartijn642.stickyredstone.content.wire.StickyRedstoneWireEvaluator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Created 05/08/2026 by SuperMartijn642
 */
@Mixin(Minecraft.class)
public class MinecraftMixin {

    @Shadow
    private @Nullable ClientLevel level;
    @Shadow
    private @Nullable HitResult hitResult;

    @ModifyExpressionValue(
        method = "continueAttack",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/multiplayer/ClientLevel;isEmptyBlock(Lnet/minecraft/core/BlockPos;)Z"
        )
    )
    private boolean continueAttack(boolean original) {
        return original || StickyRedstoneWireEvaluator.isStickyRedstoneWire(this.level.getBlockState(((BlockHitResult)this.hitResult).getBlockPos()).getBlock());
    }
}
