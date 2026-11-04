package com.supermartijn642.stickyredstone.content.wire;

import com.supermartijn642.core.ClientUtils;
import com.supermartijn642.stickyredstone.StickyRedstone;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.ChunkRenderTypeSet;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.client.model.data.ModelProperty;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created 24/07/2026 by SuperMartijn642
 */
public class DenseStickyRedstoneDustBlockStateModel implements BakedModel {

    private static final ModelProperty<Iterable<SubModel>> MODELS = new ModelProperty<>();

    private final BakedModel dummyModel;

    public DenseStickyRedstoneDustBlockStateModel(BakedModel original){
        this.dummyModel = original;
    }

    private Iterable<SubModel> models(BlockAndTintGetter level, BlockPos pos, BlockState state, ModelData entityData){
        DenseStickyRedstoneDustBlockEntity entity = DenseStickyRedstoneDust.getEntity(level, pos);
        if(entity == null)
            return List.of(new SubModel(this.dummyModel, state, this.dummyModel.getModelData(level, pos, state, entityData)));
        DustState connectionState = entity.getState();
        BlockState stateCopy = StickyRedstone.singleStickyRedstoneDust.defaultBlockState();
        BlockModelShaper modelSet = ClientUtils.getMinecraft().getModelManager().getBlockModelShaper();
        List<SubModel> models = new ArrayList<>(6);
        for(Direction face : Direction.values()){
            FaceState connections = connectionState.getFace(face);
            if(!connections.isPresent())
                continue;
            BlockState correctState = stateCopy.setValue(SingleStickyRedstoneDust.FACE, face).setValue(SingleStickyRedstoneDust.POWER, connections.power());
            for(int i = 0; i < 4; i++)
                correctState = correctState.setValue(SingleStickyRedstoneDust.getSideProperty(i), connections.get(i));
            BakedModel model = modelSet.getBlockModel(correctState);
            models.add(new SubModel(model, correctState, model.getModelData(level, pos, correctState, entityData)));
        }
        return models;
    }

    @Override
    public ModelData getModelData(BlockAndTintGetter level, BlockPos pos, BlockState state, ModelData entityData){
        return ModelData.builder()
            .with(MODELS, this.models(level, pos, state, entityData))
            .build();
    }

    @Override
    public ChunkRenderTypeSet getRenderTypes(BlockState state, RandomSource random, ModelData modelData){
        Iterable<SubModel> models = modelData.get(MODELS);
        if(models == null)
            return this.dummyModel.getRenderTypes(state, random, ModelData.EMPTY);
        ChunkRenderTypeSet layers = ChunkRenderTypeSet.none();
        for(SubModel subModel : models)
            layers = ChunkRenderTypeSet.union(layers, subModel.model.getRenderTypes(subModel.state, random, subModel.modelData));
        return layers;
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction cullDirection, RandomSource random, ModelData modelData, @Nullable RenderType renderType){
        Iterable<SubModel> models = modelData.get(MODELS);
        if(models == null)
            return this.dummyModel.getQuads(state, cullDirection, random, ModelData.EMPTY, renderType);
        List<BakedQuad> quads = new ArrayList<>();
        for(SubModel subModel : models)
            quads.addAll(subModel.model.getQuads(subModel.state, cullDirection, random, subModel.modelData, renderType));
        return quads;
    }

    @Override
    public TextureAtlasSprite getParticleIcon(ModelData modelData){
        Iterable<SubModel> models = modelData.get(MODELS);
        if(models != null){
            for(SubModel subModel : models)
                return subModel.model.getParticleIcon(subModel.modelData);
        }
        return this.dummyModel.getParticleIcon(ModelData.EMPTY);
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction cullDirection, RandomSource random){
        return this.dummyModel.getQuads(state, cullDirection, random);
    }

    @Override
    public TextureAtlasSprite getParticleIcon(){
        return this.dummyModel.getParticleIcon();
    }

    @Override
    public boolean useAmbientOcclusion(){
        return this.dummyModel.useAmbientOcclusion();
    }

    @Override
    public boolean isGui3d(){
        return this.dummyModel.isGui3d();
    }

    @Override
    public boolean usesBlockLight(){
        return this.dummyModel.usesBlockLight();
    }

    @Override
    public boolean isCustomRenderer(){
        return false;
    }

    @Override
    public ItemTransforms getTransforms(){
        return this.dummyModel.getTransforms();
    }

    @Override
    public ItemOverrides getOverrides(){
        return this.dummyModel.getOverrides();
    }

    private record SubModel(BakedModel model, BlockState state, ModelData modelData) {
    }
}
