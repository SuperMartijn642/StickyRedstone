package com.supermartijn642.stickyredstone.content.wire;

import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.DelegateBakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.Supplier;

/**
 * Created 03/08/2026 by SuperMartijn642
 */
public class SingleStickyRedstoneDustBlockStateModel extends DelegateBakedModel {

    public SingleStickyRedstoneDustBlockStateModel(BakedModel wrapped){
        super(wrapped);
    }

    @Override
    public void emitBlockQuads(BlockAndTintGetter level, BlockState state, BlockPos pos, Supplier<RandomSource> randomSupplier, RenderContext context){
        context.pushTransform(quad -> {
            if(quad.colorIndex() == 0){
                Direction facing = quad.nominalFace();
                if(facing != null)
                    quad.colorIndex(facing.getOpposite().ordinal());
            }
            return true;
        });
        super.emitBlockQuads(level, state, pos, randomSupplier, context);
        context.popTransform();
    }

    @Override
    public void emitItemQuads(ItemStack stack, Supplier<RandomSource> randomSupplier, RenderContext context){
        context.pushTransform(quad -> {
            if(quad.colorIndex() == 0){
                Direction facing = quad.nominalFace();
                if(facing != null)
                    quad.colorIndex(facing.getOpposite().ordinal());
            }
            return true;
        });
        super.emitItemQuads(stack, randomSupplier, context);
        context.popTransform();
    }
}
