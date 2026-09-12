package com.supermartijn642.stickyredstone.mixin;

import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalIntRef;
import com.supermartijn642.stickyredstone.StickyRedstone;
import com.supermartijn642.stickyredstone.content.wire.StickyRedstoneDust;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Created 12/09/2026 by SuperMartijn642
 */
@Mixin(Block.class)
public class BlockMixin {

    @Inject(
        method = "updateOrDestroy(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/LevelAccessor;Lnet/minecraft/core/BlockPos;II)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/level/LevelAccessor;setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;II)Z",
            shift = At.Shift.BEFORE
        )
    )
    private static void updateOrDestroyPre(BlockState oldState, BlockState newState, LevelAccessor level, BlockPos pos, @Block.UpdateFlags int updateFlags, int updateLimit, CallbackInfo ci, @Share("numFaces") LocalIntRef numFaces){
        if(!level.isClientSide() && (updateFlags & 32) == 0 && oldState.getBlock() instanceof StickyRedstoneDust oldBlock && newState.getBlock() instanceof StickyRedstoneDust)
            numFaces.set(oldBlock.getState(level, pos, oldState).countPresentFaces());
    }

    @Inject(
        method = "updateOrDestroy(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/LevelAccessor;Lnet/minecraft/core/BlockPos;II)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/level/LevelAccessor;setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;II)Z",
            shift = At.Shift.AFTER
        )
    )
    private static void updateOrDestroyPost(BlockState oldState, BlockState newState, LevelAccessor level, BlockPos pos, @Block.UpdateFlags int updateFlags, int updateLimit, CallbackInfo ci, @Share("numFaces") LocalIntRef numFaces){
        int oldFaces = numFaces.get();
        if(oldFaces > 0 && newState.getBlock() instanceof StickyRedstoneDust newBlock && level instanceof Level){
            int newFaces = newBlock.getState(level, pos, newState).countPresentFaces();
            if(oldFaces > newFaces)
                Block.popResource((Level)level, pos, new ItemStack(StickyRedstone.singleStickyRedstoneDust, oldFaces - newFaces));
        }
    }
}
