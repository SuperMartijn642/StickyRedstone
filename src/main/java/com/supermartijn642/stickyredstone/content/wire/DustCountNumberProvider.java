package com.supermartijn642.stickyredstone.content.wire;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.providers.number.LootNumberProviderType;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;

/**
 * Created 05/08/2026 by SuperMartijn642
 */
public class DustCountNumberProvider implements NumberProvider {

    public static final DustCountNumberProvider INSTANCE = new DustCountNumberProvider();
    private static final MapCodec<DustCountNumberProvider> CODEC = MapCodec.unit(INSTANCE);
    public static final LootNumberProviderType TYPE = new LootNumberProviderType(CODEC);

    private DustCountNumberProvider(){
    }

    @Override
    public float getFloat(LootContext context){
        Integer count = context.getParamOrNull(DenseStickyRedstoneDust.DUST_COUNT);
        return count == null ? 1 : count;
    }

    @Override
    public LootNumberProviderType getType(){
        return TYPE;
    }
}
