package com.supermartijn642.stickyredstone.content.wire;

import com.supermartijn642.core.block.BaseBlock;
import com.supermartijn642.core.block.BlockProperties;
import com.supermartijn642.core.block.BlockShape;
import com.supermartijn642.stickyredstone.StickyRedstone;
import com.supermartijn642.stickyredstone.content.StickyRepeater;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ObserverBlock;
import net.minecraft.world.level.block.RedstoneWireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.redstone.ExperimentalRedstoneUtils;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3i;

/**
 * Created 24/07/2026 by SuperMartijn642
 */
public abstract class StickyRedstoneDust extends BaseBlock {

    public static final ThreadLocal<Direction> DESTROY_FACE_OVERRIDE = new ThreadLocal<>();

    private static final BlockShape[][] FACE_SHAPES;

    static{
        BlockShape dot = BlockShape.createBlockShape(3, 0, 3, 13, 1, 13);
        BlockShape side = BlockShape.createBlockShape(3, 0, 0, 13, 1, 8);
        BlockShape sideAlt = BlockShape.createBlockShape(3, 0, 8, 13, 1, 16);
        FACE_SHAPES = new BlockShape[6][];
        for(Direction face : Direction.values()){
            BlockShape[] shapes = FACE_SHAPES[face.ordinal()] = new BlockShape[(int)Math.pow(2, 4)];
            for(int index = 0; index < shapes.length; index++){
                Vector3i rotation = new Vector3i();
                rotation.x = face == Direction.UP ? 180 : face == Direction.DOWN ? 0 : 90;
                rotation.y = face.getAxis().isVertical() ? 0 : (int)face.toYRot();
                BlockShape shape = rotateShape(dot, rotation.x, rotation.y, rotation.z);
                FaceState connections = FaceState.byIndex(index);
                if(connections.get(0))
                    shape = BlockShape.or(shape, rotateShape(side, rotation.x, rotation.y, rotation.z));
                if(connections.get(2))
                    shape = BlockShape.or(shape, rotateShape(sideAlt, rotation.x, rotation.y, rotation.z));
                switch(face.getAxis()){
                    case X -> {
                        rotation.x = 0;
                        rotation.y = face.getAxisDirection() == Direction.AxisDirection.POSITIVE ? 180 : 0;
                        rotation.z = -face.getStepX() * 90;
                    }
                    case Y -> rotation.y += face.getStepY() * 90;
                    case Z -> rotation.z += face.getStepZ() * 90;
                }
                if(connections.get(1))
                    shape = BlockShape.or(shape, rotateShape(side, rotation.x, rotation.y, rotation.z));
                if(connections.get(3))
                    shape = BlockShape.or(shape, rotateShape(sideAlt, rotation.x, rotation.y, rotation.z));
                shapes[index] = shape;
            }
        }
    }

    private static BlockShape rotateShape(BlockShape shape, int x, int y, int z){
        x = (x % 360 + 360) % 360;
        y = (y % 360 + 360) % 360;
        z = (z % 360 + 360) % 360;
        for(int i = 0; i < x; i += 90)
            shape = shape.rotate(Direction.Axis.X);
        for(int i = 0; i < y; i += 90)
            shape = shape.rotate(Direction.Axis.Y);
        for(int i = 0; i < z; i += 90)
            shape = shape.rotate(Direction.Axis.Z);
        return shape;
    }

    public StickyRedstoneDust(){
        super(false, BlockProperties.create().noCollision().strength(0).pushReaction(PushReaction.POPPED));
    }

    protected abstract FaceState getConnections(BlockGetter level, BlockPos pos, BlockState state, Direction face);

    public abstract DustState getState(BlockGetter level, BlockPos pos, BlockState state);

