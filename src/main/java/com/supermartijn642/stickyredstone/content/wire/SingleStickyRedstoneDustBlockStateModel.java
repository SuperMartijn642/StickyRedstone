package com.supermartijn642.stickyredstone.content.wire;

import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.QuadCollection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.util.TriState;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.DelegateBlockStateModel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created 03/08/2026 by SuperMartijn642
 */
public class SingleStickyRedstoneDustBlockStateModel extends DelegateBlockStateModel {

    private static final Direction[] CULL_DIRECTIONS = {
        null,
        Direction.UP,
        Direction.DOWN,
        Direction.NORTH,
        Direction.EAST,
        Direction.SOUTH,
        Direction.WEST
    };

    private final BlockModelPart wrappedPart;

    public SingleStickyRedstoneDustBlockStateModel(BlockStateModel wrapped){
        super(wrapped);
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
                newQuads.build(),
                part.useAmbientOcclusion(),
                part.particleIcon(),
                part.renderType()
            );
        }else
            this.wrappedPart = null;
    }

    private static @NotNull BakedQuad wrapQuad(BakedQuad quad){
        if(quad.tintIndex() != 0 || quad.direction() == null)
            return quad;
        int newTintIndex = quad.direction().getOpposite().ordinal();
        return new BakedQuad(
            quad.vertices(),
            newTintIndex,
            quad.direction(),
            quad.sprite(),
            quad.shade(),
            quad.lightEmission(),
            quad.hasAmbientOcclusion()
        );
    }

    @Override
    public void collectParts(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random, List<BlockModelPart> parts){
        if(this.wrappedPart != null){
            parts.add(this.wrappedPart);
            return;
        }
        List<BlockModelPart> dummyList = new ArrayList<>(1);
        super.collectParts(level, pos, state, random, dummyList);
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
        super.collectParts(random, dummyList);
        for(BlockModelPart part : dummyList)
            parts.add(new Part(part));
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
        public ChunkSectionLayer getRenderType(BlockState state){
            return this.original.getRenderType(state);
        }

        @Override
        public TriState ambientOcclusion(){
            return this.original.ambientOcclusion();
        }
    }
}
