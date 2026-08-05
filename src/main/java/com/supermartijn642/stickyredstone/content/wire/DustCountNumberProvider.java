package com.supermartijn642.stickyredstone.content.wire;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;

/**
 * Created 05/08/2026 by SuperMartijn642
 */
public class DustCountNumberProvider implements NumberProvider {

    public static final DustCountNumberProvider INSTANCE = new DustCountNumberProvider();
    public static final MapCodec<DustCountNumberProvider> CODEC = MapCodec.unit(INSTANCE);

    private DustCountNumberProvider(){
    }

    @Override
    public float getFloat(LootContext context){
        Integer count = context.getOptionalParameter(DenseStickyRedstoneDust.DUST_COUNT);
        return count == null ? 1 : count;
    }

    @Override
    public MapCodec<? extends NumberProvider> codec(){
        return CODEC;
    }
}
