package com.supermartijn642.stickyredstone.generators;

import com.supermartijn642.core.generator.LanguageGenerator;
import com.supermartijn642.core.generator.ResourceCache;
import com.supermartijn642.stickyredstone.StickyRedstone;

/**
 * Created 24/07/2026 by SuperMartijn642
 */
public class StickyRedstoneLanguageGenerator extends LanguageGenerator {

    public StickyRedstoneLanguageGenerator(String modid, ResourceCache cache){
        super(modid, cache, "en_us");
    }

    @Override
    public void generate(){
        this.itemGroup(StickyRedstone.CREATIVE_GROUP, "Sticky Redstone");
        this.block(StickyRedstone.stickyRedstoneTorch, "Sticky Redstone Torch");
        this.block(StickyRedstone.singleStickyRedstoneDust, "Sticky Redstone Dust");
        this.block(StickyRedstone.stickyRepeater, "Sticky Repeater");
        this.block(StickyRedstone.stickyComparator, "Sticky Comparator");
        this.translation("stickyredstone.item_hint", "Can be placed on any face");
    }
}
