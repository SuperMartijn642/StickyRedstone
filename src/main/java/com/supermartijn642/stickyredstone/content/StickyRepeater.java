package com.supermartijn642.stickyredstone.content;

import com.supermartijn642.core.block.BaseBlock;
import com.supermartijn642.core.block.BlockProperties;
import com.supermartijn642.core.block.BlockShape;
import com.supermartijn642.stickyredstone.StickyRedstone;
import com.supermartijn642.stickyredstone.content.wire.StickyRedstoneWireEvaluator;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.redstone.ExperimentalRedstoneUtils;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.ticks.TickPriority;
import org.jetbrains.annotations.Nullable;

/**
 * Created 04/08/2026 by SuperMartijn642
 */
public class StickyRepeater extends BaseBlock {

    public static final EnumProperty<Direction> FACE = BlockStateProperties.FACING;
    public static final IntegerProperty ORIENTATION = IntegerProperty.create("orientation", 0, 3);
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    public static final BooleanProperty LOCKED = BlockStateProperties.LOCKED;
    public static final IntegerProperty DELAY = BlockStateProperties.DELAY;

    public static final BlockShape[] SHAPES = new BlockShape[6];

    static{
        BlockShape base = BlockShape.createBlockShape(0, 0, 0, 16, 2, 16);
        SHAPES[Direction.DOWN.ordinal()] = base;
        SHAPES[Direction.UP.ordinal()] = base.flip(Direction.Axis.Y);
        SHAPES[Direction.NORTH.ordinal()] = base.rotate(Direction.Axis.X).flip(Direction.Axis.Z);
        SHAPES[Direction.EAST.ordinal()] = base.rotate(Direction.Axis.Z).flip(Direction.Axis.X);
        SHAPES[Direction.SOUTH.ordinal()] = base.rotate(Direction.Axis.X);
        SHAPES[Direction.WEST.ordinal()] = base.rotate(Direction.Axis.Z);
    }

    public static int toOrientation(Direction face, Direction side){
        if(face.getAxis() == side.getAxis())
            throw new IllegalArgumentException("Cannot get side " + side + " for face " + face + "!");
        if(face.getAxis().isVertical()){
            return switch(side){
                case NORTH -> 0;
                case EAST -> 1;
                case SOUTH -> 2;
                case WEST -> 3;
                default -> throw new AssertionError();
            };
        }
        if(side == Direction.UP)
            return 0;
        if(side == Direction.DOWN)
            return 2;
        return switch(face){
            case NORTH -> switch(side){
                case EAST -> 1;
                case WEST -> 3;
                default -> throw new AssertionError();
            };
            case EAST -> switch(side){
                case SOUTH -> 1;
                case NORTH -> 3;
                default -> throw new AssertionError();
            };
            case SOUTH -> switch(side){
                case WEST -> 1;
                case EAST -> 3;
                default -> throw new AssertionError();
            };
            case WEST -> switch(side){
                case NORTH -> 1;
                case SOUTH -> 3;
                default -> throw new AssertionError();
            };
            default -> throw new AssertionError();
        };
    }

    public static Direction fromOrientation(Direction face, int orientation){
        if(orientation < 0 || orientation > 3)
            throw new IllegalArgumentException("Orientation must be between 0 and 3");
        if(face.getAxis().isVertical()){
            return switch(orientation){
                case 0 -> Direction.NORTH;
                case 1 -> Direction.EAST;
                case 2 -> Direction.SOUTH;
                case 3 -> Direction.WEST;
                default -> throw new AssertionError();
            };
        }
        if(orientation == 0)
            return Direction.UP;
        if(orientation == 2)
            return Direction.DOWN;
        return switch(face){
            case NORTH -> switch(orientation){
                case 1 -> Direction.EAST;
                case 3 -> Direction.WEST;
                default -> throw new AssertionError();
            };
            case EAST -> switch(orientation){
                case 1 -> Direction.SOUTH;
                case 3 -> Direction.NORTH;
                default -> throw new AssertionError();
            };
            case SOUTH -> switch(orientation){
                case 1 -> Direction.WEST;
                case 3 -> Direction.EAST;
                default -> throw new AssertionError();
            };
            case WEST -> switch(orientation){
                case 1 -> Direction.NORTH;
                case 3 -> Direction.SOUTH;
                default -> throw new AssertionError();
            };
            default -> throw new AssertionError();
        };
    }

