package com.supermartijn642.stickyredstone.content.wire;

import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.DelegateBakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Created 03/08/2026 by SuperMartijn642
 */
public class SingleStickyRedstoneDustBlockStateModel extends DelegateBakedModel {

    public SingleStickyRedstoneDustBlockStateModel(BakedModel wrapped){
        super(wrapped);
    }

    @Override
    public void emitBlockQuads(QuadEmitter emitter, BlockAndTintGetter level, BlockState state, BlockPos pos, Supplier<RandomSource> randomSupplier, Predicate<@Nullable Direction> cullTest){
        emitter.pushTransform(quad -> {
            if(quad.tintIndex() == 0){
                Direction facing = quad.nominalFace();
                if(facing != null)
                    quad.tintIndex(facing.getOpposite().ordinal());
            }
            return true;
        });
        super.emitBlockQuads(emitter, level, state, pos, randomSupplier, cullTest);
        emitter.popTransform();
    }

    @Override
    public void emitItemQuads(QuadEmitter emitter, Supplier<RandomSource> randomSupplier){
        emitter.pushTransform(quad -> {
            if(quad.tintIndex() == 0){
                Direction facing = quad.nominalFace();
                if(facing != null)
                    quad.tintIndex(facing.getOpposite().ordinal());
            }
            return true;
        });
        super.emitItemQuads(emitter, randomSupplier);
        emitter.popTransform();
    }
}
