package com.supermartijn642.stickyredstone.content.wire;

import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.QuadCollection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.ModelData;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created 03/08/2026 by SuperMartijn642
 */
public class SingleStickyRedstoneDustBlockStateModel implements BlockStateModel {

    private static final Direction[] CULL_DIRECTIONS = {
        null,
        Direction.UP,
        Direction.DOWN,
        Direction.NORTH,
        Direction.EAST,
        Direction.SOUTH,
        Direction.WEST
    };

    private final BlockStateModel original;
    private final BlockModelPart wrappedPart;

    public SingleStickyRedstoneDustBlockStateModel(BlockStateModel wrapped){
        this.original = wrapped;
        if(wrapped.getClass() == SingleVariant.class && ((SingleVariant)wrapped).model instanceof SimpleModelWrapper part){
            QuadCollection.Builder newQuads = new QuadCollection.Builder();
            for(Direction cullDirection : CULL_DIRECTIONS){
                for(BakedQuad quad : part.quads().getQuads(cullDirection)){
                    BakedQuad newQuad = wrapQuad(quad);
                    if(cullDirection == null)
                        newQuads.addUnculledFace(newQuad);
                    else
                        newQuads.addCulledFace(cullDirection, newQuad);
                }
            }
            this.wrappedPart = new SimpleModelWrapper(
                part.quads(),
                part.useAmbientOcclusion(),
                part.particleIcon(),
                part.layer(),
                part.layerFast()
            );
        }else
            this.wrappedPart = null;
    }

    private static @NonNull BakedQuad wrapQuad(BakedQuad quad){
        if(quad.tintIndex() != 0 || quad.direction() == null)
            return quad;
        int newTintIndex = quad.direction().getOpposite().ordinal();
        return new BakedQuad(
            quad.position0(), quad.position1(), quad.position2(), quad.position3(),
            quad.packedUV0(), quad.packedUV1(), quad.packedUV2(), quad.packedUV3(),
            newTintIndex,
            quad.direction(),
            quad.sprite(),
            quad.shade(),
            quad.lightEmission(),
            quad.ambientOcclusion()
        );
    }

    @Override
    public @NotNull ModelData getModelData(@NotNull BlockAndTintGetter level, @NotNull BlockPos pos, @NotNull BlockState state, @NotNull ModelData modelData){
        if(this.wrappedPart != null)
            return ModelData.EMPTY;
        return this.original.getModelData(level, pos, state, modelData);
    }

    @Override
    public Collection<ChunkSectionLayer> getRenderTypes(@NotNull BlockState state, @NotNull RandomSource random, @NotNull ModelData modelData){
        if(this.wrappedPart != null)
            return this.wrappedPart.layer() == null ? BlockStateModel.super.getRenderTypes(state, random, modelData) : List.of(this.wrappedPart.layer());
        return this.original.getRenderTypes(state, random, modelData);
    }

    @Override
    public void collectParts(RandomSource random, List<BlockModelPart> parts, ModelData modelData, @Nullable ChunkSectionLayer renderType){
        if(this.wrappedPart != null){
            parts.add(this.wrappedPart);
            return;
        }
        List<BlockModelPart> dummyList = new ArrayList<>(1);
        this.original.collectParts(random, dummyList, modelData, renderType);
        for(BlockModelPart part : dummyList)
            parts.add(new Part(part));
    }

    @Override
    public void collectParts(RandomSource random, List<BlockModelPart> parts){
        if(this.wrappedPart != null){
            parts.add(this.wrappedPart);
            return;
        }
        List<BlockModelPart> dummyList = new ArrayList<>(1);
        this.original.collectParts(random, dummyList);
        for(BlockModelPart part : dummyList)
            parts.add(new Part(part));
    }

    @Override
    public TextureAtlasSprite particleIcon(@NotNull ModelData modelData){
        if(this.wrappedPart != null)
            return this.wrappedPart.particleIcon();
        return this.original.particleIcon(modelData);
    }

    @Override
    public TextureAtlasSprite particleIcon(){
        return this.original.particleIcon();
    }

    private record Part(BlockModelPart original) implements BlockModelPart {
        @Override
        public List<BakedQuad> getQuads(@Nullable Direction direction){
            List<BakedQuad> quads = this.original.getQuads(direction);
            List<BakedQuad> newQuads = new ArrayList<>(quads.size());
            for(BakedQuad quad : quads)
                newQuads.add(wrapQuad(quad));
            return newQuads;
        }

        @Override
        public boolean useAmbientOcclusion(){
            return this.original.useAmbientOcclusion();
        }

        @Override
        public TextureAtlasSprite particleIcon(){
            return this.original.particleIcon();
        }

        @Override
        public ChunkSectionLayer layer(){
            return this.original.layer();
        }

        @Override
        public ChunkSectionLayer layerFast(){
            return this.original.layerFast();
        }
    }
}
