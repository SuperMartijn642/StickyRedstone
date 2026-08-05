package com.supermartijn642.stickyredstone.generators;

import com.supermartijn642.core.generator.ModelGenerator;
import com.supermartijn642.core.generator.ResourceCache;
import com.supermartijn642.stickyredstone.StickyRedstone;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.state.properties.ComparatorMode;

/**
 * Created 24/07/2026 by SuperMartijn642
 */
public class StickyRedstoneModelGenerator extends ModelGenerator {

    public StickyRedstoneModelGenerator(String modid, ResourceCache cache){
        super(modid, cache);
    }

    @Override
    public void generate(){
        // Torch
        this.model("block/sticky_redstone_torch")
            .texture("torch", Identifier.withDefaultNamespace("block/redstone_torch"))
            .texture("slime", "block/sticky_redstone_torch_base")
            .particleTexture("#torch")
            .ambientOcclusion(false)
            .element(element ->
                element.shape(5, -0.1f, 5, 11, 0.25f, 11)
                    .face(Direction.UP, face -> face.texture("slime"))
                    .face(Direction.DOWN, face -> face.texture("slime").cullface())
            )
            .element(element ->
                element.shape(7, 0, 7, 9, 10, 9)
                    .allFaces(face -> face.texture("torch"))
                    .face(Direction.UP, face -> face.uv(7, 6, 9, 8))
                    .face(Direction.DOWN, face -> face.uv(7, 13, 9, 15).cullface())
            )
            .element(element ->
                element.from(6.5f, 7.5f, 6.5f).to(9.5f, 7.5f, 9.5f)
                    .shading(false)
                    .face(Direction.UP, face -> face.uv(8, 5, 9, 6).texture("torch"))
            )
            .element(element ->
                element.from(6.5f, 10.5f, 6.5f).to(9.5f, 10.5f, 9.5f)
                    .shading(false)
                    .face(Direction.DOWN, face -> face.uv(7, 5, 8, 6).texture("torch"))
            )
            .element(element ->
                element.from(6.5f, 7.5f, 6.5f).to(9.5f, 10.5f, 6.5f)
                    .shading(false)
                    .face(Direction.SOUTH, face -> face.uv(9, 6, 10, 7).texture("torch"))
            )
            .element(element ->
                element.from(9.5f, 7.5f, 6.5f).to(9.5f, 10.5f, 9.5f)
                    .shading(false)
                    .face(Direction.WEST, face -> face.uv(6, 7, 7, 8).texture("torch"))
            )
            .element(element ->
                element.from(6.5f, 7.5f, 9.5f).to(9.5f, 10.5f, 9.5f)
                    .shading(false)
                    .face(Direction.NORTH, face -> face.uv(6, 6, 7, 7).texture("torch"))
            )
            .element(element ->
                element.from(6.5f, 7.5f, 6.5f).to(6.5f, 10.5f, 9.5f)
                    .shading(false)
                    .face(Direction.EAST, face -> face.uv(9, 7, 10, 8).texture("torch"))
            );
        this.model("block/sticky_redstone_torch_off")
            .parent("block/sticky_redstone_torch")
            .texture("torch", Identifier.withDefaultNamespace("block/redstone_torch_off"));
        this.itemGenerated("item/sticky_redstone_torch", StickyRedstone.identifier("item/sticky_redstone_torch"));

        // Dust
        this.model("block/sticky_redstone_dust_dot")
            .texture("dust", Identifier.withDefaultNamespace("block/redstone_dust_dot"))
            .texture("slime", "block/sticky_redstone_dust_base_dot")
            .texture("overlay", Identifier.withDefaultNamespace("block/redstone_dust_overlay")) // This is just a blank texture in vanilla, but add it anyway in case resource packs use it
            .particleTexture("#dust")
            .ambientOcclusion(false)
            .element(element ->
                element.shape(0, -0.1f, 0, 16, 0.1f, 16)
                    .face(Direction.UP, face -> face.texture("slime"))
                    .face(Direction.DOWN, face -> face.uv(0, 16, 16, 0).texture("slime").cullface())
            )
            .element(element ->
                element.from(0, 0.25f, 0).to(16, 0.25f, 16)
                    .shading(false)
                    .face(Direction.UP, face -> face.texture("dust").tintIndex(0))
                    .face(Direction.DOWN, face -> face.uv(0, 16, 16, 0).texture("dust").tintIndex(0).cullface())
            )
            .element(element ->
                element.from(0, 0.25f, 0).to(16, 0.25f, 16)
                    .shading(false)
                    .face(Direction.UP, face -> face.texture("overlay"))
                    .face(Direction.DOWN, face -> face.uv(0, 16, 16, 0).texture("overlay").cullface())
            );
        this.model("block/sticky_redstone_dust_side")
            .texture("overlay", Identifier.withDefaultNamespace("block/redstone_dust_overlay")) // This is just a blank texture in vanilla, but add it anyway in case resource packs use it
            .particleTexture(Identifier.withDefaultNamespace("block/redstone_dust_dot"))
            .ambientOcclusion(false)
            .element(element ->
                element.shape(0, -0.1f, 0, 16, 0.1f, 8)
                    .face(Direction.UP, face -> face.uv(0, 0, 16, 8).texture("slime"))
                    .face(Direction.DOWN, face -> face.uv(0, 8, 16, 0).texture("slime").cullface())
            )
            .element(element ->
                element.from(0, 0.25f, 0).to(16, 0.25f, 8)
                    .shading(false)
                    .face(Direction.UP, face -> face.uv(0, 0, 16, 8).texture("dust").tintIndex(0))
                    .face(Direction.DOWN, face -> face.uv(0, 8, 16, 0).texture("dust").tintIndex(0).cullface())
            )
            .element(element ->
                element.from(0, 0.25f, 0).to(16, 0.25f, 8)
                    .shading(false)
                    .face(Direction.UP, face -> face.uv(0, 0, 16, 8).texture("overlay"))
                    .face(Direction.DOWN, face -> face.uv(0, 8, 16, 0).texture("overlay").cullface())
            );
        this.model("block/sticky_redstone_dust_side_alt")
            .texture("overlay", Identifier.withDefaultNamespace("block/redstone_dust_overlay")) // This is just a blank texture in vanilla, but add it anyway in case resource packs use it
            .particleTexture(Identifier.withDefaultNamespace("block/redstone_dust_dot"))
            .ambientOcclusion(false)
            .element(element ->
                element.shape(0, -0.1f, 8, 16, 0.1f, 16)
                    .face(Direction.UP, face -> face.uv(0, 8, 16, 16).texture("slime"))
                    .face(Direction.DOWN, face -> face.uv(0, 16, 16, 8).texture("slime").cullface())
            )
            .element(element ->
                element.from(0, 0.25f, 8).to(16, 0.25f, 16)
                    .shading(false)
                    .face(Direction.UP, face -> face.uv(0, 8, 16, 16).texture("dust").tintIndex(0))
                    .face(Direction.DOWN, face -> face.uv(0, 16, 16, 8).texture("dust").tintIndex(0).cullface())
            )
            .element(element ->
                element.from(0, 0.25f, 8).to(16, 0.25f, 16)
                    .shading(false)
                    .face(Direction.UP, face -> face.uv(0, 8, 16, 16).texture("overlay"))
                    .face(Direction.DOWN, face -> face.uv(0, 16, 16, 8).texture("overlay").cullface())
            );
        this.model("block/sticky_redstone_dust_side0")
            .parent("block/sticky_redstone_dust_side")
            .texture("dust", Identifier.withDefaultNamespace("block/redstone_dust_line0"))
            .texture("slime", "block/sticky_redstone_dust_base_line0");
        this.model("block/sticky_redstone_dust_side1")
            .parent("block/sticky_redstone_dust_side")
            .texture("dust", Identifier.withDefaultNamespace("block/redstone_dust_line1"))
            .texture("slime", "block/sticky_redstone_dust_base_line1");
        this.model("block/sticky_redstone_dust_side_alt0")
            .parent("block/sticky_redstone_dust_side_alt")
            .texture("dust", Identifier.withDefaultNamespace("block/redstone_dust_line0"))
            .texture("slime", "block/sticky_redstone_dust_base_line0");
        this.model("block/sticky_redstone_dust_side_alt1")
            .parent("block/sticky_redstone_dust_side_alt")
            .texture("dust", Identifier.withDefaultNamespace("block/redstone_dust_line1"))
            .texture("slime", "block/sticky_redstone_dust_base_line1");
        this.model("block/sticky_redstone_dust_invalid")
            .texture("all", "block/sticky_redstone_dust_invalid")
            .particleTexture("#all")
            .element(element ->
                element.shape(3, 3, 3, 13, 13, 13)
                    .shading(false)
                    .allFaces(face -> face.texture("all"))
            );
        this.itemGenerated("item/sticky_redstone_dust", StickyRedstone.identifier("item/sticky_redstone_dust"));

        // Repeater
        for(int delay = 1; delay <= 4; delay++){
            createRepeater(this.model("block/sticky_repeater" + delay + "_on"), delay, true, false);
            createRepeater(this.model("block/sticky_repeater" + delay + "_off"), delay, false, false);
            createRepeater(this.model("block/sticky_repeater" + delay + "_on_locked"), delay, true, true);
            createRepeater(this.model("block/sticky_repeater" + delay + "_off_locked"), delay, false, true);
        }
        this.itemGenerated("item/sticky_repeater", StickyRedstone.identifier("item/sticky_repeater"));

        // Comparator
        createComparator(this.model("block/sticky_comparator_compare_on"), ComparatorMode.COMPARE, true);
        createComparator(this.model("block/sticky_comparator_compare_off"), ComparatorMode.COMPARE, false);
        createComparator(this.model("block/sticky_comparator_subtract_on"), ComparatorMode.SUBTRACT, true);
        createComparator(this.model("block/sticky_comparator_subtract_off"), ComparatorMode.SUBTRACT, false);
        this.itemGenerated("item/sticky_comparator", StickyRedstone.identifier("item/sticky_comparator"));
    }

