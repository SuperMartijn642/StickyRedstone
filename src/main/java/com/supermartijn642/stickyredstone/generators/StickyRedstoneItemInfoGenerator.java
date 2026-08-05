package com.supermartijn642.stickyredstone.generators;

import com.supermartijn642.core.generator.ItemInfoGenerator;
import com.supermartijn642.core.generator.ResourceCache;
import com.supermartijn642.stickyredstone.StickyRedstone;

/**
 * Created 24/07/2026 by SuperMartijn642
 */
public class StickyRedstoneItemInfoGenerator extends ItemInfoGenerator {

    public StickyRedstoneItemInfoGenerator(String modid, ResourceCache cache){
        super(modid, cache);
    }

    @Override
    public void generate(){
        this.simpleInfo(StickyRedstone.stickyRedstoneTorch, "item/sticky_redstone_torch");
        this.simpleInfo(StickyRedstone.singleStickyRedstoneDust, "item/sticky_redstone_dust");
        this.simpleInfo(StickyRedstone.stickyRepeater, "item/sticky_repeater");
        this.simpleInfo(StickyRedstone.stickyComparator, "item/sticky_comparator");
    }
}
