package com.supermartijn642.stickyredstone.generators;

import com.supermartijn642.core.generator.BlockStateGenerator;
import com.supermartijn642.core.generator.ResourceCache;
import com.supermartijn642.stickyredstone.StickyRedstone;
import com.supermartijn642.stickyredstone.content.StickyComparator;
import com.supermartijn642.stickyredstone.content.StickyRedstoneTorchBlock;
import com.supermartijn642.stickyredstone.content.StickyRepeater;
import com.supermartijn642.stickyredstone.content.wire.SingleStickyRedstoneDust;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.properties.ComparatorMode;
import org.joml.Vector3i;

/**
 * Created 24/07/2026 by SuperMartijn642
 */
public class StickyRedstoneBlockStateGenerator extends BlockStateGenerator {

    public StickyRedstoneBlockStateGenerator(String modid, ResourceCache cache){
        super(modid, cache);
    }

    @Override
    public void generate(){
        // Torch
        this.blockState(StickyRedstone.stickyRedstoneTorch)
            .variantsForAll((state, variant) -> {
                boolean lit = state.get(StickyRedstoneTorchBlock.LIT);
                Direction face = state.get(StickyRedstoneTorchBlock.FACE);
                int xRotation = 0;
                int yRotation = 0;
                if(face == Direction.UP)
                    xRotation = 180;
                else if(face != Direction.DOWN){
                    xRotation = 90;
                    yRotation = (int)face.toYRot();
                }
                variant.model(
                    lit ? "block/sticky_redstone_torch" : "block/sticky_redstone_torch_off",
                    xRotation,
                    yRotation
                );
            });

        // Dust
        BlockStateBuilder singleDust = this.blockState(StickyRedstone.singleStickyRedstoneDust);
        for(Direction face : Direction.values()){
            Vector3i rotation = new Vector3i();
            rotation.x = face == Direction.UP ? 180 : face == Direction.DOWN ? 0 : 90;
            rotation.y = face.getAxis().isVertical() ? 0 : (int)face.toYRot();
            singleDust.multipart(
                m -> m.requireProperty(SingleStickyRedstoneDust.FACE, face).requireProperty(SingleStickyRedstoneDust.SIDE1, false).requireProperty(SingleStickyRedstoneDust.SIDE2, false).requireProperty(SingleStickyRedstoneDust.SIDE3, false).requireProperty(SingleStickyRedstoneDust.SIDE4, false)
                    .or().requireProperty(SingleStickyRedstoneDust.FACE, face).requireProperty(SingleStickyRedstoneDust.SIDE1, true).requireProperty(SingleStickyRedstoneDust.SIDE2, true)
                    .or().requireProperty(SingleStickyRedstoneDust.FACE, face).requireProperty(SingleStickyRedstoneDust.SIDE2, true).requireProperty(SingleStickyRedstoneDust.SIDE3, true)
                    .or().requireProperty(SingleStickyRedstoneDust.FACE, face).requireProperty(SingleStickyRedstoneDust.SIDE3, true).requireProperty(SingleStickyRedstoneDust.SIDE4, true)
                    .or().requireProperty(SingleStickyRedstoneDust.FACE, face).requireProperty(SingleStickyRedstoneDust.SIDE4, true).requireProperty(SingleStickyRedstoneDust.SIDE1, true),
                v -> {
                    v.model(
                        StickyRedstone.identifier("block/sticky_redstone_dust_dot"),
                        rotation.x, rotation.y, rotation.z,
                        false,
                        1
                    );
                }
            );
            singleDust.multipart(
                m -> m.requireProperty(SingleStickyRedstoneDust.FACE, face)
                    .requireProperty(SingleStickyRedstoneDust.SIDE1, true),
                v -> {
                    v.model(
                        StickyRedstone.identifier("block/sticky_redstone_dust_side0"),
                        rotation.x, rotation.y, rotation.z,
                        false,
                        1
                    );
                }
            );
            singleDust.multipart(
                m -> m.requireProperty(SingleStickyRedstoneDust.FACE, face)
                    .requireProperty(SingleStickyRedstoneDust.SIDE3, true),
                v -> {
                    v.model(
                        StickyRedstone.identifier("block/sticky_redstone_dust_side_alt0"),
                        rotation.x, rotation.y, rotation.z,
                        false,
                        1
                    );
                }
            );
            switch(face.getAxis()){
                case X -> {
                    rotation.x = 0;
                    rotation.y = face.getAxisDirection() == Direction.AxisDirection.POSITIVE ? 180 : 0;
                    rotation.z = -face.getStepX() * 90;
                }
                case Y -> rotation.y += face.getStepY() * 90;
                case Z -> rotation.z += face.getStepZ() * 90;
            }
            singleDust.multipart(
                m -> m.requireProperty(SingleStickyRedstoneDust.FACE, face)
                    .requireProperty(SingleStickyRedstoneDust.SIDE2, true),
                v -> {
                    v.model(
                        StickyRedstone.identifier("block/sticky_redstone_dust_side1"),
                        rotation.x, rotation.y, rotation.z,
                        false,
                        1
                    );
                }
            );
            singleDust.multipart(
                m -> m.requireProperty(SingleStickyRedstoneDust.FACE, face)
                    .requireProperty(SingleStickyRedstoneDust.SIDE4, true),
                v -> {
                    v.model(
                        StickyRedstone.identifier("block/sticky_redstone_dust_side_alt1"),
                        rotation.x, rotation.y, rotation.z,
                        false,
                        1
                    );
                }
            );
        }
        this.blockState(StickyRedstone.denseStickyRedstoneDust)
            .emptyVariant(variant -> variant.model("block/sticky_redstone_dust_invalid"));

        // Repeater
        this.blockState(StickyRedstone.stickyRepeater)
            .variantsForAll((state, variant) -> {
                Direction face = state.get(StickyRepeater.FACE);
                int orientation = state.get(StickyRepeater.ORIENTATION);
                boolean powered = state.get(StickyRepeater.POWERED);
                boolean locked = state.get(StickyRepeater.LOCKED);
                int delay = state.get(StickyRepeater.DELAY);
                int xRotation = 0;
                int yRotation = 0;
                int zRotation = 0;
                if(face.getAxis() == Direction.Axis.Y){
                    if(face == Direction.UP){
                        xRotation = 180;
                        yRotation = 180;
                    }
                    yRotation += orientation * 90;
                }else switch(face){
                    case NORTH -> {
                        xRotation = -90;
                        zRotation = orientation * 90;
                    }
                    case SOUTH -> {
                        xRotation = 90;
                        zRotation = 180 - orientation * 90;
                    }
                    case EAST -> {
                        yRotation = 90 + orientation * 90;
                        zRotation = -90;
                    }
                    case WEST -> {
                        yRotation = -90 + orientation * 90;
                        zRotation = 90;
                    }
                }
                variant.model(
                    StickyRedstone.identifier("block/sticky_repeater" + delay + "_" + (powered ? "on" : "off") + (locked ? "_locked" : "")),
                    xRotation, yRotation, zRotation,
                    false,
                    1
                );
            });

        // Comparator
        this.blockState(StickyRedstone.stickyComparator)
            .variantsForAllExcept((state, variant) -> {
                Direction face = state.get(StickyComparator.FACE);
                int orientation = state.get(StickyComparator.ORIENTATION);
                boolean powered = state.get(StickyComparator.POWERED);
                ComparatorMode mode = state.get(StickyComparator.MODE);
                int xRotation = 0;
                int yRotation = 0;
                int zRotation = 0;
                if(face.getAxis() == Direction.Axis.Y){
                    if(face == Direction.UP){
                        xRotation = 180;
                        yRotation = 180;
                    }
                    yRotation += orientation * 90;
                }else switch(face){
                    case NORTH -> {
                        xRotation = -90;
                        zRotation = orientation * 90;
                    }
                    case SOUTH -> {
                        xRotation = 90;
                        zRotation = 180 - orientation * 90;
                    }
                    case EAST -> {
                        yRotation = 90 + orientation * 90;
                        zRotation = -90;
                    }
                    case WEST -> {
                        yRotation = -90 + orientation * 90;
                        zRotation = 90;
                    }
                }
                variant.model(
                    StickyRedstone.identifier("block/sticky_comparator_" + (mode == ComparatorMode.COMPARE ? "compare" : "subtract") + "_" + (powered ? "on" : "off")),
                    xRotation, yRotation, zRotation,
                    false,
                    1
                );
            }, StickyComparator.OUTPUT_SIGNAL);
    }
}
