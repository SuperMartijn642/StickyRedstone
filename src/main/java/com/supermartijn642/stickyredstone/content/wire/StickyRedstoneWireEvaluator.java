package com.supermartijn642.stickyredstone.content.wire;

import com.supermartijn642.stickyredstone.StickyRedstone;
import com.supermartijn642.stickyredstone.content.StickyRepeater;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RedStoneWireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RedstoneSide;
import net.minecraft.world.level.redstone.Orientation;
import org.jetbrains.annotations.Nullable;

/**
 * Created 01/08/2026 by SuperMartijn642
 */
public abstract class StickyRedstoneWireEvaluator {

    public static final StickyRedstoneWireEvaluator DEFAULT = new Default();

    public static boolean isVanillaRedstoneWire(Block block){
        return block == Blocks.REDSTONE_WIRE;
    }

    public static boolean isStickyRedstoneWire(Block block){
        return block == StickyRedstone.singleStickyRedstoneDust || block == StickyRedstone.denseStickyRedstoneDust;
    }

    public static boolean isRedstoneWire(Block block){
        return isVanillaRedstoneWire(block) || isStickyRedstoneWire(block);
    }

    public static FaceState getConnections(BlockGetter level, BlockPos pos, BlockState state, Direction face){
        if(isVanillaRedstoneWire(state.getBlock())){
            if(face.getAxis().isHorizontal()){
                return state.getValue(RedStoneWireBlock.PROPERTY_BY_DIRECTION.get(face)) == RedstoneSide.UP ?
                    FaceState.get(false, false, false, false, state.getValue(RedStoneWireBlock.POWER)).set(face, Direction.DOWN, true).set(face, Direction.UP, true) :
                    FaceState.ABSENT;
            }
            if(face == Direction.UP)
                return FaceState.ABSENT;
            return FaceState.get(false, false, false, false, state.getValue(RedStoneWireBlock.POWER))
                .set(face, Direction.NORTH, state.getValue(RedStoneWireBlock.PROPERTY_BY_DIRECTION.get(Direction.NORTH)) != RedstoneSide.NONE)
                .set(face, Direction.EAST, state.getValue(RedStoneWireBlock.PROPERTY_BY_DIRECTION.get(Direction.EAST)) != RedstoneSide.NONE)
                .set(face, Direction.SOUTH, state.getValue(RedStoneWireBlock.PROPERTY_BY_DIRECTION.get(Direction.SOUTH)) != RedstoneSide.NONE)
                .set(face, Direction.WEST, state.getValue(RedStoneWireBlock.PROPERTY_BY_DIRECTION.get(Direction.WEST)) != RedstoneSide.NONE);
        }
        if(isStickyRedstoneWire(state.getBlock()))
            return ((StickyRedstoneDust)state.getBlock()).getConnections(level, pos, state, face);
        return FaceState.ABSENT;
    }

    public abstract void updatePowerStrength(Level level, BlockPos pos, BlockState state, Direction face, @Nullable Orientation orientation, boolean skipShapeUpdates);

    protected int getBlockSignal(Level level, BlockPos pos, Direction face){
        try{
            ((RedStoneWireBlock)Blocks.REDSTONE_WIRE).shouldSignal = false;
            int best = 0;
            BlockPos.MutableBlockPos sidePos = new BlockPos.MutableBlockPos();
            for(Direction side : Direction.values()){
                sidePos.setWithOffset(pos, side);
                BlockState sideState = level.getBlockState(sidePos);
                int signal;
                if(StickyRepeater.isDiode(sideState)){
                    if(StickyRepeater.getDiodeFace(sideState) != face)
                        continue;
                    signal = sideState.getDirectSignal(level, sidePos, side);
                }else
                    signal = level.getSignal(sidePos, side);
                if(StickyRepeater.isStickyDiode(sideState)
                    && StickyRepeater.getDiodeFace(sideState) != face)
                    continue;
                if(signal >= 15)
                    return 15;
                if(signal > best)
                    best = signal;
            }
            return best;
        }finally{
            ((RedStoneWireBlock)Blocks.REDSTONE_WIRE).shouldSignal = true;
        }
    }