    private static void createRepeater(ModelBuilder model, int delay, boolean on, boolean locked){
        // Base
        model.texture("base", Identifier.withDefaultNamespace("block/smooth_stone"))
            .texture("base_top", Identifier.withDefaultNamespace(on ? "block/repeater_on" : "block/repeater"))
            .texture("slime", "block/sticky_repeater_base")
            .particleTexture("#base_top")
            .ambientOcclusion(false)
            .element(element ->
                element.shape(0, 0, 0, 16, 2, 16)
                    .face(Direction.UP, face -> face.texture("base_top"))
                    .face(Direction.DOWN, face -> face.texture("base").cullface())
                    .face(Direction.NORTH, face -> face.uv(0, 14, 16, 16).texture("base").cullface())
                    .face(Direction.EAST, face -> face.uv(0, 14, 16, 16).texture("base").cullface())
                    .face(Direction.SOUTH, face -> face.uv(0, 14, 16, 16).texture("base").cullface())
                    .face(Direction.WEST, face -> face.uv(0, 14, 16, 16).texture("base").cullface())
            ).element(element ->
                element.shape(-0.1f, -2, -0.1f, 16.1f, 2, 16.1f)
                    .face(Direction.NORTH, face -> face.uv(0, 6, 16, 10).texture("slime").cullface())
                    .face(Direction.EAST, face -> face.uv(0, 6, 16, 10).texture("slime").cullface())
                    .face(Direction.SOUTH, face -> face.uv(0, 6, 16, 10).texture("slime").cullface())
                    .face(Direction.WEST, face -> face.uv(0, 6, 16, 10).texture("slime").cullface())
            );
        // Lock
        if(locked){
            float offset = (delay - 1) * 2;
            model.texture("lock", Identifier.withDefaultNamespace("block/bedrock"))
                .element(element ->
                    element.shape(2, 2, 6 + offset, 14, 4, 8 + offset)
                        .face(Direction.UP, face -> face.uv(7, 2, 9, 14).texture("lock").rotation(90))
                        .face(Direction.NORTH, face -> face.uv(7, 2, 9, 14).texture("lock"))
                        .face(Direction.EAST, face -> face.uv(6, 7, 8, 9).texture("lock"))
                        .face(Direction.SOUTH, face -> face.uv(2, 7, 14, 9).texture("lock"))
                        .face(Direction.WEST, face -> face.uv(2, 7, 14, 9).texture("lock"))
                );
        }
        // Front torch
        model.texture("torch", Identifier.withDefaultNamespace(on ? "block/redstone_torch" : "block/redstone_torch_off"));
        createDiodeTorch(model, 7, 2, on, "torch");
        // Back torch
        if(!locked){
            float offset = (delay - 1) * 2;
            createDiodeTorch(model, 7, 6 + offset, on, "torch");
        }
    }

