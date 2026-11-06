package com.supermartijn642.stickyredstone.content.wire;

import com.supermartijn642.core.block.EntityHoldingBlock;
import com.supermartijn642.stickyredstone.StickyRedstone;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

import java.util.Collections;
import java.util.List;

/**
 * Created 24/07/2026 by SuperMartijn642
 */
public class DenseStickyRedstoneDust extends StickyRedstoneDust implements EntityHoldingBlock {

    public static final LootContextParam<Integer> DUST_COUNT = new LootContextParam<>(StickyRedstone.identifier("dust_count"));
    public static final LootContextParamSet DUST_COUNT_PARAM_SET;

    static{
        LootContextParamSet.Builder builder = new LootContextParamSet.Builder();
        for(LootContextParam<?> param : LootContextParamSets.BLOCK.getRequired())
            builder.required(param);
        for(LootContextParam<?> param : LootContextParamSets.BLOCK.getAllowed())
            if(!LootContextParamSets.BLOCK.getRequired().contains(param))
                builder.optional(param);
        builder.required(DUST_COUNT);
        DUST_COUNT_PARAM_SET = builder.build();
    }

    public static final BooleanProperty INITIALIZED = BooleanProperty.create("initialized");

    public static DenseStickyRedstoneDustBlockEntity getEntity(BlockGetter level, BlockPos pos){
        if(StickyRedstone.denseStickyRedstoneDustEntity == null)
            return null;
        return StickyRedstone.denseStickyRedstoneDustEntity.getBlockEntity(level, pos);
    }

    public DenseStickyRedstoneDust(){
        this.registerDefaultState(this.defaultBlockState().setValue(INITIALIZED, true));
    }

    @Override
    public BlockEntity createNewBlockEntity(BlockPos pos, BlockState state){
        return new DenseStickyRedstoneDustBlockEntity(pos, state);
    }

    @Override
    protected FaceState getConnections(BlockGetter level, BlockPos pos, BlockState state, Direction face){
        DenseStickyRedstoneDustBlockEntity entity = getEntity(level, pos);
        return entity == null ? FaceState.ABSENT : entity.getState().getFace(face);
    }

    @Override
    public DustState getState(BlockGetter level, BlockPos pos, BlockState state){
        DenseStickyRedstoneDustBlockEntity entity = getEntity(level, pos);
        return entity == null ? DustState.EMPTY : entity.getState();
    }

    @Override
    public BlockState updateState(BlockGetter level, BlockPos pos, BlockState blockState, DustState newState){
        if(newState.equals(DustState.EMPTY))
            throw new IllegalStateException("Cannot update state to empty!");
        boolean hasMultipleFaces = false;
        Direction presentFace = null;
        for(Direction face : Direction.values()){
            if(newState.getFace(face).isPresent()){
                if(presentFace != null){
                    hasMultipleFaces = true;
                    break;
                }
                presentFace = face;
            }
        }
        assert presentFace != null;
        if(!hasMultipleFaces)
            return StickyRedstone.singleStickyRedstoneDust.getBlockStateForConnections(presentFace, newState.getFace(presentFace));
        DenseStickyRedstoneDustBlockEntity entity = getEntity(level, pos);
        if(entity != null)
            entity.setState(newState);
        return blockState;
    }

    @Override
    protected void updateIndirectNeighbourShapes(BlockState state, LevelAccessor level, BlockPos pos, int updateFlags, int updateLimit){
        // The block entity may not yet be there, updated for a new state, or already removed
        // Hence, we have no way to know which sides actually contain(ed) dust, so just update everything
        BlockPos.MutableBlockPos sideBelowPos = new BlockPos.MutableBlockPos();
        BlockPos.MutableBlockPos sidePos = null;
        for(Direction face : Direction.values()){
            for(Direction side : Direction.values()){
                if(side.getAxis() == face.getAxis())
                    continue;
                sideBelowPos.setWithOffset(pos, side).move(face);
                Block sideBelowBlock = level.getBlockState(sideBelowPos).getBlock();
                if(!StickyRedstoneWireEvaluator.isRedstoneWire(sideBelowBlock))
                    continue;
                if(!StickyRedstoneWireEvaluator.isVanillaRedstoneWire(sideBelowBlock)
                    && !StickyRedstoneWireEvaluator.getConnections(level, sideBelowPos, level.getBlockState(sideBelowPos), side.getOpposite()).isPresent())
                    continue;
                if(sidePos == null)
                    sidePos = new BlockPos.MutableBlockPos();
                if(StickyRedstoneWireEvaluator.isVanillaRedstoneWire(sideBelowBlock) && face == Direction.DOWN){
                    sidePos.setWithOffset(pos, face);
                    level.neighborShapeChanged(side.getOpposite(), level.getBlockState(sidePos), sideBelowPos, sidePos, updateFlags, updateLimit);
                }else{
                    sidePos.setWithOffset(pos, side);
                    level.neighborShapeChanged(face.getOpposite(), level.getBlockState(sidePos), sideBelowPos, sidePos, updateFlags, updateLimit);
                }
            }
        }
    }

    @Override
    public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state){
        return new ItemStack(StickyRedstone.stickyRedstoneDust);
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder params){
        ResourceKey<LootTable> lootTable = this.getLootTable();
        if(lootTable == BuiltInLootTables.EMPTY)
            return Collections.emptyList();
        BlockEntity entity = params.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
        LootParams lootParams = params.withParameter(LootContextParams.BLOCK_STATE, state)
            .withParameter(DUST_COUNT, entity instanceof DenseStickyRedstoneDustBlockEntity ? ((DenseStickyRedstoneDustBlockEntity)entity).getDustCount() : 1)
            .create(DUST_COUNT_PARAM_SET);
        ServerLevel level = lootParams.getLevel();
        LootTable table = level.getServer().reloadableRegistries().getLootTable(lootTable);
        return table.getRandomItems(lootParams);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> builder){
        builder.add(INITIALIZED);
    }
}
