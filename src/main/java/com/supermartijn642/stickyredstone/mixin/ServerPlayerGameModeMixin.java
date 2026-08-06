package com.supermartijn642.stickyredstone.mixin;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.supermartijn642.stickyredstone.StickyRedstone;
import com.supermartijn642.stickyredstone.content.wire.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Created 04/08/2026 by SuperMartijn642
 */
@Mixin(ServerPlayerGameMode.class)
public class ServerPlayerGameModeMixin {

    @Shadow
    private ServerLevel level;

    @WrapOperation(
        method = "destroyBlock",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/server/level/ServerPlayerGameMode;removeBlock(Lnet/minecraft/core/BlockPos;Z)Z"
        ),
        expect = 2
    )
    private boolean destroyBlock(ServerPlayerGameMode gameMode, BlockPos pos, boolean canHarvest, Operation<Boolean> operation, @Local(ordinal = 0) BlockState oldState) {
        if(!StickyRedstoneWireEvaluator.isStickyRedstoneWire(oldState.getBlock()))
            return operation.call(gameMode, pos, canHarvest);
        Direction hitFace = StickyRedstoneDust.DESTROY_FACE_OVERRIDE.get();
        if(hitFace == null)
            return operation.call(gameMode, pos, canHarvest);
        if(!StickyRedstoneWireEvaluator.getConnections(this.level, pos, oldState, hitFace).isPresent())
            return operation.call(gameMode, pos, canHarvest);
        // Check if there are other faces besides the clicked one
        int otherFaces = 0;
        Direction otherFace = null;
        for(Direction face : Direction.values()){
            if(face == hitFace)
                continue;
            if(StickyRedstoneWireEvaluator.getConnections(this.level, pos, oldState, face).isPresent()){
                otherFaces++;
                otherFace = face;
            }
        }
        if(otherFaces == 0)
            return operation.call(gameMode, pos, canHarvest);
        // Remove just the clicked face and keep the others
        StickyRedstoneDust block = (StickyRedstoneDust)oldState.getBlock();
        if(otherFaces == 1){
            FaceState otherFaceConnections = StickyRedstoneWireEvaluator.getConnections(this.level, pos, oldState, otherFace);
            BlockState newBlockState = StickyRedstone.singleStickyRedstoneDust.getBlockStateForConnections(otherFace, otherFaceConnections);
            BlockPos supportPos = pos.relative(hitFace);
            newBlockState = newBlockState.updateShape(this.level, this.level, pos, hitFace, supportPos, this.level.getBlockState(supportPos), this.level.getRandom());
            return this.level.setBlock(pos, newBlockState, Block.UPDATE_ALL | Block.UPDATE_IMMEDIATE);
        }else{
            DustState state = block.getState(this.level, pos, oldState);
            BlockPos supportPos = pos.relative(hitFace);
            BlockState newBlockState = block.defaultBlockState().setValue(DenseStickyRedstoneDust.INITIALIZED, false); // Need this dumb property just for the game to let me update everything
            if(!this.level.setBlock(pos, newBlockState, 0))
                return false;
            newBlockState = block.updateState(this.level, pos, oldState, state.setFace(hitFace, FaceState.ABSENT));
            newBlockState = block.updateShape(newBlockState, this.level, this.level, pos, hitFace, supportPos, this.level.getBlockState(supportPos), this.level.getRandom());
            newBlockState = newBlockState.setValue(DenseStickyRedstoneDust.INITIALIZED, true);
            return this.level.setBlock(pos, newBlockState, Block.UPDATE_ALL | Block.UPDATE_IMMEDIATE);
        }
    }

    @WrapWithCondition(
        method = "destroyBlock",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/level/block/Block;playerDestroy(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/block/entity/BlockEntity;Lnet/minecraft/world/item/ItemStack;)V"
        )
    )
    private boolean destroyBlock(Block block, Level level, Player player, BlockPos pos, BlockState state, BlockEntity entity, ItemStack stack) {
        if(!StickyRedstoneWireEvaluator.isStickyRedstoneWire(block))
            return true;
        player.awardStat(Stats.BLOCK_MINED.get(block));
        player.causeFoodExhaustion(0.005f);
        Block.dropResources(StickyRedstone.singleStickyRedstoneDust.defaultBlockState(), level, pos, entity, player, stack);
        return false;
    }

    @Inject(
        method = "handleBlockBreakAction",
        at = @At("HEAD")
    )
    private void handleBlockBreakAction(BlockPos pos, ServerboundPlayerActionPacket.Action action, Direction direction, int maxY, int sequence, CallbackInfo ci){
        if(action != ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK && action != ServerboundPlayerActionPacket.Action.STOP_DESTROY_BLOCK)
            return;
        if(this.level == null)
            return;
        if(StickyRedstoneWireEvaluator.isRedstoneWire(this.level.getBlockState(pos).getBlock()))
            StickyRedstoneDust.DESTROY_FACE_OVERRIDE.set(direction);
    }
}