    private static void createComparator(ModelBuilder model, ComparatorMode mode, boolean on){
        // Base
        model.texture("base", Identifier.withDefaultNamespace("block/smooth_stone"))
            .texture("base_top", Identifier.withDefaultNamespace(on ? "block/comparator_on" : "block/comparator"))
            .texture("slime", "block/sticky_repeater_base")
            .particleTexture("#base_top")
            .ambientOcclusion(false)
            .element(element ->
                element.shape(0, 0, 0, 16, 2, 16)
                    .face(Direction.UP, face -> face.texture("base_top"))
                    .face(Direction.DOWN, face -> face.texture("base").cullface())
                    .face(Direction.NORTH, face -> face.uv(0, 14, 16, 16).texture("base").cullface())
                    .face(Direction.EAST, face -> face.uv(0, 14, 16, 16).texture("base").cullface())
                    .face(Direction.SOUTH, face -> face.uv(0, 14, 16, 16).texture("base").cullface())
                    .face(Direction.WEST, face -> face.uv(0, 14, 16, 16).texture("base").cullface())
            ).element(element ->
                element.shape(-0.1f, -2, -0.1f, 16.1f, 2, 16.1f)
                    .face(Direction.NORTH, face -> face.uv(0, 6, 16, 10).texture("slime").cullface())
                    .face(Direction.EAST, face -> face.uv(0, 6, 16, 10).texture("slime").cullface())
                    .face(Direction.SOUTH, face -> face.uv(0, 6, 16, 10).texture("slime").cullface())
                    .face(Direction.WEST, face -> face.uv(0, 6, 16, 10).texture("slime").cullface())
            );
        // Front torch
        model.texture("torch_on", Identifier.withDefaultNamespace("block/redstone_torch"))
            .texture("torch_off", Identifier.withDefaultNamespace("block/redstone_torch_off"));
        createDiodeTorch(model, 7, 2, mode == ComparatorMode.SUBTRACT, mode == ComparatorMode.SUBTRACT ? "torch_on" : "torch_off");
        // Back torches
        createDiodeTorch(model, 4, 11, on, on ? "torch_on" : "torch_off");
        createDiodeTorch(model, 10, 11, on, on ? "torch_on" : "torch_off");
    }

