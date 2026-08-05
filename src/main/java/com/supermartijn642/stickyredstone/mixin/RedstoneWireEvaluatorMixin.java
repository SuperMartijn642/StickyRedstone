package com.supermartijn642.stickyredstone.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.supermartijn642.stickyredstone.content.wire.FaceState;
import com.supermartijn642.stickyredstone.content.wire.StickyRedstoneWireEvaluator;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.redstone.RedstoneWireEvaluator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Created 03/08/2026 by SuperMartijn642
 */
@Mixin(RedstoneWireEvaluator.class)
public class RedstoneWireEvaluatorMixin {

    @ModifyExpressionValue(
        method = "getIncomingWireSignal",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/level/redstone/RedstoneWireEvaluator;getWireSignal(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)I",
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
        method = "getIncomingWireSignal",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/level/redstone/RedstoneWireEvaluator;getWireSignal(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)I",
            ordinal = 1
        )
    )
    private int checkDiagonallyAboveNeighbor(int original, @Local(ordinal = 3) BlockPos neighborPos, @Local Level level, @Local Direction neighborDirection){
        BlockState neighborState = level.getBlockState(neighborPos);
        if(!StickyRedstoneWireEvaluator.isStickyRedstoneWire(neighborState.getBlock()))
            return original;
        FaceState neighborConnections = StickyRedstoneWireEvaluator.getConnections(level, neighborPos, neighborState, Direction.DOWN);
        if(neighborConnections.isPresent())
            return neighborConnections.power();
        return 0;
    }

    @ModifyExpressionValue(
        method = "getIncomingWireSignal",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/level/redstone/RedstoneWireEvaluator;getWireSignal(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)I",
            ordinal = 1
        )
    )
    private int checkDiagonallyBelowNeighbor(int original, @Local(ordinal = 3) BlockPos neighborPos, @Local Level level, @Local Direction neighborDirection){
        BlockState neighborState = level.getBlockState(neighborPos);
        if(!StickyRedstoneWireEvaluator.isStickyRedstoneWire(neighborState.getBlock()))
            return original;
        FaceState neighborConnections = StickyRedstoneWireEvaluator.getConnections(level, neighborPos, neighborState, neighborDirection.getOpposite());
        if(neighborConnections.isPresent())
            return neighborConnections.power();
        return 0;
    }
}
