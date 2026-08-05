package com.supermartijn642.stickyredstone.content;

import com.supermartijn642.core.block.BaseBlock;
import com.supermartijn642.core.block.BlockProperties;
import com.supermartijn642.core.block.BlockShape;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.redstone.ExperimentalRedstoneUtils;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Created 23/07/2026 by SuperMartijn642
 */
public class StickyRedstoneTorchBlock extends BaseBlock {

    public static final EnumProperty<Direction> FACE = BlockStateProperties.FACING;
    public static final BooleanProperty LIT = BlockStateProperties.LIT;
    private static final Map<BlockGetter,List<Toggle>> RECENT_TOGGLES = new WeakHashMap<>();
    private static final BlockShape[] SHAPES = new BlockShape[6];

    static {
        BlockShape shape = BlockShape.createBlockShape(6, 0, 6, 10, 10, 10);
        for(Direction face : Direction.values()){
            BlockShape faceShape = shape;
            if(face == Direction.UP)
                faceShape = shape.rotate(Direction.Axis.X).rotate(Direction.Axis.X);
            else if(face != Direction.DOWN){
                faceShape = faceShape.rotate(Direction.Axis.X);
                for(int i = 0; i < (int)face.toYRot() / 90; i++)
                    faceShape = faceShape.rotate(Direction.Axis.Y);
            }
            SHAPES[face.ordinal()] = faceShape;
        }
    }

    public StickyRedstoneTorchBlock(){
        super(false, BlockProperties.create().noCollision().strength(0).lightLevel(Blocks.litBlockEmission(7)).sound(SoundType.WOOD).pushReaction(PushReaction.DESTROY));
        this.registerDefaultState(this.defaultBlockState().setValue(FACE, Direction.DOWN).setValue(LIT, true));
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context){
        BlockState state = this.defaultBlockState();
        LevelReader level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        for(Direction face : context.getNearestLookingDirections()){
            state = state.setValue(FACE, face);
            if(state.canSurvive(level, pos))
                return state;
        }
        return null;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> builder){
        builder.add(FACE, LIT);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random){
        if(state.getValue(LIT)){
            Direction face = state.getValue(FACE);
            double x = pos.getX() + 0.5 - face.getStepX() * 0.2 + (random.nextDouble() - 0.5) * 0.2;
            double y = pos.getY() + 0.5 - face.getStepY() * 0.2 + (random.nextDouble() - 0.5) * 0.2;
            double z = pos.getZ() + 0.5 - face.getStepZ() * 0.2 + (random.nextDouble() - 0.5) * 0.2;
            level.addParticle(DustParticleOptions.REDSTONE, x, y, z, 0.0, 0.0, 0.0);
        }
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context){
        return SHAPES[state.getValue(FACE).ordinal()].getUnderlying();
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos){
        Direction face = state.getValue(FACE);
        return level.getBlockState(pos.relative(face)).isFaceSturdy(level, pos.relative(face.getOpposite()), face, SupportType.RIGID);
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos, Direction directionToNeighbour, BlockPos neighbourPos, BlockState neighbourState, RandomSource random){
        return directionToNeighbour == state.getValue(FACE) && !state.canSurvive(level, pos) ? Blocks.AIR.defaultBlockState() : state;
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation){
        return state.setValue(FACE, rotation.rotate(state.getValue(FACE)));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror){
        return state.setValue(FACE, mirror.mirror(state.getValue(FACE)));
    }

    private void notifyNeighbors(Level level, BlockPos pos, BlockState state){
        Orientation orientation = ExperimentalRedstoneUtils.initialOrientation(level, null, Direction.UP);
        for(Direction side : Direction.values())
            level.updateNeighborsAt(pos.relative(side), this, ExperimentalRedstoneUtils.withFront(orientation, side));
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston){
        this.notifyNeighbors(level, pos, state);
    }

    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean movedByPiston){
        if(!movedByPiston)
            this.notifyNeighbors(level, pos, state);
    }

    @Override
    protected int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction oppositeSideOfNeighbor){
        Direction side = oppositeSideOfNeighbor.getOpposite();
        return state.getValue(LIT) && side != state.getValue(FACE) ? 15 : 0;
    }

    protected boolean isSupportBlockPowered(Level level, BlockPos pos, BlockState state){
        Direction face = state.getValue(FACE);
        return level.hasSignal(pos.relative(face), face);
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, @Nullable Orientation orientation, boolean movedByPiston){
        if(state.getValue(LIT) == this.isSupportBlockPowered(level, pos, state) && !level.getBlockTicks().willTickThisTick(pos, this))
            level.scheduleTick(pos, this, RedstoneTorchBlock.TOGGLE_DELAY);
    }

    @Override
    protected int getDirectSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction){
        return direction == state.getValue(FACE) ? state.getSignal(level, pos, direction) : 0;
    }

    @Override
    protected boolean isSignalSource(BlockState state){
        return true;
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random){
        // Remove stale out toggles
        List<Toggle> toggles = RECENT_TOGGLES.get(level);
        while(toggles != null && !toggles.isEmpty() && level.getGameTime() - toggles.getFirst().when > RedstoneTorchBlock.RECENT_TOGGLE_TIMER)
            toggles.removeFirst();

        // Update torch lit state
        boolean neighborSignal = this.isSupportBlockPowered(level, pos, state);
        if(state.getValue(LIT)){
            if(neighborSignal){
                level.setBlock(pos, state.setValue(LIT, false), Block.UPDATE_ALL);
                if(isToggledTooFrequently(level, pos, true)){
                    level.levelEvent(LevelEvent.REDSTONE_TORCH_BURNOUT, pos, 0);
                    level.scheduleTick(pos, level.getBlockState(pos).getBlock(), RedstoneTorchBlock.RESTART_DELAY);
                }
            }
        }else if(!neighborSignal && !isToggledTooFrequently(level, pos, false))
            level.setBlock(pos, state.setValue(LIT, true), 3);
    }

    private static boolean isToggledTooFrequently(Level level, BlockPos pos, boolean add){
        List<Toggle> toggles = RECENT_TOGGLES.computeIfAbsent(level, _ -> new ArrayList<>());
        if(add)
            toggles.add(new Toggle(pos.immutable(), level.getGameTime()));
        int count = 0;
        for(Toggle toggle : toggles){
            if(toggle.pos.equals(pos) && ++count >= RedstoneTorchBlock.MAX_RECENT_TOGGLES)
                return true;
        }
        return false;
    }

    private record Toggle(BlockPos pos, long when) {
    }
}
