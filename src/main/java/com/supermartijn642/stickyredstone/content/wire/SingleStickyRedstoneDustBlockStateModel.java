package com.supermartijn642.stickyredstone.content.wire;

import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.block.dispatch.SingleVariant;
import net.minecraft.client.resources.model.SimpleModelWrapper;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.geometry.QuadCollection;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.util.TriState;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.DelegateBlockStateModel;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

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

    private final BlockStateModelPart wrappedPart;

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
                part.quads(),
                part.useAmbientOcclusion(),
                part.particleMaterial()
            );
        }else
            this.wrappedPart = null;
    }

    private static @NonNull BakedQuad wrapQuad(BakedQuad quad){
        BakedQuad.MaterialInfo materialInfo = quad.materialInfo();
        if(materialInfo.tintIndex() != 0 || quad.direction() == null)
            return quad;
        int newTintIndex = quad.direction().getOpposite().ordinal();
        return new BakedQuad(
            quad.position0(), quad.position1(), quad.position2(), quad.position3(),
            quad.packedUV0(), quad.packedUV1(), quad.packedUV2(), quad.packedUV3(),
            quad.direction(),
            new BakedQuad.MaterialInfo(
                materialInfo.sprite(),
                materialInfo.layer(),
                materialInfo.itemRenderType(),
                materialInfo.itemGlintRenderType(),
                materialInfo.itemGlintSpecialRenderType(),
                newTintIndex,
                materialInfo.shadeDirectionOverride(),
                materialInfo.lightEmission(),
                materialInfo.ambientOcclusion()
            ),
            quad.bakedNormals(),
            quad.bakedColors()
        );
    }

    @Override
    public void collectParts(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random, List<BlockStateModelPart> parts){
        if(this.wrappedPart != null){
            parts.add(this.wrappedPart);
            return;
        }
        List<BlockStateModelPart> dummyList = new ArrayList<>(1);
        super.collectParts(level, pos, state, random, dummyList);
        for(BlockStateModelPart part : dummyList)
            parts.add(new Part(part));
    }

    @Override
    public void collectParts(RandomSource random, List<BlockStateModelPart> parts){
        if(this.wrappedPart != null){
            parts.add(this.wrappedPart);
            return;
        }
        List<BlockStateModelPart> dummyList = new ArrayList<>(1);
        super.collectParts(random, dummyList);
        for(BlockStateModelPart part : dummyList)
            parts.add(new Part(part));
    }

    private record Part(BlockStateModelPart original) implements BlockStateModelPart {
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
        public Material.Baked particleMaterial(){
            return this.original.particleMaterial();
        }

        @Override
        public @BakedQuad.MaterialFlags int materialFlags(){
            return this.original.materialFlags();
        }

        @Override
        public TriState ambientOcclusion(){
            return this.original.ambientOcclusion();
        }
    }
}
