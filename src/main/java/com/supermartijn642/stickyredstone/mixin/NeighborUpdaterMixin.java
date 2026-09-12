package com.supermartijn642.stickyredstone.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.supermartijn642.stickyredstone.StickyRedstone;
import com.supermartijn642.stickyredstone.content.wire.StickyRedstoneDust;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.redstone.NeighborUpdater;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Created 12/09/2026 by SuperMartijn642
 */
@Mixin(NeighborUpdater.class)
public interface NeighborUpdaterMixin {

    @WrapOperation(
        method = "executeShapeUpdate(Lnet/minecraft/world/level/LevelAccessor;Lnet/minecraft/core/Direction;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;II)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/level/block/state/BlockState;updateShape(Lnet/minecraft/world/level/LevelReader;Lnet/minecraft/world/level/ScheduledTickAccess;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/Direction;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/util/RandomSource;)Lnet/minecraft/world/level/block/state/BlockState;"
        )
    )
    private static BlockState executeShapeUpdate(BlockState currentState, LevelReader level, ScheduledTickAccess tickAccess, BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, RandomSource randomSource, Operation<BlockState> operation,
                                                 @Local(ordinal = 0) int updateFlags){
        if(!level.isClientSide() && (updateFlags & 32) == 0 && currentState.getBlock() instanceof StickyRedstoneDust oldBlock){
            int oldFaces = oldBlock.getState(level, pos, currentState).countPresentFaces();
            BlockState newState = operation.call(currentState, level, tickAccess, pos, direction, neighborPos, neighborState, randomSource);
            if(currentState == newState){
                int newFaces = oldBlock.getState(level, pos, currentState).countPresentFaces();
                if(oldFaces > newFaces)
                    Block.popResource((Level)level, pos, new ItemStack(StickyRedstone.singleStickyRedstoneDust, oldFaces - newFaces));
            }
        }
        return operation.call(currentState, level, tickAccess, pos, direction, neighborPos, neighborState, randomSource);
    }
}
