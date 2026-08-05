package com.supermartijn642.stickyredstone.content.wire;

import net.minecraft.core.Direction;

import java.util.ArrayList;
import java.util.List;

/**
 * Created 24/07/2026 by SuperMartijn642
 */
public abstract sealed class FaceState implements Comparable<FaceState> {

    public static final FaceState ABSENT = new Absent();

    static FaceState get(boolean side1, boolean side2, boolean side3, boolean side4, int power){
        return VALUES[index(side1, side2, side3, side4, power)];
    }

    private static final FaceState[] VALUES;

    static{
        int states = (int)Math.pow(2, 4) * 16 + 1;
        VALUES = new FaceState[states];
        for(int state = 0; state < states - 1; state++)
            VALUES[state] = new Present(state);
        VALUES[states - 1] = ABSENT;
    }

    private static int index(boolean side1, boolean side2, boolean side3, boolean side4, int power){
        return (side1 ? 1 : 0) | (side2 ? 2 : 0) | (side3 ? 4 : 0) | (side4 ? 8 : 0) | (power << 4);
    }

    public static int sideIndex(Direction face, Direction side){
        if(face.getAxis() == side.getAxis())
            throw new IllegalArgumentException("Cannot get side " + side + " for face " + face + "!");
        return switch(face){
            case UP -> switch(side){
                case NORTH -> 2;
                case EAST -> 3;
                case SOUTH -> 0;
                case WEST -> 1;
                default -> throw new AssertionError();
            };
            case DOWN -> switch(side){
                case NORTH -> 0;
                case EAST -> 3;
                case SOUTH -> 2;
                case WEST -> 1;
                default -> throw new AssertionError();
            };
            case NORTH -> switch(side){
                case UP -> 2;
                case EAST -> 1;
                case DOWN -> 0;
                case WEST -> 3;
                default -> throw new AssertionError();
            };
            case SOUTH -> switch(side){
                case UP -> 2;
                case EAST -> 3;
                case DOWN -> 0;
                case WEST -> 1;
                default -> throw new AssertionError();
            };
            case EAST -> switch(side){
                case NORTH -> 3;
                case UP -> 2;
                case SOUTH -> 1;
                case DOWN -> 0;
                default -> throw new AssertionError();
            };
            case WEST -> switch(side){
                case NORTH -> 1;
                case UP -> 2;
                case SOUTH -> 3;
                case DOWN -> 0;
                default -> throw new AssertionError();
            };
        };
    }

    public static FaceState byIndex(int index){
        return VALUES[index];
    }

    public abstract boolean isPresent();

    public abstract boolean get(int side);

    public abstract FaceState set(int side, boolean value);

    public boolean get(Direction face, Direction side){
        return this.get(sideIndex(face, side));
    }

    public FaceState set(Direction face, Direction side, boolean value){
        return this.set(sideIndex(face, side), value);
    }

    public abstract int power();

    public abstract FaceState power(int power);

    public abstract int index();

    private static final class Absent extends FaceState {
        private static final String NAME = "absent";
        private static final int INDEX = (int)Math.pow(2, 4) * 16;

        @Override
        public int compareTo(FaceState o){
            return o == ABSENT ? 0 : -1;
        }

        @Override
        public boolean isPresent(){
            return false;
        }

        @Override
        public boolean get(int side){
            return false;
        }

        @Override
        public FaceState set(int side, boolean value){
            throw new IllegalStateException("Cannot set side for ABSENT!");
        }

        @Override
        public int power(){
            return 0;
        }

        @Override
        public FaceState power(int power){
            throw new IllegalStateException("Cannot set power for ABSENT!");
        }

        @Override
        public int index(){
            return INDEX;
        }

        @Override
        public String toString(){
            return NAME;
        }
    }

    private static final class Present extends FaceState {

        private final int index;
        private final String name;

        private Present(int index){
            this.index = index;
            List<String> sides = new ArrayList<>(4);
            for(int i = 0; i < 4; i++)
                if(this.get(i)) sides.add("side" + (i + 1));
            this.name = sides.isEmpty() ? "present" : String.join(",", sides) + ",power=" + this.power();
        }

        @Override
        public boolean isPresent(){
            return true;
        }

        @Override
        public boolean get(int side){
            return (this.index & (1 << side)) != 0;
        }

        @Override
        public FaceState set(int side, boolean value){
            return VALUES[value ? this.index | (1 << side) : this.index & ~(1 << side)];
        }

        @Override
        public int power(){
            return this.index >> 4;
        }

        @Override
        public FaceState power(int power){
            return VALUES[(this.index & ~(15 << 4)) | power << 4];
        }

        @Override
        public int index(){
            return this.index;
        }

        @Override
        public int compareTo(FaceState o){
            if(o == ABSENT)
                return 1;
            Present state = (Present)o;
            return this.index - state.index;
        }

        @Override
        public String toString(){
            return this.name;
        }

        @Override
        public boolean equals(Object o){
            if(o == null || this.getClass() != o.getClass()) return false;

            Present state = (Present)o;
            return this.index == state.index;
        }

        @Override
        public int hashCode(){
            return this.index;
        }
    }
}
