package com.supermartijn642.stickyredstone;

import com.supermartijn642.core.registry.ClientRegistrationHandler;
import com.supermartijn642.stickyredstone.content.wire.DenseStickyRedstoneDustBlockStateModel;
import com.supermartijn642.stickyredstone.content.wire.SingleStickyRedstoneDust;
import com.supermartijn642.stickyredstone.content.wire.SingleStickyRedstoneDustBlockStateModel;
import com.supermartijn642.stickyredstone.content.wire.StickyRedstoneWireEvaluator;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.minecraft.client.color.block.BlockColor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.RedStoneWireBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

/**
 * Created 7/1/2021 by SuperMartijn642
 */
public class StickyRedstoneClient implements ClientModInitializer {

    @Override
    public void onInitializeClient(){
        ClientRegistrationHandler handler = ClientRegistrationHandler.get(StickyRedstone.MODID);
        handler.registerBlockModelTranslucentRenderType(() -> StickyRedstone.stickyRedstoneTorch);
        handler.registerBlockModelTranslucentRenderType(() -> StickyRedstone.singleStickyRedstoneDust);
        handler.registerBlockModelTranslucentRenderType(() -> StickyRedstone.denseStickyRedstoneDust);
        handler.registerBlockModelTranslucentRenderType(() -> StickyRedstone.stickyRepeater);
        handler.registerBlockModelTranslucentRenderType(() -> StickyRedstone.stickyComparator);
        handler.registerBlockModelOverwrite(() -> StickyRedstone.singleStickyRedstoneDust, SingleStickyRedstoneDustBlockStateModel::new);
        handler.registerBlockModelOverwrite(() -> StickyRedstone.denseStickyRedstoneDust, DenseStickyRedstoneDustBlockStateModel::new);
        BlockColor tintSource = new BlockColor() {
            final Direction[] faces = Direction.values();

            @Override
            public int getColor(BlockState state, @Nullable BlockAndTintGetter level, @Nullable BlockPos pos, int index){
                if(index < 0 || index >= 6)
                    return -1;
                Direction face = this.faces[index];
                if(level != null && pos != null)
                    return RedStoneWireBlock.getColorForPower(StickyRedstoneWireEvaluator.getConnections(level, pos, state, face).power());
                int power = 0;
                if(state.is(StickyRedstone.singleStickyRedstoneDust) && state.getValue(SingleStickyRedstoneDust.FACE) == face)
                    power = state.getValue(SingleStickyRedstoneDust.POWER);
                return RedStoneWireBlock.getColorForPower(power);
            }
        };
        ColorProviderRegistry.BLOCK.register(tintSource, StickyRedstone.singleStickyRedstoneDust, StickyRedstone.denseStickyRedstoneDust);
    }
}
