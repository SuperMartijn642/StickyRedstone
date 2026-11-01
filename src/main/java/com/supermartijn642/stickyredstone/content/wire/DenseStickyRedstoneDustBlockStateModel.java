package com.supermartijn642.stickyredstone.content.wire;

import com.supermartijn642.core.ClientUtils;
import com.supermartijn642.stickyredstone.StickyRedstone;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

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
        BlockModelShaper models = ClientUtils.getMinecraft().getModelManager().getBlockModelShaper();
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
                    this.next = models.getBlockModel(correctState);
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
    public void collectParts(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random, List<BlockModelPart> parts){
        for(BlockStateModel model : this.models(level, pos, state))
            model.collectParts(level, pos, state, random, parts);
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
    public TextureAtlasSprite particleIcon(BlockAndTintGetter level, BlockPos pos, BlockState state){
        for(BlockStateModel model : this.models(level, pos, state))
            return model.particleIcon(level, pos, state);
        return this.dummyModel.particleIcon(level, pos, state);
    }

    @Override
    public void collectParts(RandomSource random, List<BlockModelPart> output){
        this.dummyModel.collectParts(random, output);
    }

    @Override
    public TextureAtlasSprite particleIcon(){
        return this.dummyModel.particleIcon();
    }
}
