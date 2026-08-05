package com.supermartijn642.stickyredstone.content.wire;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;
import java.util.function.UnaryOperator;

/**
 * Created 24/07/2026 by SuperMartijn642
 */
public class SingleStickyRedstoneDust extends StickyRedstoneDust {

    public static final EnumProperty<Direction> FACE = BlockStateProperties.FACING;
    public static final BooleanProperty SIDE1 = BooleanProperty.create("side1");
    public static final BooleanProperty SIDE2 = BooleanProperty.create("side2");
    public static final BooleanProperty SIDE3 = BooleanProperty.create("side3");
    public static final BooleanProperty SIDE4 = BooleanProperty.create("side4");
    private static final BooleanProperty[] SIDES = {SIDE1, SIDE2, SIDE3, SIDE4};
    public static final IntegerProperty POWER = BlockStateProperties.POWER;

    public static BooleanProperty getSideProperty(Direction face, Direction side){
        return SIDES[FaceState.sideIndex(face, side)];
    }

    public static BooleanProperty getSideProperty(int side){
        return SIDES[side];
    }

    public SingleStickyRedstoneDust(){
        this.registerDefaultState(
            this.defaultBlockState()
                .setValue(FACE, Direction.DOWN)
                .setValue(SIDE1, true).setValue(SIDE2, true).setValue(SIDE3, true).setValue(SIDE4, true)
                .setValue(POWER, 0)
        );
    }

    @Override
    protected FaceState getConnections(BlockGetter level, BlockPos pos, BlockState state, Direction face){
        if(state.getValue(FACE) != face)
            return FaceState.ABSENT;
        return FaceState.get(
            state.getValue(SIDES[0]),
            state.getValue(SIDES[1]),
            state.getValue(SIDES[2]),
            state.getValue(SIDES[3]),
            state.getValue(POWER)
        );
    }

    @Override
    public DustState getState(BlockGetter level, BlockPos pos, BlockState state){
        Direction face = state.getValue(FACE);
        FaceState connections = FaceState.get(
            state.getValue(SIDES[0]),
            state.getValue(SIDES[1]),
            state.getValue(SIDES[2]),
            state.getValue(SIDES[3]),
            state.getValue(POWER)
        );
        return DustState.singleFace(face, connections);
    }

    public BlockState getBlockStateForConnections(Direction face, FaceState connections){
        return this.defaultBlockState()
            .setValue(FACE, face)
            .setValue(SIDE1, connections.get(0))
            .setValue(SIDE2, connections.get(1))
            .setValue(SIDE3, connections.get(2))
            .setValue(SIDE4, connections.get(3))
            .setValue(POWER, connections.power());
    }

    @Override
    public BlockState updateState(BlockGetter level, BlockPos pos, BlockState blockState, DustState newState){
        if(newState.equals(DustState.EMPTY))
            throw new IllegalStateException("Cannot update state to empty!");
        Direction presentFace = null;
        for(Direction face : Direction.values()){
            if(newState.getFace(face).isPresent()){
                if(presentFace != null)
                    throw new AssertionError("Single dust should never be updated to multiple faces!");
                presentFace = face;
            }
        }
        assert presentFace != null;
        return this.getBlockStateForConnections(presentFace, newState.getFace(presentFace));
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context){
        Direction face = context.getClickedFace().getOpposite();
        FaceState connections = this.getProperConnections(
            DustState.EMPTY.setFace(face, FaceState.get(true, true, true, true, 0)),
            context.getLevel(),
            context.getClickedPos(),
            face
        );
        return this.getBlockStateForConnections(face, connections);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> builder){
        super.createBlockStateDefinition(builder);
        builder.add(FACE, SIDE1, SIDE2, SIDE3, SIDE4, POWER);
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror){
        return this.rotateOrMirror(state, mirror::mirror);
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation){
        return this.rotateOrMirror(state, rotation::rotate);
    }

    private BlockState rotateOrMirror(BlockState state, UnaryOperator<Direction> transform){
        Direction oldFace = state.getValue(FACE);
        FaceState oldConnections = this.getConnections(null, null, state, oldFace);
        Direction newFace = transform.apply(oldFace);
        FaceState newConnections = FaceState.get(false, false, false, false, oldConnections.power());
        for(Direction side : Direction.values()){
            if(oldFace.getAxis() == side.getAxis())
                continue;
            if(oldConnections.get(oldFace, side)){
                Direction newSide = transform.apply(side);
                if(newSide.getAxis() != newFace.getAxis())
                    newConnections = newConnections.set(newFace, newSide, true);
            }
        }
        return this.getBlockStateForConnections(newFace, newConnections);
    }
}
