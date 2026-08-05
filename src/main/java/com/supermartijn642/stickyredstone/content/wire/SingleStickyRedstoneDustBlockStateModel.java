package com.supermartijn642.stickyredstone.content.wire;

import net.fabricmc.fabric.api.client.model.loading.v1.wrapper.WrapperBlockStateModel;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadEmitter;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;

/**
 * Created 03/08/2026 by SuperMartijn642
 */
public class SingleStickyRedstoneDustBlockStateModel extends WrapperBlockStateModel {

    public SingleStickyRedstoneDustBlockStateModel(BlockStateModel wrapped) {
        super(wrapped);
    }

    @Override
    public void emitQuads(QuadEmitter emitter, BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random, Predicate<@Nullable Direction> cullTest){
        emitter.pushTransform(quad -> {
            if(quad.tintIndex() == 0){
                Direction facing = quad.nominalFace();
                if(facing != null)
                    quad.tintIndex(facing.getOpposite().ordinal());
            }
            return true;
        });
        super.emitQuads(emitter, level, pos, state, random, cullTest);
        emitter.popTransform();
    }
}
