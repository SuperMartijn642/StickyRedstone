package com.supermartijn642.stickyredstone.content.wire;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.SimpleBakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.BakedModelWrapper;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Created 03/08/2026 by SuperMartijn642
 */
public class SingleStickyRedstoneDustBlockStateModel extends BakedModelWrapper<BakedModel> {

    private final List<BakedQuad> unculledQuads;
    private final Map<Direction,List<BakedQuad>> culledQuads;

    public SingleStickyRedstoneDustBlockStateModel(BakedModel wrapped){
        super(wrapped);
        if(wrapped.getClass() == SimpleBakedModel.class){
            RandomSource random = RandomSource.create();
            this.culledQuads = new EnumMap<>(Direction.class);
            for(Direction cullDirection : Direction.values()){
                List<BakedQuad> quads = new ArrayList<>();
                for(BakedQuad quad : wrapped.getQuads(Blocks.AIR.defaultBlockState(), cullDirection, random))
                    quads.add(wrapQuad(quad));
                this.culledQuads.put(cullDirection, List.copyOf(quads));
            }
            List<BakedQuad> unculledQuads = new ArrayList<>();
            for(BakedQuad quad : wrapped.getQuads(Blocks.AIR.defaultBlockState(), null, random))
                unculledQuads.add(wrapQuad(quad));
            this.unculledQuads = List.copyOf(unculledQuads);
        }else{
            this.unculledQuads = null;
            this.culledQuads = null;
        }
    }

    private static @NotNull BakedQuad wrapQuad(BakedQuad quad){
        if(quad.getTintIndex() != 0 || quad.getDirection() == null)
            return quad;
        int newTintIndex = quad.getDirection().getOpposite().ordinal();
        return new BakedQuad(
            quad.getVertices(),
            newTintIndex,
            quad.getDirection(),
            quad.getSprite(),
            quad.isShade(),
            quad.hasAmbientOcclusion()
        );
    }

    @Override
    public ModelData getModelData(BlockAndTintGetter level, BlockPos pos, BlockState state, ModelData modelData){
        if(this.unculledQuads != null)
            return ModelData.EMPTY;
        return super.getModelData(level, pos, state, modelData);
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction cullDirection, RandomSource random, ModelData modelData, @Nullable RenderType renderType){
        if(this.unculledQuads != null){
            return cullDirection == null ?
                this.unculledQuads :
                this.culledQuads.get(cullDirection);
        }
        List<BakedQuad> quads = new ArrayList<>(super.getQuads(state, cullDirection, random, modelData, renderType));
        quads.replaceAll(SingleStickyRedstoneDustBlockStateModel::wrapQuad);
        return quads;
    }
}
