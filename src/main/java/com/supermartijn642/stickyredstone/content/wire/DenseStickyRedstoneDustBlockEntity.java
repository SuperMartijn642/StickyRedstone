package com.supermartijn642.stickyredstone.content.wire;

import com.supermartijn642.core.block.BaseBlockEntity;
import com.supermartijn642.stickyredstone.StickyRedstone;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * Created 24/07/2026 by SuperMartijn642
 */
public class DenseStickyRedstoneDustBlockEntity extends BaseBlockEntity {

    private DustState state = DustState.EMPTY;

    public DenseStickyRedstoneDustBlockEntity(BlockPos pos, BlockState state){
        super(StickyRedstone.denseStickyRedstoneDustEntity, pos, state);
    }

    public DustState getState(){
        return this.state;
    }

    public void setState(DustState state){
        this.state = state;
        this.dataChanged();
    }

    public int getDustCount(){
        int count = 0;
        for(Direction face : Direction.values()){
            if(this.state.getFace(face).isPresent())
                count++;
        }
        return count;
    }

    @Override
    protected void writeData(ValueOutput output){
        output.putInt("up", this.state.getFace(Direction.UP).index());
        output.putInt("down", this.state.getFace(Direction.DOWN).index());
        output.putInt("north", this.state.getFace(Direction.NORTH).index());
        output.putInt("east", this.state.getFace(Direction.EAST).index());
        output.putInt("south", this.state.getFace(Direction.SOUTH).index());
        output.putInt("west", this.state.getFace(Direction.WEST).index());
    }

    @Override
    protected void readData(ValueInput input){
        FaceState up = input.getInt("up").map(FaceState::byIndex).orElse(FaceState.ABSENT);
        FaceState down = input.getInt("down").map(FaceState::byIndex).orElse(FaceState.ABSENT);
        FaceState north = input.getInt("north").map(FaceState::byIndex).orElse(FaceState.ABSENT);
        FaceState east = input.getInt("east").map(FaceState::byIndex).orElse(FaceState.ABSENT);
        FaceState south = input.getInt("south").map(FaceState::byIndex).orElse(FaceState.ABSENT);
        FaceState west = input.getInt("west").map(FaceState::byIndex).orElse(FaceState.ABSENT);
        this.state = DustState.of(up, down, north, east, south, west);
    }
}
