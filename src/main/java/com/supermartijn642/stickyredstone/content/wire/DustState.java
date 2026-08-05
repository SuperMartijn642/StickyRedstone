package com.supermartijn642.stickyredstone.content.wire;

import net.minecraft.core.Direction;

import java.util.Arrays;
import java.util.Objects;

/**
 * Created 25/07/2026 by SuperMartijn642
 */
public class DustState {

    public static final DustState EMPTY = of(FaceState.ABSENT, FaceState.ABSENT, FaceState.ABSENT, FaceState.ABSENT, FaceState.ABSENT, FaceState.ABSENT);

    public static DustState of(FaceState up, FaceState down, FaceState north, FaceState east, FaceState south, FaceState west){
        if(up == null || down == null || north == null || east == null || south == null || west == null)
            throw new NullPointerException();
        return new DustState(up, down, north, east, south, west);
    }

    public static DustState singleFace(Direction face, FaceState connections){
        return EMPTY.setFace(face, connections);
    }

    private final FaceState[] faceConnections = new FaceState[6];

    private DustState(FaceState up, FaceState down, FaceState north, FaceState east, FaceState south, FaceState west){
        this.faceConnections[Direction.UP.ordinal()] = up;
        this.faceConnections[Direction.DOWN.ordinal()] = down;
        this.faceConnections[Direction.NORTH.ordinal()] = north;
        this.faceConnections[Direction.EAST.ordinal()] = east;
        this.faceConnections[Direction.SOUTH.ordinal()] = south;
        this.faceConnections[Direction.WEST.ordinal()] = west;
    }

    private DustState(){
    }

    public FaceState getFace(Direction face){
        return this.faceConnections[face.ordinal()];
    }

    public boolean getConnection(Direction face, Direction side){
        return this.getFace(face).get(face, side);
    }

    public boolean getConnection(Direction face, int side){
        return this.getFace(face).get(side);
    }

    public DustState setFace(Direction face, FaceState connections){
        Objects.requireNonNull(connections);
        DustState state = new DustState();
        System.arraycopy(this.faceConnections, 0, state.faceConnections, 0, 6);
        state.faceConnections[face.ordinal()] = connections;
        return state;
    }

    @Override
    public boolean equals(Object o){
        if(o == null || this.getClass() != o.getClass()) return false;

        DustState state = (DustState)o;
        return Arrays.equals(this.faceConnections, state.faceConnections);
    }

    @Override
    public int hashCode(){
        return Arrays.hashCode(this.faceConnections);
    }
}
