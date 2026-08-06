package com.supermartijn642.stickyredstone.content.wire;

import com.supermartijn642.core.item.ItemProperties;
import com.supermartijn642.stickyredstone.StickyRedstone;
import com.supermartijn642.stickyredstone.content.StickBlockItem;
import net.minecraft.advancements.triggers.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;

/**
 * Created 03/08/2026 by SuperMartijn642
 */
public class StickyRedstoneDustItem extends StickBlockItem {

    public StickyRedstoneDustItem(Block block, ItemProperties properties){
        super(block, properties);
    }

    @Override
    public InteractionResult place(BlockPlaceContext context){
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState blockState = level.getBlockState(pos);
        if(!StickyRedstoneWireEvaluator.isStickyRedstoneWire(blockState.getBlock()))
            return super.place(context);
        // Check if dust can be added on clicked face
        StickyRedstoneDust block = (StickyRedstoneDust)blockState.getBlock();
        DustState state = block.getState(level, pos, blockState);
        Direction face = context.getClickedFace().getOpposite();
        if(state.getFace(face).isPresent())
            return InteractionResult.FAIL;
        BlockPos supportPos = pos.relative(face);
        BlockState supportState = level.getBlockState(supportPos);
        if(!StickyRedstoneDust.canSurviveOn(level, supportPos, supportState, face.getOpposite()))
            return InteractionResult.FAIL;
        // Place the dust
        boolean wasSingleDust = block == StickyRedstone.singleStickyRedstoneDust;
        block = StickyRedstone.denseStickyRedstoneDust;
        blockState = block.defaultBlockState().setValue(DenseStickyRedstoneDust.INITIALIZED, false);
        if(!level.setBlock(pos, blockState, Block.UPDATE_CLIENTS | (wasSingleDust ? 0 : Block.UPDATE_KNOWN_SHAPE)))
            return InteractionResult.FAIL;
        FaceState connections = block.getProperConnections(state, level, pos, face);
        BlockState newBlockState = block.updateState(level, pos, blockState, state.setFace(face, connections));
        newBlockState = block.updateShape(newBlockState, level, level, pos, face, supportPos, supportState, level.getRandom());
        newBlockState = newBlockState.setValue(DenseStickyRedstoneDust.INITIALIZED, true); // Need this dumb property just for the game to let me update everything
        level.setBlock(pos, newBlockState, Block.UPDATE_ALL | Block.UPDATE_IMMEDIATE);
        block.updatePowerStrength(level, pos, newBlockState, face, null, true);
        // Updates post placement
        ItemStack stack = context.getItemInHand();
        Player player = context.getPlayer();
        if(player instanceof ServerPlayer)
            CriteriaTriggers.PLACED_BLOCK.trigger((ServerPlayer)player, pos, stack);
        SoundType soundType = newBlockState.getSoundType();
        level.playSound(player, pos, this.getPlaceSound(newBlockState), SoundSource.BLOCKS, (soundType.getVolume() + 1) / 2, soundType.getPitch() * 0.8f);
        level.gameEvent(GameEvent.BLOCK_PLACE, pos, GameEvent.Context.of(player, newBlockState));
        stack.consume(1, player);
        return InteractionResult.SUCCESS;
    }
}
