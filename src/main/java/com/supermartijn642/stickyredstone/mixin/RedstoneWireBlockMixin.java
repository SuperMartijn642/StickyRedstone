package com.supermartijn642.stickyredstone.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.supermartijn642.stickyredstone.content.wire.FaceState;
import com.supermartijn642.stickyredstone.content.wire.StickyRedstoneWireEvaluator;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.RedStoneWireBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Created 03/08/2026 by SuperMartijn642
 */
@Mixin(RedStoneWireBlock.class)
public class RedstoneWireBlockMixin {

    @ModifyExpressionValue(
        method = "updateIndirectNeighbourShapes",
        at = {
            @At(
                value = "INVOKE",
                target = "Lnet/minecraft/world/level/block/state/BlockState;is(Lnet/minecraft/world/level/block/Block;)Z",
                ordinal = 1
            ),
            @At(
                value = "INVOKE",
                target = "Lnet/minecraft/world/level/block/state/BlockState;is(Lnet/minecraft/world/level/block/Block;)Z",
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
            target = "Lnet/minecraft/world/level/block/state/BlockState;canRedstoneConnectTo(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/Direction;)Z",
            ordinal = 0
        )
    )
    private boolean checkConnectionUp(boolean original, @Local BlockGetter level, @Local(ordinal = 1) BlockPos relativePos){
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
            target = "Lnet/minecraft/world/level/block/state/BlockState;canRedstoneConnectTo(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/Direction;)Z",
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
            target = "Lnet/minecraft/world/level/block/state/BlockState;canRedstoneConnectTo(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/Direction;)Z"
        )
    )
    private boolean checkConnectionSide(boolean original, @Local BlockGetter level, @Local(ordinal = 1) BlockPos neighborPos, @Local BlockState neighborState){
        if(original)
            return true;
        if(!StickyRedstoneWireEvaluator.isStickyRedstoneWire(neighborState.getBlock()))
            return false;
        return StickyRedstoneWireEvaluator.getConnections(level, neighborPos, neighborState, Direction.DOWN).isPresent();
    }

    @ModifyExpressionValue(
        method = "calculateTargetStrength",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/level/block/RedStoneWireBlock;getWireSignal(Lnet/minecraft/world/level/block/state/BlockState;)I",
            ordinal = 0
        )
    )
    private int checkDirectNeighbor(int original, @Local(ordinal = 1) BlockPos neighborPos, @Local BlockState neighborState, @Local Level level, @Local Direction neighborDirection){
        if(!StickyRedstoneWireEvaluator.isStickyRedstoneWire(neighborState.getBlock()))
            return original;
        FaceState neighborConnections = StickyRedstoneWireEvaluator.getConnections(level, neighborPos, neighborState, Direction.DOWN);
        if(neighborConnections.isPresent())
            return neighborConnections.power();
        return 0;
    }

    @ModifyExpressionValue(
        method = "calculateTargetStrength",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/level/block/RedStoneWireBlock;getWireSignal(Lnet/minecraft/world/level/block/state/BlockState;)I",
            ordinal = 1
        )
    )
    private int checkDiagonallyAboveNeighbor(int original, @Local(ordinal = 1) BlockPos neighborPos, @Local Level level, @Local Direction neighborDirection){
        neighborPos = neighborPos.above();
        BlockState neighborState = level.getBlockState(neighborPos);
        if(!StickyRedstoneWireEvaluator.isStickyRedstoneWire(neighborState.getBlock()))
            return original;
        FaceState neighborConnections = StickyRedstoneWireEvaluator.getConnections(level, neighborPos, neighborState, Direction.DOWN);
        if(neighborConnections.isPresent())
            return neighborConnections.power();
        return 0;
    }

    @ModifyExpressionValue(
        method = "calculateTargetStrength",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/level/block/RedStoneWireBlock;getWireSignal(Lnet/minecraft/world/level/block/state/BlockState;)I",
            ordinal = 2
        )
    )
    private int checkDiagonallyBelowNeighbor(int original, @Local(ordinal = 1) BlockPos neighborPos, @Local Level level, @Local Direction neighborDirection){
        neighborPos = neighborPos.below();
        BlockState neighborState = level.getBlockState(neighborPos);
        if(!StickyRedstoneWireEvaluator.isStickyRedstoneWire(neighborState.getBlock()))
            return original;
        FaceState neighborConnections = StickyRedstoneWireEvaluator.getConnections(level, neighborPos, neighborState, neighborDirection.getOpposite());
        if(neighborConnections.isPresent())
            return neighborConnections.power();
        return 0;
    }
}