    protected int getIncomingWireSignal(Level level, BlockPos pos, Direction face){
        BlockState self = level.getBlockState(pos);
        FaceState ownConnections = getConnections(level, pos, self, face);
        if(!ownConnections.isPresent())
            return 0;
        boolean isStickyRedstone = isStickyRedstoneWire(self.getBlock());

        int wireSignal = 0;
        BlockPos.MutableBlockPos sidePos = new BlockPos.MutableBlockPos();
        for(Direction side : Direction.values()){
            if(side.getAxis() == face.getAxis())
                continue;
            // Check side face of the block itself
            if(isStickyRedstone){
                FaceState sideConnections = getConnections(level, pos, self, side);
                if(sideConnections.isPresent()){
                    wireSignal = Math.max(wireSignal, sideConnections.power());
                    continue;
                }
            }
            // Check direct neighbor block
            sidePos.setWithOffset(pos, side);
            BlockState sideState = level.getBlockState(sidePos);
            if(isRedstoneWire(sideState.getBlock())){
                FaceState neighborConnections = getConnections(level, sidePos, sideState, face);
                if(neighborConnections.isPresent()){
                    wireSignal = Math.max(wireSignal, neighborConnections.power());
                    continue;
                }
            }
            // Check block to the side and behind
            if(!sideState.isRedstoneConductor(level, sidePos)){
                sidePos.move(face);
                FaceState neighborConnections = getConnections(level, sidePos, level.getBlockState(sidePos), side.getOpposite());
                if(neighborConnections.isPresent())
                    wireSignal = Math.max(wireSignal, neighborConnections.power());
                continue;
            }
            // Check block to the side and in front
            if(!isStickyRedstone){
                sidePos.setWithOffset(pos, face.getOpposite());
                if(!level.getBlockState(sidePos).isRedstoneConductor(level, sidePos)){
                    sidePos.move(side);
                    FaceState neighborConnections = getConnections(level, sidePos, level.getBlockState(sidePos), face);
                    if(neighborConnections.isPresent())
                        wireSignal = Math.max(wireSignal, neighborConnections.power());
                }
            }
        }
        return Math.max(0, wireSignal - 1);
    }

    private static class Default extends StickyRedstoneWireEvaluator {
        @Override
        public void updatePowerStrength(Level level, BlockPos pos, BlockState state, Direction face, @Nullable Orientation orientation, boolean skipShapeUpdates){
            if(isVanillaRedstoneWire(state.getBlock()) && face != Direction.DOWN)
                throw new IllegalStateException();
            FaceState ownConnections = getConnections(level, pos, state, face);
            int targetStrength = this.calculateTargetStrength(level, pos, face);
            if(!ownConnections.isPresent() || ownConnections.power() != targetStrength){
                if(ownConnections.isPresent()){
                    if(isStickyRedstoneWire(state.getBlock())){
                        StickyRedstoneDust block = (StickyRedstoneDust)state.getBlock();
                        BlockState newBlockState = block.updateState(level, pos, state, block.getState(level, pos, state).setFace(face, ownConnections.power(targetStrength)));
                        if(newBlockState != state)
                            level.setBlock(pos, newBlockState, Block.UPDATE_CLIENTS);
                    }else if(level.getBlockState(pos) == state)
                        level.setBlock(pos, state.setValue(RedStoneWireBlock.POWER, targetStrength), 2);
                }

                BlockPos.MutableBlockPos neighborPos = new BlockPos.MutableBlockPos();
                level.updateNeighborsAt(pos, state.getBlock());
                for(Direction direction : Direction.values())
                    level.updateNeighborsAt(neighborPos.setWithOffset(pos, direction), state.getBlock());
            }
        }

        private int calculateTargetStrength(Level level, BlockPos pos, Direction face){
            int blockSignal = this.getBlockSignal(level, pos, face);
            return blockSignal == 15 ? blockSignal : Math.max(blockSignal, this.getIncomingWireSignal(level, pos, face));
        }
    }
}
