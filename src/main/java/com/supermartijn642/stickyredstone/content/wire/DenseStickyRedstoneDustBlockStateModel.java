package com.supermartijn642.stickyredstone.content.wire;

import com.supermartijn642.core.ClientUtils;
import com.supermartijn642.stickyredstone.StickyRedstone;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Created 24/07/2026 by SuperMartijn642
 */
public class DenseStickyRedstoneDustBlockStateModel implements BakedModel {

    private final BakedModel dummyModel;

    public DenseStickyRedstoneDustBlockStateModel(BakedModel original){
        this.dummyModel = original;
    }

    private Iterable<SubModel> models(BlockGetter level, BlockPos pos, BlockState state){
        DenseStickyRedstoneDustBlockEntity entity = DenseStickyRedstoneDust.getEntity(level, pos);
        if(entity == null)
            return List.of(new SubModel(this.dummyModel, state));
        DustState connectionState = entity.getState();
        BlockState stateCopy = StickyRedstone.singleStickyRedstoneDust.defaultBlockState();
        BlockModelShaper models = ClientUtils.getMinecraft().getModelManager().getBlockModelShaper();
        return () -> new Iterator<>() {
            private int index = 0;
            private boolean hasNext;
            private SubModel subModel = new SubModel();

            private void getNext(){
                while(this.index < 6){
                    Direction face = Direction.values()[this.index++];
                    FaceState connections = connectionState.getFace(face);
                    if(!connections.isPresent())
                        continue;
                    BlockState correctState = stateCopy.setValue(SingleStickyRedstoneDust.FACE, face).setValue(SingleStickyRedstoneDust.POWER, connections.power());
                    for(int i = 0; i < 4; i++)
                        correctState = correctState.setValue(SingleStickyRedstoneDust.getSideProperty(i), connections.get(i));
                    this.subModel.state = correctState;
                    this.subModel.model = models.getBlockModel(correctState);
                    this.hasNext = true;
                    break;
                }
            }

            @Override
            public boolean hasNext(){
                if(!this.hasNext && this.index < 6)
                    this.getNext();
                return this.hasNext;
            }

            @Override
            public SubModel next(){
                if(!this.hasNext){
                    if(this.index < 6)
                        this.getNext();
                    if(!this.hasNext)
                        throw new NoSuchElementException();
                }
                this.hasNext = false;
                return this.subModel;
            }
        };
    }

    @Override
    public boolean isVanillaAdapter(){
        return false;
    }

    @Override
    public void emitBlockQuads(QuadEmitter emitter, BlockAndTintGetter level, BlockState state, BlockPos pos, Supplier<RandomSource> randomSupplier, Predicate<@Nullable Direction> cullTest){
        for(SubModel subModel : this.models(level, pos, state))
            subModel.model.emitBlockQuads(emitter, level, subModel.state, pos, randomSupplier, cullTest);
    }

    @Override
    public void emitItemQuads(QuadEmitter emitter, Supplier<RandomSource> randomSupplier){
        this.dummyModel.emitItemQuads(emitter, randomSupplier); // This should never be relevant
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction direction, RandomSource randomSource){
        return this.dummyModel.getQuads(state, direction, randomSource);
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
    public ItemTransforms getTransforms(){
        return this.dummyModel.getTransforms();
    }

    private static final class SubModel {
        BakedModel model;
        BlockState state;

        SubModel(BakedModel model, BlockState state){
            this.model = model;
            this.state = state;
        }

        SubModel(){
        }
    }
}
