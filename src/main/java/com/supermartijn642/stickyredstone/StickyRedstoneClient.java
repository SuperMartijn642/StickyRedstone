package com.supermartijn642.stickyredstone;

import com.supermartijn642.core.registry.ClientRegistrationHandler;
import com.supermartijn642.stickyredstone.content.wire.DenseStickyRedstoneDustBlockStateModel;
import com.supermartijn642.stickyredstone.content.wire.SingleStickyRedstoneDust;
import com.supermartijn642.stickyredstone.content.wire.SingleStickyRedstoneDustBlockStateModel;
import com.supermartijn642.stickyredstone.content.wire.StickyRedstoneWireEvaluator;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.BlockColorRegistry;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.RedstoneWireBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;
import java.util.stream.IntStream;

/**
 * Created 7/1/2021 by SuperMartijn642
 */
public class StickyRedstoneClient implements ClientModInitializer {

    @Override
    public void onInitializeClient(){
        ClientRegistrationHandler handler = ClientRegistrationHandler.get(StickyRedstone.MODID);
        handler.registerBlockStateModelOverwrite(() -> StickyRedstone.singleStickyRedstoneDust, SingleStickyRedstoneDustBlockStateModel::new);
        handler.registerBlockStateModelOverwrite(() -> StickyRedstone.denseStickyRedstoneDust, DenseStickyRedstoneDustBlockStateModel::new);
        List<BlockTintSource> tintSources = IntStream.range(0, 6).<BlockTintSource>mapToObj(i -> new BlockTintSource() {
            final Direction face = Direction.values()[i];

            @Override
            public int color(BlockState state){
                int power = 0;
                if(state.is(StickyRedstone.singleStickyRedstoneDust) && state.getValue(SingleStickyRedstoneDust.FACE) == this.face)
                    power = state.getValue(SingleStickyRedstoneDust.POWER);
                return RedstoneWireBlock.getColorForPower(power);
            }

            @Override
            public int colorInWorld(BlockState state, BlockAndTintGetter level, BlockPos pos){
                return RedstoneWireBlock.getColorForPower(StickyRedstoneWireEvaluator.getConnections(level, pos, state, this.face).power());
            }
        }).toList();
        BlockColorRegistry.register(tintSources, StickyRedstone.singleStickyRedstoneDust, StickyRedstone.denseStickyRedstoneDust);
    }
}
