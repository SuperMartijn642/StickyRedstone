package com.supermartijn642.stickyredstone.content.wire;

import com.supermartijn642.core.ClientUtils;
import com.supermartijn642.core.util.Pair;
import com.supermartijn642.stickyredstone.StickyRedstone;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.BlockStateModelSet;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.client.model.data.ModelProperty;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Created 24/07/2026 by SuperMartijn642
 */
public class DenseStickyRedstoneDustBlockStateModel implements BlockStateModel {

    private static final ModelProperty<Iterable<Pair<BlockStateModel,ModelData>>> MODELS = new ModelProperty<>();

    private final BlockStateModel dummyModel;

    public DenseStickyRedstoneDustBlockStateModel(BlockStateModel original){
        this.dummyModel = original;
    }

    private Iterable<Pair<BlockStateModel,ModelData>> models(BlockAndTintGetter level, BlockPos pos, BlockState state, ModelData entityData){
        DenseStickyRedstoneDustBlockEntity entity = DenseStickyRedstoneDust.getEntity(level, pos);
        if(entity == null)
            return List.of(Pair.of(this.dummyModel, this.dummyModel.getModelData(level, pos, state, entityData)));
        DustState connectionState = entity.getState();
        BlockState stateCopy = StickyRedstone.singleStickyRedstoneDust.defaultBlockState();
        BlockStateModelSet modelSet = ClientUtils.getMinecraft().getModelManager().getBlockStateModelSet();
        List<Pair<BlockStateModel,ModelData>> models = new ArrayList<>(6);
        for(Direction face : Direction.values()){
            FaceState connections = connectionState.getFace(face);
            if(!connections.isPresent())
                continue;
            BlockState correctState = stateCopy.setValue(SingleStickyRedstoneDust.FACE, face).setValue(SingleStickyRedstoneDust.POWER, connections.power());
            for(int i = 0; i < 4; i++)
                correctState = correctState.setValue(SingleStickyRedstoneDust.getSideProperty(i), connections.get(i));
            BlockStateModel model = modelSet.get(correctState);
            models.add(Pair.of(model, model.getModelData(level, pos, correctState, entityData)));
        }
        return models;
    }

    @Override
    public @NotNull ModelData getModelData(@NotNull BlockAndTintGetter level, @NotNull BlockPos pos, @NotNull BlockState state, @NotNull ModelData entityData){
        return ModelData.builder()
            .with(MODELS, this.models(level, pos, state, entityData))
            .build();
    }

    @Override
    public void collectParts(RandomSource random, List<BlockStateModelPart> parts, ModelData modelData){
        Iterable<Pair<BlockStateModel,ModelData>> models = modelData.get(MODELS);
        if(models == null){
            this.dummyModel.collectParts(random, parts, ModelData.EMPTY);
            return;
        }
        for(Pair<BlockStateModel,ModelData> model : models)
            model.left().collectParts(random, parts, model.right());
    }

    @Override
    public Material.Baked particleMaterial(@NotNull ModelData modelData){
        Iterable<Pair<BlockStateModel,ModelData>> models = modelData.get(MODELS);
        if(models != null){
            for(Pair<BlockStateModel,ModelData> model : models)
                return model.left().particleMaterial(model.right());
        }
        return this.dummyModel.particleMaterial(ModelData.EMPTY);
    }

    @Override
    public void collectParts(RandomSource random, List<BlockStateModelPart> output){
        this.dummyModel.collectParts(random, output);
    }

    @Override
    public Material.Baked particleMaterial(){
        return this.dummyModel.particleMaterial();
    }

    @Override
    public @BakedQuad.MaterialFlags int materialFlags(){
        return this.dummyModel.materialFlags();
    }

    @Override
    public boolean hasMaterialFlag(@BakedQuad.MaterialFlags int flag){
        return this.dummyModel.hasMaterialFlag(flag);
    }
}
