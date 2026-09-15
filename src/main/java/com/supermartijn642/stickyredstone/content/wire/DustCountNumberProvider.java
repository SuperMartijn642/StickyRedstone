package com.supermartijn642.stickyredstone.content.wire;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProvider;

/**
 * Created 05/08/2026 by SuperMartijn642
 */
public class DustCountNumberProvider implements ContextIntProvider {

    public static final DustCountNumberProvider INSTANCE = new DustCountNumberProvider();
    public static final MapCodec<DustCountNumberProvider> CODEC = MapCodec.unit(INSTANCE);

    private DustCountNumberProvider(){
    }

    @Override
    public int getIntUnsafe(LootContext context) throws ArithmeticException{
        Integer count = context.getOptional(DenseStickyRedstoneDust.DUST_COUNT);
        return count == null ? 1 : count;
    }

    @Override
    public void validate(ValidationContext context){
    }

    @Override
    public MapCodec<? extends ContextIntProvider> codec(){
        return CODEC;
    }
}
