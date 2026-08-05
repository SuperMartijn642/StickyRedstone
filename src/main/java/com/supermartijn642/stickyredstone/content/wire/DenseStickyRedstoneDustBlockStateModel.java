package com.supermartijn642.stickyredstone.content.wire;

import com.supermartijn642.core.ClientUtils;
import com.supermartijn642.stickyredstone.StickyRedstone;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadEmitter;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.BlockStateModelSet;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.Predicate;

/**
 * Created 24/07/2026 by SuperMartijn642
 */
public class DenseStickyRedstoneDustBlockStateModel implements BlockStateModel {

    private final BlockStateModel dummyModel;

    public DenseStickyRedstoneDustBlockStateModel(BlockStateModel original){
        this.dummyModel = original;
    }

    private Iterable<BlockStateModel> models(BlockGetter level, BlockPos pos, BlockState state){
        DenseStickyRedstoneDustBlockEntity entity = DenseStickyRedstoneDust.getEntity(level, pos);
        if(entity == null)
            return List.of(this.dummyModel);
        DustState connectionState = entity.getState();
        BlockState stateCopy = StickyRedstone.singleStickyRedstoneDust.defaultBlockState();
        BlockStateModelSet models = ClientUtils.getMinecraft().getModelManager().getBlockStateModelSet();
        return () -> new Iterator<>() {
            private int index = 0;
            private BlockStateModel next;

            private void getNext(){
                while(this.index < 6){
                    Direction face = Direction.values()[this.index++];
                    FaceState connections = connectionState.getFace(face);
                    if(!connections.isPresent())
                        continue;
                    BlockState correctState = stateCopy.setValue(SingleStickyRedstoneDust.FACE, face).setValue(SingleStickyRedstoneDust.POWER, connections.power());
                    for(int i = 0; i < 4; i++)
                        correctState = correctState.setValue(SingleStickyRedstoneDust.getSideProperty(i), connections.get(i));
                    this.next = models.get(correctState);
                    break;
                }
            }

            @Override
            public boolean hasNext(){
                if(this.next == null && this.index < 6)
                    this.getNext();
                return this.next != null;
            }

            @Override
            public BlockStateModel next(){
                if(this.next == null){
                    if(this.index < 6)
                        this.getNext();
                    if(this.next == null)
                        throw new NoSuchElementException();
                }
                BlockStateModel next = this.next;
                this.next = null;
                return next;
            }
        };
    }

    @Override
    public void emitQuads(QuadEmitter emitter, BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random, Predicate<@Nullable Direction> cullTest){
        for(BlockStateModel model : this.models(level, pos, state))
            model.emitQuads(emitter, level, pos, state, random, cullTest);
    }

    @Override
    public @Nullable Object createGeometryKey(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random){
        List<Object> keys = new ArrayList<>();
        keys.add(this);
        for(BlockStateModel model : this.models(level, pos, state)){
            Object key = model.createGeometryKey(level, pos, state, random);
            if(key == null)
                return null;
            keys.add(key);
        }
        return keys;
    }

    @Override
    public Material.Baked particleMaterial(BlockAndTintGetter level, BlockPos pos, BlockState state){
        for(BlockStateModel model : this.models(level, pos, state))
            return model.particleMaterial(level, pos, state);
        return this.dummyModel.particleMaterial(level, pos, state);
    }

    @Override
    public @BakedQuad.MaterialFlags int materialFlags(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random){
        int flags = 0;
        for(BlockStateModel model : this.models(level, pos, state))
            flags |= model.materialFlags(level, pos, state, random);
        return flags;
    }

    @Override
    public boolean hasMaterialFlag(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random, @BakedQuad.MaterialFlags int flag){
        for(BlockStateModel model : this.models(level, pos, state)){
            if(model.hasMaterialFlag(level, pos, state, random, flag))
                return true;
        }
        return false;
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