    public abstract BlockState updateState(BlockGetter level, BlockPos pos, BlockState blockState, DustState newState);

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context){
        BlockShape shape = null;
        for(Direction face : Direction.values()){
            FaceState connections = this.getConnections(level, pos, state, face);
            if(!connections.isPresent())
                continue;
            if(shape == null)
                shape = FACE_SHAPES[face.ordinal()][connections.power(0).index()];
            else
                shape = BlockShape.or(shape, FACE_SHAPES[face.ordinal()][connections.power(0).index()]);
        }
        return shape == null ? BlockShape.fullCube().getUnderlying() : shape.getUnderlying();
    }

    public VoxelShape getHighlightShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context, Direction face){
        // Get the shape from single dust for the targeted face
        FaceState connections = StickyRedstoneWireEvaluator.getConnections(level, pos, state, face);
        if(!connections.isPresent())
            return this.getShape(state, level, pos, context);
        BlockState dummyState = StickyRedstone.singleStickyRedstoneDust.getBlockStateForConnections(face, connections);
        return dummyState.getShape(level, pos, context);
    }

    public VoxelShape getHighlightShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context, BlockHitResult hitResult){
        // Find the face is being looked at
        Vec3 location = hitResult.getLocation();
        Direction face;
        FaceState connections;
        do{
            face = Direction.getApproximateNearest(
                ((location.x % 1) + 1) % 1 - 0.5,
                ((location.y % 1) + 1) % 1 - 0.5,
                ((location.z % 1) + 1) % 1 - 0.5
            );
            connections = StickyRedstoneWireEvaluator.getConnections(level, pos, state, face);
            if(connections.isPresent())
                break;
            location = new Vec3(
                face.getAxis() == Direction.Axis.X ? 0.5 : location.x,
                face.getAxis() == Direction.Axis.Y ? 0.5 : location.y,
                face.getAxis() == Direction.Axis.Z ? 0.5 : location.z
            );
            if(location.x == 0.5 && location.y == 0.5 && location.z == 0.5)
                return this.getShape(state, level, pos, context);
        }while(true);
        // Get the shape from single dust for the targeted face
        return this.getHighlightShape(state, level, pos, context, face);
    }

    private static boolean isCross(FaceState connections){
        return connections.isPresent() && connections.get(0) && connections.get(1) && connections.get(2) && connections.get(3);
    }

    private static boolean isDot(FaceState connections){
        return connections.isPresent() && !connections.get(0) && !connections.get(1) && !connections.get(2) && !connections.get(3);
    }

    protected FaceState getProperConnections(DustState state, BlockGetter level, BlockPos pos, Direction face){
        FaceState current = state.getFace(face);
        boolean wasDot = isDot(current);
        current = this.getMissingConnections(state.setFace(face, FaceState.get(false, false, false, false, state.getFace(face).power())), level, pos, face);
        if(wasDot && isDot(current))
            return current;

        boolean line13Empty = !current.get(0) && !current.get(2);
        boolean line24Empty = !current.get(1) && !current.get(3);
        if(line13Empty)
            current = current.set(1, true).set(3, true);
        if(line24Empty)
            current = current.set(0, true).set(2, true);
        return current;
    }

    private FaceState getMissingConnections(DustState state, BlockGetter level, BlockPos pos, Direction face){
        FaceState connections = state.getFace(face);
        BlockPos.MutableBlockPos sidePos = null;
        for(Direction side : Direction.values()){
            if(side.getAxis() == face.getAxis())
                continue;
            if(!connections.get(face, side)){
                if(state.getFace(side).isPresent()){
                    connections = connections.set(face, side, true);
                    continue;
                }
                if(sidePos == null)
                    sidePos = new BlockPos.MutableBlockPos();
                sidePos.setWithOffset(pos, side);
                BlockState sideState = level.getBlockState(sidePos);
                boolean shouldConnect = shouldConnectTo(level, sidePos, sideState, face, side.getOpposite(), false);
                if(!shouldConnect && !sideState.isRedstoneConductor(level, sidePos)){
                    sidePos.move(face);
                    shouldConnect = shouldConnectTo(level, sidePos, level.getBlockState(sidePos), side.getOpposite(), face.getOpposite(), true);
                }
                connections = connections.set(face, side, shouldConnect);
            }
        }
        return connections;
    }

    @Override
    public BlockState updateShape(BlockState blockState, LevelReader level, ScheduledTickAccess ticks, BlockPos pos, Direction side, BlockPos sidePos, BlockState sideBlockState, RandomSource random){
        boolean hasChanged = false;
        DustState state;
        if(this.getConnections(level, pos, blockState, side).isPresent() && !canSurviveOn(level, sidePos, sideBlockState, side.getOpposite())){
            state = this.getState(level, pos, blockState).setFace(side, FaceState.ABSENT);
            if(state.equals(DustState.EMPTY))
                return Blocks.AIR.defaultBlockState();
            hasChanged = true;
        }else
            state = this.getState(level, pos, blockState);

        for(Direction face : Direction.values()){
            if(face == side.getOpposite())
                continue;
            FaceState connections = state.getFace(face);
            if(!connections.isPresent())
                continue;
            FaceState newConnections = this.getProperConnections(state, level, pos, face);
            if(connections != newConnections){
                state = state.setFace(face, newConnections);
                hasChanged = true;
            }
        }

        if(hasChanged)
            return this.updateState(level, pos, blockState, state);
        return blockState;
    }

    @Override
    protected void updateIndirectNeighbourShapes(BlockState blockState, LevelAccessor level, BlockPos pos, @UpdateFlags int updateFlags, int updateLimit){
        BlockPos.MutableBlockPos sideBelowPos = new BlockPos.MutableBlockPos();
        BlockPos.MutableBlockPos sidePos = null;
        DustState state = this.getState(level, pos, blockState);
        for(Direction face : Direction.values()){
            FaceState connections = state.getFace(face);
            if(!state.equals(DustState.EMPTY) && !connections.isPresent())
                continue;
            for(Direction side : Direction.values()){
                if(side.getAxis() == face.getAxis())
                    continue;
                if(!state.equals(DustState.EMPTY) && !connections.get(face, side))
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
                    level.neighborShapeChanged(side.getOpposite(), sideBelowPos, sidePos, level.getBlockState(sidePos), updateFlags, updateLimit);
                }else{
                    sidePos.setWithOffset(pos, side);
                    level.neighborShapeChanged(face.getOpposite(), sideBelowPos, sidePos, level.getBlockState(sidePos), updateFlags, updateLimit);
                }
            }
        }
    }

    private static boolean shouldConnectTo(BlockGetter level, BlockPos pos, BlockState state, Direction face, Direction side, boolean wiresOnly){
        if(StickyRedstoneWireEvaluator.isStickyRedstoneWire(state.getBlock()))
            return StickyRedstoneWireEvaluator.getConnections(level, pos, state, face).isPresent();
        if(StickyRedstoneWireEvaluator.isVanillaRedstoneWire(state.getBlock()))
            return face == Direction.DOWN || (face != Direction.UP && side.getAxis() == Direction.Axis.Y);
        if(wiresOnly)
            return false;
        if(StickyRepeater.isDiode(state)){
            if(StickyRepeater.getDiodeFace(state) != face)
                return false;
            if(state.is(StickyRedstone.stickyRepeater))
                return StickyRepeater.getDiodeFront(state).getAxis() == side.getAxis();
        }
        if(state.is(Blocks.OBSERVER))
            return side == state.getValue(ObserverBlock.FACING);
        return state.shouldRedstoneWireConnectTo(level, pos, side.getOpposite());
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos){
        boolean hasASurvivingFace = false;
        BlockPos.MutableBlockPos sidePos = new BlockPos.MutableBlockPos();
        for(Direction face : Direction.values()){
            FaceState connections = this.getConnections(level, pos, state, face);
            if(!connections.isPresent())
                continue;
            sidePos.setWithOffset(pos, face);
            if(canSurviveOn(level, sidePos, level.getBlockState(sidePos), face.getOpposite())){
                hasASurvivingFace = true;
                break;
            }
        }
        return hasASurvivingFace;
    }

    public static boolean canSurviveOn(BlockGetter level, BlockPos pos, BlockState state, Direction side){
        return state.isFaceSturdy(level, pos, side) || (side == Direction.UP && state.is(Blocks.HOPPER));
    }

    @Override
    protected void onPlace(BlockState blockState, Level level, BlockPos pos, BlockState oldBlockState, boolean movedByPiston){
        if(oldBlockState.getBlock() instanceof StickyRedstoneDust || level.isClientSide())
            return;
        for(Direction face : Direction.values()){
            if(this.getConnections(level, pos, blockState, face).isPresent())
                this.updatePowerStrength(level, pos, blockState, face, null, true);
        }
        DustState state = this.getState(level, pos, blockState);
        BlockPos.MutableBlockPos sidePos = new BlockPos.MutableBlockPos();
        for(Direction.Axis axis : Direction.Axis.values()){
            if(state.getFace(axis.getPositive()).isPresent() || state.getFace(axis.getNegative()).isPresent()){
                sidePos.setWithOffset(pos, axis.getNegative());
                level.updateNeighborsAt(sidePos, this);
                sidePos.setWithOffset(pos, axis.getPositive());
                level.updateNeighborsAt(sidePos, this);
            }
        }
        this.updateNeighborsOfNeighboringWires(level, pos);
    }

    public void updatePowerStrength(Level level, BlockPos pos, BlockState state, Direction face, @Nullable Orientation orientation, boolean shapeUpdateWiresAroundInitialPosition){
        StickyRedstoneWireEvaluator.DEFAULT.updatePowerStrength(level, pos, state, face, orientation, shapeUpdateWiresAroundInitialPosition);
    }

    private void updateNeighborsOfNeighboringWires(Level level, BlockPos pos){
        BlockPos.MutableBlockPos neighbor = new BlockPos.MutableBlockPos();
        BlockPos.MutableBlockPos dummyPos = new BlockPos.MutableBlockPos();
        for(Direction side : Direction.values())
            this.updateNeighborsAndTheirNeighbors(level, neighbor.setWithOffset(pos, side), dummyPos);

        for(Direction side : Direction.Plane.HORIZONTAL){
            neighbor.setWithOffset(pos, side);
            this.updateNeighborsAndTheirNeighbors(level, neighbor, dummyPos);
            this.updateNeighborsAndTheirNeighbors(level, neighbor.move(Direction.UP), dummyPos);
            this.updateNeighborsAndTheirNeighbors(level, neighbor.move(Direction.DOWN, 2), dummyPos);
        }
        neighbor.setWithOffset(pos, Direction.UP);
        this.updateNeighborsAndTheirNeighbors(level, neighbor, dummyPos);
        neighbor.setWithOffset(pos, Direction.DOWN);
        this.updateNeighborsAndTheirNeighbors(level, neighbor, dummyPos);
    }

    private void updateNeighborsAndTheirNeighbors(Level level, BlockPos pos, BlockPos.MutableBlockPos dummyPos){
        Block block = level.getBlockState(pos).getBlock();
        if(!StickyRedstoneWireEvaluator.isRedstoneWire(block))
            return;
        level.updateNeighborsAt(pos, block);
        for(Direction side : Direction.values())
            level.updateNeighborsAt(dummyPos.setWithOffset(pos, side), block);
    }

    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean movedByPiston){
        if(movedByPiston)
            return;
        BlockPos.MutableBlockPos sidePos = new BlockPos.MutableBlockPos();
        for(Direction side : Direction.values())
            level.updateNeighborsAt(sidePos.setWithOffset(pos, side), this);
        for(Direction face : Direction.values())
            this.updatePowerStrength(level, pos, Blocks.AIR.defaultBlockState(), face, null, false);
        this.updateNeighborsOfNeighboringWires(level, pos);
    }

    @Override
    protected void neighborChanged(BlockState blockState, Level level, BlockPos pos, Block block, @Nullable Orientation orientation, boolean movedByPiston){
        if(level.isClientSide())
            return;
        BlockPos.MutableBlockPos sidePos = new BlockPos.MutableBlockPos();
        for(Direction face : Direction.values()){
            FaceState connections = this.getConnections(level, pos, blockState, face);
            if(!connections.isPresent())
                continue;
            sidePos.setWithOffset(pos, face);
            if(canSurviveOn(level, sidePos, level.getBlockState(sidePos), face.getOpposite()))
                this.updatePowerStrength(level, pos, blockState, face, orientation, false);
        }
    }

    @Override
    protected int getDirectSignal(BlockState state, BlockGetter level, BlockPos pos, Direction oppositeOfSideOfBlock){
        return !((RedstoneWireBlock)Blocks.REDSTONE_WIRE).shouldSignal ? 0 : state.getSignal(level, pos, oppositeOfSideOfBlock);
    }

    @Override
    protected int getSignal(BlockState blockState, BlockGetter level, BlockPos pos, Direction oppositeOfSideOfBlock){
        if(!((RedstoneWireBlock)Blocks.REDSTONE_WIRE).shouldSignal)
            return 0;

        FaceState connections = this.getConnections(level, pos, blockState, oppositeOfSideOfBlock.getOpposite());
        if(connections.isPresent())
            return connections.power();
        int power = 0;
        DustState state = this.getState(level, pos, blockState);
        for(Direction face : Direction.values()){
            if(face.getAxis() == oppositeOfSideOfBlock.getAxis())
                continue;
            connections = this.getProperConnections(state, level, pos, face);
            if(connections.power() > power && connections.get(face, oppositeOfSideOfBlock.getOpposite()))
                power = connections.power();
        }
        return power;
    }

    @Override
    protected boolean isSignalSource(BlockState state){
        return ((RedstoneWireBlock)Blocks.REDSTONE_WIRE).shouldSignal;
    }

    @Override
    protected InteractionFeedback interact(BlockState blockState, Level level, BlockPos pos, Player player, InteractionHand hand, Direction hitSide, Vec3 hitLocation){
        if(!player.getAbilities().mayBuild)
            return InteractionFeedback.PASS;

        Direction face = Direction.getApproximateNearest(hitLocation.x - pos.getX() - 0.5, hitLocation.y - pos.getY() - 0.5, hitLocation.z - pos.getZ() - 0.5);
        FaceState connections = this.getConnections(level, pos, blockState, face);
        if(isCross(connections) || isDot(connections)){
            FaceState newConnections = isCross(connections) ? FaceState.get(false, false, false, false, 0) : FaceState.get(true, true, true, true, 0);
            DustState state = this.getState(level, pos, blockState);
            newConnections = this.getProperConnections(state.setFace(face, newConnections), level, pos, face);
            newConnections = newConnections.power(connections.power());
            if(newConnections != connections){
                BlockState newBlockState = this.updateState(level, pos, blockState, state.setFace(face, newConnections));
                if(blockState != newBlockState)
                    level.setBlock(pos, newBlockState, Block.UPDATE_ALL);
                this.updatesOnShapeChange(level, pos, face, connections, newConnections);
                return InteractionFeedback.SUCCESS;
            }
        }
        return InteractionFeedback.PASS;
    }

    public void updatesOnShapeChange(Level level, BlockPos pos, Direction face, FaceState oldState, FaceState newState){
        Orientation orientation = ExperimentalRedstoneUtils.initialOrientation(level, null, Direction.UP);
        BlockPos.MutableBlockPos sidePos = new BlockPos.MutableBlockPos();
        for(Direction side : Direction.values()){
            if(side.getAxis() == face.getAxis())
                continue;
            sidePos.setWithOffset(pos, side);
            if(oldState.get(face, side) == newState.get(face, side) || level.getBlockState(sidePos).isRedstoneConductor(level, sidePos))
                continue;
            level.updateNeighborsAtExceptFromFacing(sidePos, this, side.getOpposite(), ExperimentalRedstoneUtils.withFront(orientation, side));
        }
    }

    @Override
    protected boolean shouldRedstoneWireConnectTo(BlockState state, BlockGetter level, BlockPos pos, @Nullable Direction oppositeOfSideOfBlock){
        return oppositeOfSideOfBlock != null && this.getConnections(level, pos, state, Direction.DOWN).isPresent();
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random){
        for(Direction face : Direction.values()){
            FaceState connections = this.getConnections(level, pos, state, face);
            int power = connections.power();
            if(power == 0)
                continue;
            for(Direction side : Direction.values()){
                if(side.getAxis() == face.getAxis())
                    continue;
                float range = connections.get(face, side) ? 0.5f : 0.3f;
                if(random.nextFloat() >= 0.2f * range)
                    continue;
                float offset = range * random.nextFloat();
                double x = 0.5 + 0.4375f * face.getStepX() + offset * side.getStepX();
                double y = 0.5 + 0.4375f * face.getStepY() + offset * side.getStepY();
                double z = 0.5 + 0.4375f * face.getStepZ() + offset * side.getStepZ();
                level.addParticle(
                    new DustParticleOptions(RedstoneWireBlock.COLORS[power], 1),
                    pos.getX() + x, pos.getY() + y, pos.getZ() + z,
                    0, 0, 0
                );
            }
        }
    }
}
