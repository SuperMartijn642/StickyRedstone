package com.supermartijn642.stickyredstone.content;

import com.supermartijn642.core.block.BaseBlock;
import com.supermartijn642.core.block.BlockProperties;
import com.supermartijn642.stickyredstone.content.wire.StickyRedstoneWireEvaluator;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ComparatorBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.redstone.ExperimentalRedstoneUtils;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.ticks.TickPriority;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Created 04/08/2026 by SuperMartijn642
 */
public class StickyComparator extends BaseBlock {

    public static final EnumProperty<Direction> FACE = BlockStateProperties.FACING;
    public static final IntegerProperty ORIENTATION = StickyRepeater.ORIENTATION;
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    public static final EnumProperty<ComparatorMode> MODE = BlockStateProperties.MODE_COMPARATOR;
    public static final IntegerProperty OUTPUT_SIGNAL = IntegerProperty.create("signal", 0, 15);

    public StickyComparator(){
        super(false, BlockProperties.create().strength(0).sound(SoundType.STONE).pushReaction(PushReaction.DESTROY));
        this.registerDefaultState(
            this.defaultBlockState()
                .setValue(FACE, Direction.DOWN)
                .setValue(ORIENTATION, 0)
                .setValue(POWERED, false)
                .setValue(MODE, ComparatorMode.COMPARE)
                .setValue(OUTPUT_SIGNAL, 0)
        );
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context){
        BlockPos.MutableBlockPos supportPos = new BlockPos.MutableBlockPos();
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Direction face = context.getClickedFace().getOpposite();
        // Find face to place the comparator on
        supportPos.setWithOffset(pos, face);
        if(!StickyRepeater.canSurviveOn(level.getBlockState(supportPos), level, supportPos, face.getOpposite())){
            face = face.getOpposite();
            supportPos.setWithOffset(pos, face);
            if(!StickyRepeater.canSurviveOn(level.getBlockState(supportPos), level, supportPos, face.getOpposite())){
                for(Direction side : Direction.values()){
                    if(side.getAxis() == context.getClickedFace().getAxis())
                        continue;
                    face = side;
                    supportPos.setWithOffset(pos, face);
                    if(StickyRepeater.canSurviveOn(level.getBlockState(supportPos), level, supportPos, face.getOpposite()))
                        break;
                }
                if(!StickyRepeater.canSurviveOn(level.getBlockState(supportPos), level, supportPos, face.getOpposite()))
                    return null;
            }
        }
        // Get orientation
        Direction front = null;
        for(Direction side : context.getNearestLookingDirections()){
            if(side.getAxis() != face.getAxis()){
                front = side;
                break;
            }
        }
        if(front == null)
            return null;
        int orientation = StickyRepeater.toOrientation(face, front);
        // Get proper state
        return this.defaultBlockState()
            .setValue(FACE, face)
            .setValue(ORIENTATION, orientation);
    }

    @Override
    protected InteractionFeedback interact(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, Direction hitSide, Vec3 hitLocation){
        if (!player.getAbilities().mayBuild)
            return InteractionFeedback.PASS;
        state = state.cycle(MODE);
        float pitch = state.getValue(MODE) == ComparatorMode.SUBTRACT ? 0.55f : 0.5f;
        level.playSound(player, pos, SoundEvents.COMPARATOR_CLICK, SoundSource.BLOCKS, 0.3f, pitch);
        level.setBlock(pos, state, Block.UPDATE_CLIENTS);
        this.refreshOutputState(level, pos, state);
        return InteractionFeedback.SUCCESS;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context){
        return StickyRepeater.SHAPES[state.getValue(FACE).ordinal()].getUnderlying();
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> builder){
        builder.add(FACE, ORIENTATION, POWERED, MODE, OUTPUT_SIGNAL);
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos){
        Direction face = state.getValue(FACE);
        BlockPos supportPos = pos.relative(face);
        return StickyRepeater.canSurviveOn(level.getBlockState(supportPos), level, supportPos, face.getOpposite());
    }

    @Override
    protected void tick(final BlockState state, final ServerLevel level, final BlockPos pos, final RandomSource random){
        this.refreshOutputState(level, pos, state);
    }

    @Override
    protected int getDirectSignal(BlockState state, BlockGetter level, BlockPos pos, Direction oppositeOfSideOfBlock){
        return getSignal(state, oppositeOfSideOfBlock.getOpposite());
    }