    private static void createDiodeTorch(ModelBuilder model, float x, float z, boolean on, String texture){
        model.element(element ->
            element.shape(0 + x, 2, 0 + z, 2 + x, 7, 2 + z)
                .shading(!on)
                .face(Direction.UP, face -> face.uv(7, 6, 9, 8).texture(texture))
                .face(Direction.NORTH, face -> face.uv(7, 6, 9, 11).texture(texture))
                .face(Direction.EAST, face -> face.uv(7, 6, 9, 11).texture(texture))
                .face(Direction.SOUTH, face -> face.uv(7, 6, 9, 11).texture(texture))
                .face(Direction.WEST, face -> face.uv(7, 6, 9, 11).texture(texture))
        );
        if(on){
            model.element(element ->
                    element.shape(-0.5f + x, 4.5f, -0.5f + z, 2.5f + x, 4.5f, 2.5f + z)
                        .shading(false)
                        .face(Direction.UP, face -> face.uv(8, 5, 9, 6).texture(texture))
                )
                .element(element ->
                    element.shape(-0.5f + x, 7.5f, -0.5f + z, 2.5f + x, 7.5f, 2.5f + z)
                        .shading(false)
                        .face(Direction.DOWN, face -> face.uv(7, 5, 8, 6).texture(texture))
                )
                .element(element ->
                    element.shape(-0.5f + x, 4.5f, 2.5f + z, 2.5f + x, 7.5f, 2.5f + z)
                        .shading(false)
                        .face(Direction.NORTH, face -> face.uv(6, 6, 7, 7).texture(texture))
                )
                .element(element ->
                    element.shape(-0.5f + x, 4.5f, -0.5f + z, 2.5f + x, 7.5f, -0.5f + z)
                        .shading(false)
                        .face(Direction.SOUTH, face -> face.uv(9, 6, 10, 7).texture(texture))
                )
                .element(element ->
                    element.shape(-0.5f + x, 4.5f, -0.5f + z, -0.5f + x, 7.5f, 2.5f + z)
                        .shading(false)
                        .face(Direction.EAST, face -> face.uv(9, 7, 10, 8).texture(texture))
                )
                .element(element ->
                    element.shape(2.5f + x, 4.5f, -0.5f + z, 2.5f + x, 7.5f, 2.5f + z)
                        .shading(false)
                        .face(Direction.WEST, face -> face.uv(6, 7, 7, 8).texture(texture))
                );
        }
    }
}
