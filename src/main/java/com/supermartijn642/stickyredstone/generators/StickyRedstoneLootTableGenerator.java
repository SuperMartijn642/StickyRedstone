package com.supermartijn642.stickyredstone.generators;

import com.supermartijn642.core.generator.LootTableGenerator;
import com.supermartijn642.core.generator.ResourceCache;
import com.supermartijn642.stickyredstone.StickyRedstone;
import com.supermartijn642.stickyredstone.content.wire.DenseStickyRedstoneDust;
import com.supermartijn642.stickyredstone.content.wire.DustCountNumberProvider;
import net.minecraft.core.Holder;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;

/**
 * Created 24/07/2026 by SuperMartijn642
 */
public class StickyRedstoneLootTableGenerator extends LootTableGenerator {

    public StickyRedstoneLootTableGenerator(String modid, ResourceCache cache){
        super(modid, cache);
    }

    @Override
    public void generate(){
        this.dropSelf(StickyRedstone.stickyRedstoneTorch);
        this.dropSelf(StickyRedstone.singleStickyRedstoneDust);
        this.lootTable(StickyRedstone.denseStickyRedstoneDust)
            .parameters(DenseStickyRedstoneDust.DUST_COUNT_PARAM_SET)
            .pool(pool ->
                pool.survivesExplosionCondition()
                    .function(SetItemCountFunction.setCount(Holder.direct(DustCountNumberProvider.INSTANCE)).build())
                    .itemEntry(StickyRedstone.stickyRedstoneDust)
            );
        this.dropSelf(StickyRedstone.stickyRepeater);
        this.dropSelf(StickyRedstone.stickyComparator);
    }
}