    public static boolean isStickyDiode(BlockState state){
        return state.is(StickyRedstone.stickyRepeater) || state.is(StickyRedstone.stickyComparator);
    }

    public static boolean isVanillaDiode(BlockState state){
        return DiodeBlock.isDiode(state);
    }

    public static boolean isDiode(BlockState state){
        return isStickyDiode(state) || isVanillaDiode(state);
    }

    public static Direction getDiodeFace(BlockState state){
        if(isStickyDiode(state))
            return state.getValue(FACE);
        if(isVanillaDiode(state))
            return Direction.DOWN;
        throw new IllegalArgumentException("Block is not a diode!");
    }

    public static Direction getDiodeFront(BlockState state){
        if(isStickyDiode(state))
            return fromOrientation(state.getValue(FACE), state.getValue(ORIENTATION));
        if(isVanillaDiode(state))
            return state.getValue(DiodeBlock.FACING).getOpposite();
        throw new IllegalArgumentException("Block is not a diode!");
    }

    public StickyRepeater(){
        super(false, BlockProperties.create().strength(0).sound(SoundType.STONE).pushReaction(PushReaction.DESTROY));
        this.registerDefaultState(
            this.defaultBlockState()
                .setValue(FACE, Direction.DOWN)
                .setValue(ORIENTATION, 0)
                .setValue(POWERED, false)
                .setValue(LOCKED, false)
                .setValue(DELAY, 1)
        );
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context){
        BlockPos.MutableBlockPos supportPos = new BlockPos.MutableBlockPos();
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Direction face = context.getClickedFace().getOpposite();
        // Find face to place the repeater on
        supportPos.setWithOffset(pos, face);
        if(!canSurviveOn(level.getBlockState(supportPos), level, supportPos, face.getOpposite())){
            face = face.getOpposite();
            supportPos.setWithOffset(pos, face);
            if(!canSurviveOn(level.getBlockState(supportPos), level, supportPos, face.getOpposite())){
                for(Direction side : Direction.values()){
                    if(side.getAxis() == context.getClickedFace().getAxis())
                        continue;
                    face = side;
                    supportPos.setWithOffset(pos, face);
                    if(canSurviveOn(level.getBlockState(supportPos), level, supportPos, face.getOpposite()))
                        break;
                }
                if(!canSurviveOn(level.getBlockState(supportPos), level, supportPos, face.getOpposite()))
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
        int orientation = toOrientation(face, front);
        // Get proper state
        BlockState state = this.defaultBlockState()
            .setValue(FACE, face)
            .setValue(ORIENTATION, orientation);
        return state.setValue(LOCKED, this.isLocked(level, pos, state));
    }

    @Override
    protected InteractionFeedback interact(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, Direction hitSide, Vec3 hitLocation){
        if(!player.getAbilities().mayBuild)
            return InteractionFeedback.PASS;
        level.setBlock(pos, state.cycle(DELAY), Block.UPDATE_ALL);
        return InteractionFeedback.SUCCESS;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context){
        return SHAPES[state.getValue(FACE).ordinal()].getUnderlying();
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> builder){
        builder.add(FACE, ORIENTATION, POWERED, LOCKED, DELAY);
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos){
        Direction face = state.getValue(FACE);
        BlockPos supportPos = pos.relative(face);
        return canSurviveOn(level.getBlockState(supportPos), level, supportPos, face.getOpposite());
    }

    public static boolean canSurviveOn(BlockState state, LevelReader level, BlockPos pos, Direction side){
        return state.isFaceSturdy(level, pos, side, SupportType.RIGID);
    }

    @Override
    protected void tick(final BlockState state, final ServerLevel level, final BlockPos pos, final RandomSource random){
        if(this.isLocked(level, pos, state))
            return;
        boolean on = state.getValue(POWERED);
        boolean shouldTurnOn = this.shouldTurnOn(level, pos, state);
        if(on && !shouldTurnOn)
            level.setBlock(pos, state.setValue(POWERED, false), Block.UPDATE_CLIENTS);
        else if(!on){
            level.setBlock(pos, state.setValue(POWERED, true), Block.UPDATE_CLIENTS);
            if(!shouldTurnOn)
                level.scheduleTick(pos, this, state.getValue(DELAY) * 2, TickPriority.VERY_HIGH);
        }
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
        if(!state.getValue(POWERED))
            return 0;
        Direction face = state.getValue(FACE);
        if(face.getAxis() == side.getAxis())
            return 0;
        return state.getValue(ORIENTATION) == toOrientation(face, side) ? 15 : 0;
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, @Nullable Orientation orientation, boolean movedByPiston){
        if(!state.canSurvive(level, pos)){
            dropResources(state, level, pos);
            level.removeBlock(pos, false);
            BlockPos.MutableBlockPos sidePos = new BlockPos.MutableBlockPos();
            for(Direction side : Direction.values())
                level.updateNeighborsAt(sidePos.setWithOffset(pos, side), this);
            return;
        }
        this.checkTickOnNeighbor(level, pos, state);
    }

    private void checkTickOnNeighbor(Level level, BlockPos pos, BlockState state){
        if(this.isLocked(level, pos, state))
            return;
        boolean on = state.getValue(POWERED);
        boolean shouldTurnOn = this.shouldTurnOn(level, pos, state);
        if(on == shouldTurnOn || level.getBlockTicks().willTickThisTick(pos, this))
            return;
        TickPriority priority = TickPriority.HIGH;
        if(this.shouldPrioritize(level, pos, state))
            priority = TickPriority.EXTREMELY_HIGH;
        else if(on)
            priority = TickPriority.VERY_HIGH;
        level.scheduleTick(pos, this, state.getValue(DELAY) * 2, priority);
    }

    private boolean isLocked(LevelReader level, BlockPos pos, BlockState state){
        return this.getAlternateSignal(level, pos, state) > 0;
    }

    private boolean shouldTurnOn(Level level, BlockPos pos, BlockState state){
        return this.getInputSignal(level, pos, state) > 0;
    }

    private int getInputSignal(Level level, BlockPos pos, BlockState state){
        Direction face = state.getValue(FACE);
        Direction front = fromOrientation(face, state.getValue(ORIENTATION));
        BlockPos inputPos = pos.relative(front.getOpposite());
        BlockState inputState = level.getBlockState(inputPos);
        if(StickyRedstoneWireEvaluator.isRedstoneWire(inputState.getBlock()))
            return StickyRedstoneWireEvaluator.getConnections(level, inputPos, inputState, face).power();
        if(StickyRepeater.isDiode(inputState)){
            if(StickyRepeater.getDiodeFace(inputState) != face)
                return 0;
        }
        return level.getSignal(inputPos, front.getOpposite());
    }

    private int getAlternateSignal(SignalGetter level, BlockPos pos, BlockState state){
        Direction face = state.getValue(FACE);
        Direction front = fromOrientation(face, state.getValue(ORIENTATION));
        Direction clockWise = front.getClockWise(face.getAxis());
        BlockPos.MutableBlockPos sidePos = new BlockPos.MutableBlockPos();
        int clockWiseSignal = level.getControlInputSignal(sidePos.setWithOffset(pos, clockWise), clockWise, true);
        BlockState inputState = level.getBlockState(sidePos);
        if(StickyRedstoneWireEvaluator.isRedstoneWire(inputState.getBlock()))
            clockWiseSignal = 0;
        else if(StickyRepeater.isDiode(inputState)){
            clockWiseSignal = StickyRepeater.getDiodeFace(inputState) == face ?
                inputState.getDirectSignal(level, sidePos, clockWise) : 0;
        }
        Direction counterClockWise = front.getCounterClockWise(face.getAxis());
        int counterClockWiseSignal = level.getControlInputSignal(sidePos.setWithOffset(pos, counterClockWise), counterClockWise, true);
        inputState = level.getBlockState(sidePos);
        if(StickyRedstoneWireEvaluator.isRedstoneWire(inputState.getBlock()))
            counterClockWiseSignal = 0;
        else if(StickyRepeater.isDiode(inputState)){
            counterClockWiseSignal = StickyRepeater.getDiodeFace(inputState) == face ?
                inputState.getDirectSignal(level, sidePos, counterClockWise) : 0;
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
        Direction front = fromOrientation(face, state.getValue(ORIENTATION));
        BlockPos frontPos = pos.relative(front);
        Orientation orientation = ExperimentalRedstoneUtils.initialOrientation(level, front, face.getOpposite());
        level.neighborChanged(frontPos, this, orientation);
        level.updateNeighborsAtExceptFromFacing(frontPos, this, front.getOpposite(), orientation);
    }

    private boolean shouldPrioritize(BlockGetter level, BlockPos pos, BlockState state){
        Direction face = state.getValue(FACE);
        Direction front = fromOrientation(face, state.getValue(ORIENTATION));
        BlockState frontState = level.getBlockState(pos.relative(front));
        return isDiode(frontState) && getDiodeFace(frontState) == face && getDiodeFront(frontState) == front.getOpposite();
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos, Direction directionToNeighbour, BlockPos neighbourPos, BlockState neighbourState, RandomSource random){
        Direction face = state.getValue(FACE);
        if(directionToNeighbour.getAxis() == face.getAxis()){
            if(directionToNeighbour == face && !canSurviveOn(level.getBlockState(neighbourPos), level, neighbourPos, face.getOpposite()))
                return Blocks.AIR.defaultBlockState();
            return state;
        }
        return level.isClientSide() ? state : state.setValue(LOCKED, this.isLocked(level, pos, state));
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random){
        if(!state.getValue(POWERED))
            return;
        Direction face = state.getValue(FACE);
        Direction front = fromOrientation(face, state.getValue(ORIENTATION));
        float offset = -5 / 16f;
        if(random.nextBoolean())
            offset = (state.getValue(DELAY) * 2 - 1) / 16f;
        double x = pos.getX() + 0.5 + face.getStepX() * 0.1 + (random.nextDouble() - 0.5) * 0.2 - front.getStepX() * offset;
        double y = pos.getY() + 0.5 + face.getStepY() * 0.1 + (random.nextDouble() - 0.5) * 0.2 - front.getStepY() * offset;
        double z = pos.getZ() + 0.5 + face.getStepZ() * 0.1 + (random.nextDouble() - 0.5) * 0.2 - front.getStepZ() * offset;
        level.addParticle(DustParticleOptions.REDSTONE, x, y, z, 0, 0, 0);
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror){
        Direction oldFace = state.getValue(FACE);
        Direction newFace = mirror.mirror(oldFace);
        Direction newFront = mirror.mirror(fromOrientation(oldFace, state.getValue(ORIENTATION)));
        if(newFront.getAxis() == newFace.getAxis())
            newFront = newFace.getAxis() == Direction.Axis.Z ? Direction.UP : Direction.NORTH;
        return state.setValue(FACE, newFace)
            .setValue(ORIENTATION, toOrientation(newFace, newFront));
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation){
        Direction oldFace = state.getValue(FACE);
        Direction newFace = rotation.rotate(oldFace);
        Direction newFront = rotation.rotate(fromOrientation(oldFace, state.getValue(ORIENTATION)));
        if(newFront.getAxis() == newFace.getAxis())
            newFront = newFace.getAxis() == Direction.Axis.Z ? Direction.UP : Direction.NORTH;
        return state.setValue(FACE, newFace)
            .setValue(ORIENTATION, toOrientation(newFace, newFront));
    }
}