    @Override
    protected int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction oppositeOfSideOfBlock){
        if(!((RedStoneWireBlock)Blocks.REDSTONE_WIRE).shouldSignal && state.getValue(FACE) != Direction.DOWN)
            return 0;
        return getSignal(state, oppositeOfSideOfBlock.getOpposite());
    }

    private static int getSignal(BlockState state, Direction side){
        int signal = state.getValue(OUTPUT_SIGNAL);
        if(!state.getValue(POWERED) || signal == 0)
            return 0;
        Direction face = state.getValue(FACE);
        if(face.getAxis() == side.getAxis())
            return 0;
        if(state.getValue(ORIENTATION) != StickyRepeater.toOrientation(face, side))
            return 0;
        return signal;
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, @Nullable Orientation orientation, boolean movedByPiston){
        if(!state.canSurvive(level, pos)){
            BlockEntity entity = state.hasBlockEntity() ? level.getBlockEntity(pos) : null;
            dropResources(state, level, pos, entity);
            level.removeBlock(pos, false);
            BlockPos.MutableBlockPos sidePos = new BlockPos.MutableBlockPos();
            for(Direction side : Direction.values())
                level.updateNeighborsAt(sidePos.setWithOffset(pos, side), this);
            return;
        }
        this.checkTickOnNeighbor(level, pos, state);
    }

    private void checkTickOnNeighbor(Level level, BlockPos pos, BlockState state){
        if(level.getBlockTicks().willTickThisTick(pos, this))
            return;
        int outputValue = this.calculateOutputSignal(level, pos, state);
        int oldSignal = state.getValue(OUTPUT_SIGNAL);
        if(outputValue != oldSignal || state.getValue(POWERED) != this.shouldTurnOn(level, pos, state)){
            TickPriority priority = this.shouldPrioritize(level, pos, state) ? TickPriority.HIGH : TickPriority.NORMAL;
            level.scheduleTick(pos, this, 2, priority);
        }
    }

    private boolean shouldTurnOn(Level level, BlockPos pos, BlockState state){
        int input = this.getInputSignal(level, pos, state);
        if(input == 0)
            return false;
        int sideInput = this.getAlternateSignal(level, pos, state);
        return input > sideInput || (input == sideInput && state.getValue(MODE) == ComparatorMode.COMPARE);
    }

    private int getInputSignal(Level level, BlockPos pos, BlockState state){
        Direction face = state.getValue(FACE);
        Direction front = StickyRepeater.fromOrientation(face, state.getValue(ORIENTATION));
        BlockPos.MutableBlockPos inputPos = new BlockPos.MutableBlockPos().setWithOffset(pos, front.getOpposite());
        BlockState inputState = level.getBlockState(inputPos);
        if(inputState.hasAnalogOutputSignal())
            return inputState.getAnalogOutputSignal(level, inputPos, front);
        int signal = level.getSignal(inputPos, front.getOpposite());
        if(StickyRedstoneWireEvaluator.isRedstoneWire(inputState.getBlock()))
            signal = StickyRedstoneWireEvaluator.getConnections(level, inputPos, inputState, face).power();
        else if(StickyRepeater.isDiode(inputState)){
            if(StickyRepeater.getDiodeFace(inputState) != face)
                signal = 0;
        }
        if(signal < 15 && inputState.isRedstoneConductor(level, inputPos)){
            inputPos.move(front.getOpposite());
            inputState = level.getBlockState(inputPos);
            ItemFrame itemFrame = this.getItemFrame(level, front.getOpposite(), inputPos);
            int itemFrameOrBlockSignal = Math.max(
                itemFrame == null ? Integer.MIN_VALUE : itemFrame.getAnalogOutput(),
                inputState.hasAnalogOutputSignal() ? inputState.getAnalogOutputSignal(level, inputPos, front) : Integer.MIN_VALUE
            );
            if(itemFrameOrBlockSignal != Integer.MIN_VALUE)
                signal = itemFrameOrBlockSignal;
        }
        return signal;
    }

    private @Nullable ItemFrame getItemFrame(Level level, Direction direction, BlockPos pos) {
        List<ItemFrame> itemFrames = level.getEntitiesOfClass(
            ItemFrame.class,
            new AABB(pos),
            entity -> entity.getDirection() == direction
        );
        return itemFrames.size() == 1 ? itemFrames.getFirst() : null;
    }

    private int getAlternateSignal(SignalGetter level, BlockPos pos, BlockState state){
        Direction face = state.getValue(FACE);
        Direction front = StickyRepeater.fromOrientation(face, state.getValue(ORIENTATION));
        Direction clockWise = front.getClockWise(face.getAxis());
        BlockPos.MutableBlockPos sidePos = new BlockPos.MutableBlockPos();
        int clockWiseSignal = level.getControlInputSignal(sidePos.setWithOffset(pos, clockWise), clockWise, false);
        BlockState inputState = level.getBlockState(sidePos);
        if(StickyRedstoneWireEvaluator.isRedstoneWire(inputState.getBlock()))
            clockWiseSignal = StickyRedstoneWireEvaluator.getConnections(level, sidePos, inputState, face).power();
        else if(StickyRepeater.isDiode(inputState)){
            if(StickyRepeater.getDiodeFace(inputState) != face)
                clockWiseSignal = 0;
        }
        Direction counterClockWise = front.getCounterClockWise(face.getAxis());
        int counterClockWiseSignal = level.getControlInputSignal(sidePos.setWithOffset(pos, counterClockWise), counterClockWise, false);
        inputState = level.getBlockState(sidePos);
        if(StickyRedstoneWireEvaluator.isRedstoneWire(inputState.getBlock()))
            counterClockWiseSignal = StickyRedstoneWireEvaluator.getConnections(level, sidePos, inputState, face).power();
        else if(StickyRepeater.isDiode(inputState)){
            if(StickyRepeater.getDiodeFace(inputState) != face)
                counterClockWiseSignal = 0;
        }
        return Math.max(clockWiseSignal, counterClockWiseSignal);
    }

    @Override
    protected boolean isSignalSource(BlockState state){
        return true;
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity entity, ItemStack stack){
        if(this.shouldTurnOn(level, pos, state))
            level.scheduleTick(pos, this, 1);
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston){
        this.updateNeighborsInFront(level, pos, state);
    }

    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean movedByPiston){
        if(!movedByPiston)
            this.updateNeighborsInFront(level, pos, state);
    }

    private void updateNeighborsInFront(Level level, BlockPos pos, BlockState state){
        Direction face = state.getValue(FACE);
        Direction front = StickyRepeater.fromOrientation(face, state.getValue(ORIENTATION));
        BlockPos frontPos = pos.relative(front);
        Orientation orientation = ExperimentalRedstoneUtils.initialOrientation(level, front, face.getOpposite());
        level.neighborChanged(frontPos, this, orientation);
        level.updateNeighborsAtExceptFromFacing(frontPos, this, front.getOpposite(), orientation);
    }

    private boolean shouldPrioritize(BlockGetter level, BlockPos pos, BlockState state){
        Direction face = state.getValue(FACE);
        Direction front = StickyRepeater.fromOrientation(face, state.getValue(ORIENTATION));
        BlockState frontState = level.getBlockState(pos.relative(front));
        return StickyRepeater.isDiode(frontState) && StickyRepeater.getDiodeFace(frontState) == face && StickyRepeater.getDiodeFront(frontState) == front.getOpposite();
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos, Direction directionToNeighbour, BlockPos neighbourPos, BlockState neighbourState, RandomSource random){
        Direction face = state.getValue(FACE);
        if(directionToNeighbour == face && !StickyRepeater.canSurviveOn(level.getBlockState(neighbourPos), level, neighbourPos, face.getOpposite()))
            return Blocks.AIR.defaultBlockState();
        return state;
    }

    private int calculateOutputSignal(Level level, BlockPos pos, BlockState state){
        int inputSignal = this.getInputSignal(level, pos, state);
        if(inputSignal == 0)
            return 0;
        int alternateSignal = this.getAlternateSignal(level, pos, state);
        if(alternateSignal > inputSignal)
            return 0;
        return state.getValue(MODE) == ComparatorMode.SUBTRACT ? inputSignal - alternateSignal : inputSignal;
    }

    private void refreshOutputState(Level level, BlockPos pos, BlockState state) {
        int signal = this.calculateOutputSignal(level, pos, state);
        if (state.getValue(OUTPUT_SIGNAL) != signal || state.getValue(MODE) == ComparatorMode.COMPARE) {
            state = state.setValue(OUTPUT_SIGNAL, signal);
            boolean powered = this.shouldTurnOn(level, pos, state);
            if(state.getValue(POWERED) != powered)
                level.setBlock(pos, state.setValue(POWERED, powered), Block.UPDATE_CLIENTS);
            else
                level.setBlock(pos, state, 0);
            this.updateNeighborsInFront(level, pos, state);
        }
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror){
        Direction oldFace = state.getValue(FACE);
        Direction newFace = mirror.mirror(oldFace);
        Direction newFront = mirror.mirror(StickyRepeater.fromOrientation(oldFace, state.getValue(ORIENTATION)));
        if(newFront.getAxis() == newFace.getAxis())
            newFront = newFace.getAxis() == Direction.Axis.Z ? Direction.UP : Direction.NORTH;
        return state.setValue(FACE, newFace)
            .setValue(ORIENTATION, StickyRepeater.toOrientation(newFace, newFront));
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation){
        Direction oldFace = state.getValue(FACE);
        Direction newFace = rotation.rotate(oldFace);
        Direction newFront = rotation.rotate(StickyRepeater.fromOrientation(oldFace, state.getValue(ORIENTATION)));
        if(newFront.getAxis() == newFace.getAxis())
            newFront = newFace.getAxis() == Direction.Axis.Z ? Direction.UP : Direction.NORTH;
        return state.setValue(FACE, newFace)
            .setValue(ORIENTATION, StickyRepeater.toOrientation(newFace, newFront));
    }
}
