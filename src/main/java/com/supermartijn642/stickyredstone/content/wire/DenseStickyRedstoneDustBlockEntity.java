package com.supermartijn642.stickyredstone.content.wire;

import com.supermartijn642.core.block.BaseBlockEntity;
import com.supermartijn642.stickyredstone.StickyRedstone;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;

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
    protected CompoundTag writeData(){
        CompoundTag data = new CompoundTag();
        data.putInt("up", this.state.getFace(Direction.UP).index());
        data.putInt("down", this.state.getFace(Direction.DOWN).index());
        data.putInt("north", this.state.getFace(Direction.NORTH).index());
        data.putInt("east", this.state.getFace(Direction.EAST).index());
        data.putInt("south", this.state.getFace(Direction.SOUTH).index());
        data.putInt("west", this.state.getFace(Direction.WEST).index());
        return data;
    }

    @Override
    protected void readData(CompoundTag data){
        FaceState up = data.getInt("up").map(FaceState::byIndex).orElse(FaceState.ABSENT);
        FaceState down = data.getInt("down").map(FaceState::byIndex).orElse(FaceState.ABSENT);
        FaceState north = data.getInt("north").map(FaceState::byIndex).orElse(FaceState.ABSENT);
        FaceState east = data.getInt("east").map(FaceState::byIndex).orElse(FaceState.ABSENT);
        FaceState south = data.getInt("south").map(FaceState::byIndex).orElse(FaceState.ABSENT);
        FaceState west = data.getInt("west").map(FaceState::byIndex).orElse(FaceState.ABSENT);
        this.state = DustState.of(up, down, north, east, south, west);
    }
}
