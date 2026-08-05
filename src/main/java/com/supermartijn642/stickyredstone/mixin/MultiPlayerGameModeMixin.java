package com.supermartijn642.stickyredstone.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.supermartijn642.stickyredstone.StickyRedstone;
import com.supermartijn642.stickyredstone.content.wire.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Created 04/08/2026 by SuperMartijn642
 */
@Mixin(MultiPlayerGameMode.class)
public class MultiPlayerGameModeMixin {

    @Final
    @Shadow
    private Minecraft minecraft;

    @Inject(
        method = "destroyBlock",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/level/block/Block;playerWillDestroy(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/entity/player/Player;)Lnet/minecraft/world/level/block/state/BlockState;",
            shift = At.Shift.BEFORE
        )
    )
    private void destroyBlock(BlockPos pos, CallbackInfoReturnable<Boolean> ci, @Local Level level, @Local(ordinal = 0) BlockState oldState, @Share("face") LocalRef<Direction> sharedFace) {
        if(!StickyRedstoneWireEvaluator.isStickyRedstoneWire(oldState.getBlock()))
            return;
        if(!(this.minecraft.hitResult instanceof BlockHitResult hitResult))
            return;
        // Find the face that was clicked
        Vec3 location = hitResult.getLocation();
        Direction hitFace;
        do{
            hitFace = Direction.getApproximateNearest(
                ((location.x % 1) + 1) % 1 - 0.5,
                ((location.y % 1) + 1) % 1 - 0.5,
                ((location.z % 1) + 1) % 1 - 0.5
            );
            if(StickyRedstoneWireEvaluator.getConnections(level, pos, oldState, hitFace).isPresent())
                break;
            location = new Vec3(
                hitFace.getAxis() == Direction.Axis.X ? 0.5 : location.x,
                hitFace.getAxis() == Direction.Axis.Y ? 0.5 : location.y,
                hitFace.getAxis() == Direction.Axis.Z ? 0.5 : location.z
            );
            if(location.x == 0.5 && location.y == 0.5 && location.z == 0.5)
                return;
        }while(true);
        // Check if there are other faces besides the clicked one
        boolean hasOtherFaces = false;
        for(Direction face : Direction.values()){
            if(face == hitFace)
                continue;
            if(StickyRedstoneWireEvaluator.getConnections(level, pos, oldState, face).isPresent()){
                hasOtherFaces = true;
                break;
            }
        }
        if(!hasOtherFaces)
            return;
        // Set the face
        sharedFace.set(hitFace);
        StickyRedstoneDust.DESTROY_FACE_OVERRIDE.set(hitFace);
    }

    @WrapOperation(
        method = "destroyBlock",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/level/block/state/BlockState;onDestroyedByPlayer(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/item/ItemStack;ZLnet/minecraft/world/level/material/FluidState;)Z"
        )
    )
    private boolean destroyBlock(BlockState oldState, Level level, BlockPos pos, Player player, ItemStack tool, boolean willHarvest, FluidState fluid, Operation<Boolean> operation, @Share("face") LocalRef<Direction> sharedFace) {
        Direction hitFace = sharedFace.get();
        if(hitFace == null)
            return operation.call(oldState, level, pos, player, tool, willHarvest, fluid);
        // Check if there are other faces besides the clicked one
        int otherFaces = 0;
        Direction otherFace = null;
        for(Direction face : Direction.values()){
            if(face == hitFace)
                continue;
            if(StickyRedstoneWireEvaluator.getConnections(level, pos, oldState, face).isPresent()){
                otherFaces++;
                otherFace = face;
            }
        }
        if(otherFaces == 0)
            throw new AssertionError();
        // Remove just the clicked face and keep the others
        StickyRedstoneDust block = (StickyRedstoneDust)oldState.getBlock();
        if(otherFaces == 1){
            FaceState otherFaceConnections = StickyRedstoneWireEvaluator.getConnections(level, pos, oldState, otherFace);
            BlockState newBlockState = StickyRedstone.singleStickyRedstoneDust.getBlockStateForConnections(otherFace, otherFaceConnections);
            BlockPos supportPos = pos.relative(hitFace);
            newBlockState = newBlockState.updateShape(level, level, pos, hitFace, supportPos, level.getBlockState(supportPos), level.getRandom());
            return level.setBlock(pos, newBlockState, Block.UPDATE_ALL | Block.UPDATE_IMMEDIATE);
        }else{
            DustState state = block.getState(level, pos, oldState);
            BlockPos supportPos = pos.relative(hitFace);
            BlockState newBlockState = block.defaultBlockState().setValue(DenseStickyRedstoneDust.INITIALIZED, false); // Need this dumb property just for the game to let me update everything
            if(!level.setBlock(pos, newBlockState, 0))
                return false;
            newBlockState = block.updateState(level, pos, oldState, state.setFace(hitFace, FaceState.ABSENT));
            newBlockState = block.updateShape(newBlockState, level, level, pos, hitFace, supportPos, level.getBlockState(supportPos), level.getRandom());
            newBlockState = newBlockState.setValue(DenseStickyRedstoneDust.INITIALIZED, true);
            return level.setBlock(pos, newBlockState, Block.UPDATE_ALL | Block.UPDATE_IMMEDIATE);
        }
    }

    @Inject(
        method = "continueDestroyBlock",
        at = {
            @At("HEAD"),
            @At("RETURN")
        }
    )
    private void continueDestroyBlock(BlockPos pos, Direction direction, CallbackInfoReturnable<Boolean> ci) {
        StickyRedstoneDust.DESTROY_FACE_OVERRIDE.remove();
    }
}
