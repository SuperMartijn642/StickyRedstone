package com.supermartijn642.stickyredstone.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.supermartijn642.stickyredstone.content.wire.StickyRedstoneDust;
import com.supermartijn642.stickyredstone.content.wire.StickyRedstoneWireEvaluator;
import net.minecraft.client.renderer.extract.LevelExtractor;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Created 04/08/2026 by SuperMartijn642
 */
@Mixin(LevelExtractor.class)
public class LevelExtractorMixin {

    @WrapOperation(
        method = "extractBlockOutline",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/level/block/state/BlockState;getShape(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/phys/shapes/CollisionContext;)Lnet/minecraft/world/phys/shapes/VoxelShape;"
        )
    )
    private VoxelShape extractBlockOutline(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context, Operation<VoxelShape> operation, @Local BlockHitResult hitResult) {
        if(!StickyRedstoneWireEvaluator.isStickyRedstoneWire(state.getBlock()))
            return operation.call(state, level, pos, context);
        return ((StickyRedstoneDust)state.getBlock()).getHighlightShape(state, level, pos, context, hitResult);
    }
}
