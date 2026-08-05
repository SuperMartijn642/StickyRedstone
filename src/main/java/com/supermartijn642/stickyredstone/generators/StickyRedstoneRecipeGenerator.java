package com.supermartijn642.stickyredstone.generators;

import com.supermartijn642.core.generator.RecipeGenerator;
import com.supermartijn642.core.generator.ResourceCache;
import com.supermartijn642.stickyredstone.StickyRedstone;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags;
import net.minecraft.world.item.Items;

/**
 * Created 24/07/2026 by SuperMartijn642
 */
public class StickyRedstoneRecipeGenerator extends RecipeGenerator {

    public StickyRedstoneRecipeGenerator(String modid, ResourceCache cache){
        super(modid, cache);
    }

    @Override
    public void generate(){
        // Torch
        this.shapeless("sticky_redstone_torch_conversion", StickyRedstone.stickyRedstoneTorch)
            .input(Items.REDSTONE_TORCH)
            .input(ConventionalItemTags.SLIME_BALLS)
            .unlockedBy(ConventionalItemTags.SLIME_BALLS);
        this.shaped(StickyRedstone.stickyRedstoneTorch)
            .pattern("D")
            .pattern("S")
            .input('D', StickyRedstone.stickyRedstoneDust)
            .input('S', ConventionalItemTags.WOODEN_RODS)
            .unlockedBy(ConventionalItemTags.SLIME_BALLS);

        // Dust
        this.shaped(StickyRedstone.stickyRedstoneDust, 8)
            .pattern("DDD")
            .pattern("DSD")
            .pattern("DDD")
            .input('D', ConventionalItemTags.REDSTONE_DUSTS)
            .input('S', ConventionalItemTags.SLIME_BALLS)
            .unlockedBy(ConventionalItemTags.SLIME_BALLS);

        // Repeater
        this.shapeless("sticky_repeater_conversion", StickyRedstone.stickyRepeater)
            .input(Items.REPEATER)
            .input(ConventionalItemTags.SLIME_BALLS)
            .unlockedBy(ConventionalItemTags.SLIME_BALLS);
        this.shaped(StickyRedstone.stickyRepeater)
            .pattern("TDT")
            .pattern("SSS")
            .input('T', Items.REDSTONE_TORCH, StickyRedstone.stickyRedstoneTorch)
            .input('D', StickyRedstone.stickyRedstoneDust)
            .input('S', ConventionalItemTags.STONES)
            .unlockedBy(ConventionalItemTags.SLIME_BALLS);

        // Comparator
        this.shapeless("sticky_comparator_conversion", StickyRedstone.stickyComparator)
            .input(Items.COMPARATOR)
            .input(ConventionalItemTags.SLIME_BALLS)
            .unlockedBy(ConventionalItemTags.SLIME_BALLS);
        this.shaped(StickyRedstone.stickyComparator)
            .pattern(" Y ")
            .pattern("TQT")
            .pattern("SSS")
            .input('Y', StickyRedstone.stickyRedstoneTorch)
            .input('T', Items.REDSTONE_TORCH, StickyRedstone.stickyRedstoneTorch)
            .input('Q', ConventionalItemTags.QUARTZ_GEMS)
            .input('S', ConventionalItemTags.STONES)
            .unlockedBy(ConventionalItemTags.SLIME_BALLS);
    }
}
