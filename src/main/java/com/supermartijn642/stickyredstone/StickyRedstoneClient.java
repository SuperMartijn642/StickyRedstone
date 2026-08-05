package com.supermartijn642.stickyredstone;

import com.supermartijn642.core.registry.ClientRegistrationHandler;
import com.supermartijn642.stickyredstone.content.wire.*;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.BlockColorRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.BlockTintsFactory;
import net.fabricmc.fabric.api.client.rendering.v1.ColorResolverRegistry;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.client.color.block.BlockTintSources;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.ColorResolver;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.RedStoneWireBlock;
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
                return RedStoneWireBlock.getColorForPower(power);
            }

            @Override
            public int colorInWorld(BlockState state, BlockAndTintGetter level, BlockPos pos){
                return RedStoneWireBlock.getColorForPower(StickyRedstoneWireEvaluator.getConnections(level, pos, state, this.face).power());
            }
        }).toList();
        BlockColorRegistry.register(tintSources, StickyRedstone.singleStickyRedstoneDust, StickyRedstone.denseStickyRedstoneDust);
    }
}
