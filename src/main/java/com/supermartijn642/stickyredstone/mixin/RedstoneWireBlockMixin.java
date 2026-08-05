package com.supermartijn642.stickyredstone.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.supermartijn642.stickyredstone.StickyRedstone;
import com.supermartijn642.stickyredstone.content.StickyRepeater;
import com.supermartijn642.stickyredstone.content.wire.StickyRedstoneWireEvaluator;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.RedStoneWireBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Created 03/08/2026 by SuperMartijn642
 */
@Mixin(RedStoneWireBlock.class)
public class RedstoneWireBlockMixin {

    @Inject(
        method = "useExperimentalEvaluator",
        at = @At("HEAD"),
        cancellable = true
    )
    private static void useExperimentalEvaluator(Level level, CallbackInfoReturnable<Boolean> ci){
        ci.setReturnValue(false);
    }

    @ModifyExpressionValue(
        method = "updateIndirectNeighbourShapes",
        at = {
            @At(
                value = "INVOKE",
                target = "Lnet/minecraft/world/level/block/state/BlockState;is(Ljava/lang/Object;)Z",
                ordinal = 1
            ),
            @At(
                value = "INVOKE",
                target = "Lnet/minecraft/world/level/block/state/BlockState;is(Ljava/lang/Object;)Z",
                ordinal = 2
            )
        }
    )
    private boolean updateIndirectNeighbourShapes(boolean original, @Local LevelAccessor level, @Local BlockPos.MutableBlockPos neighborPos){
        return original || StickyRedstoneWireEvaluator.isStickyRedstoneWire(level.getBlockState(neighborPos).getBlock());
    }

    @ModifyExpressionValue(
        method = "getConnectingSide(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/Direction;Z)Lnet/minecraft/world/level/block/state/properties/RedstoneSide;",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/level/block/RedStoneWireBlock;shouldConnectTo(Lnet/minecraft/world/level/block/state/BlockState;)Z",
            ordinal = 0
        )
    )
    private boolean checkConnectionUp(boolean original, @Local BlockGetter level, @Local(ordinal = 1) BlockPos relativePos, @Local Direction direction){
        if(original)
            return true;
        BlockPos neighborPos = relativePos.above();
        BlockState neighborState = level.getBlockState(neighborPos);
        if(!StickyRedstoneWireEvaluator.isStickyRedstoneWire(neighborState.getBlock()))
            return false;
        return StickyRedstoneWireEvaluator.getConnections(level, neighborPos, neighborState, Direction.DOWN).isPresent();
    }

    @ModifyExpressionValue(
        method = "getConnectingSide(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/Direction;Z)Lnet/minecraft/world/level/block/state/properties/RedstoneSide;",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/level/block/RedStoneWireBlock;shouldConnectTo(Lnet/minecraft/world/level/block/state/BlockState;)Z",
            ordinal = 1
        )
    )
    private boolean checkConnectionDown(boolean original, @Local BlockGetter level, @Local(ordinal = 1) BlockPos relativePos, @Local Direction direction){
        if(original)
            return true;
        BlockPos neighborPos = relativePos.below();
        BlockState neighborState = level.getBlockState(neighborPos);
        if(!StickyRedstoneWireEvaluator.isStickyRedstoneWire(neighborState.getBlock()))
            return false;
        return StickyRedstoneWireEvaluator.getConnections(level, neighborPos, neighborState, direction.getOpposite()).isPresent();
    }

    @ModifyExpressionValue(
        method = "getConnectingSide(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/Direction;Z)Lnet/minecraft/world/level/block/state/properties/RedstoneSide;",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/level/block/RedStoneWireBlock;shouldConnectTo(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/Direction;)Z"
        )
    )
    private boolean checkConnectionSide(boolean original, @Local BlockGetter level, @Local(ordinal = 1) BlockPos neighborPos, @Local BlockState neighborState){
        if(original)
            return true;
        if(!StickyRedstoneWireEvaluator.isStickyRedstoneWire(neighborState.getBlock()))
            return false;
        return StickyRedstoneWireEvaluator.getConnections(level, neighborPos, neighborState, Direction.DOWN).isPresent();
    }

    @Inject(
        method = "shouldConnectTo(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/Direction;)Z",
        at = @At("HEAD"),
        cancellable = true
    )
    private static void checkDiodeFace(BlockState state, @Nullable Direction direction, CallbackInfoReturnable<Boolean> ci){
        if(!StickyRepeater.isStickyDiode(state))
            return;
        if(StickyRepeater.getDiodeFace(state) != Direction.DOWN){
            ci.setReturnValue(false);
            return;
        }
        if(state.is(StickyRedstone.stickyRepeater))
            ci.setReturnValue(direction != null && StickyRepeater.getDiodeFront(state).getAxis() == direction.getAxis());
    }
}
