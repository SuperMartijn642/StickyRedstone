package com.supermartijn642.stickyredstone.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.supermartijn642.stickyredstone.content.StickyRepeater;
import com.supermartijn642.stickyredstone.content.wire.StickyRedstoneWireEvaluator;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.SignalGetter;
import net.minecraft.world.level.block.DiodeBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Created 04/08/2026 by SuperMartijn642
 */
@Mixin(DiodeBlock.class)
public class DiodeBlockMixin {

    @WrapOperation(
        method = "getInputSignal",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/level/Level;getSignal(Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/Direction;)I"
        )
    )
    private int getInputSignal(Level level, BlockPos pos, Direction oppositeOfSideOfBlock, Operation<Integer> operation){
        BlockState state = level.getBlockState(pos);
        if(StickyRedstoneWireEvaluator.isStickyRedstoneWire(state.getBlock())){
            if(!StickyRedstoneWireEvaluator.getConnections(level, pos, state, Direction.DOWN).isPresent())
                return 0;
        }
        if(StickyRepeater.isStickyDiode(state)){
            if(StickyRepeater.getDiodeFace(state) != Direction.DOWN)
                return 0;
        }
        return operation.call(level, pos, oppositeOfSideOfBlock);
    }

    @WrapOperation(
        method = "getAlternateSignal",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/level/SignalGetter;getControlInputSignal(Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/Direction;Z)I"
        ),
        expect = 2
    )
    private int getAlternateSignal(SignalGetter level, BlockPos pos, Direction oppositeOfSideOfBlock, boolean onlyDiodes, Operation<Integer> operation){
        BlockState state = level.getBlockState(pos);
        if(StickyRedstoneWireEvaluator.isStickyRedstoneWire(state.getBlock())){
            if(!StickyRedstoneWireEvaluator.getConnections(level, pos, state, Direction.DOWN).isPresent())
                return 0;
        }
        if(StickyRepeater.isStickyDiode(state)){
            if(StickyRepeater.getDiodeFace(state) != Direction.DOWN)
                return 0;
            return state.getDirectSignal(level, pos, oppositeOfSideOfBlock);
        }
        return operation.call(level, pos, oppositeOfSideOfBlock, onlyDiodes);
    }
}
