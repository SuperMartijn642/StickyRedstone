package com.supermartijn642.stickyredstone;

import com.supermartijn642.core.block.BaseBlockEntityType;
import com.supermartijn642.core.block.BlockProperties;
import com.supermartijn642.core.item.BaseBlockItem;
import com.supermartijn642.core.item.CreativeItemGroup;
import com.supermartijn642.core.item.ItemProperties;
import com.supermartijn642.core.registry.GeneratorRegistrationHandler;
import com.supermartijn642.core.registry.RegistrationHandler;
import com.supermartijn642.core.registry.RegistryEntryAcceptor;
import com.supermartijn642.stickyredstone.content.StickBlockItem;
import com.supermartijn642.stickyredstone.content.StickyComparator;
import com.supermartijn642.stickyredstone.content.StickyRepeater;
import com.supermartijn642.stickyredstone.content.wire.*;
import com.supermartijn642.stickyredstone.content.StickyRedstoneTorchBlock;
import com.supermartijn642.stickyredstone.generators.*;
import net.fabricmc.api.ModInitializer;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;

/**
 * Created 7/7/2020 by SuperMartijn642
 */
public class StickyRedstone implements ModInitializer {

    public static final String MODID = "stickyredstone";

    public static Identifier identifier(String path){
        return Identifier.fromNamespaceAndPath(MODID, path);
    }

    public static final CreativeItemGroup CREATIVE_GROUP = CreativeItemGroup.create(MODID, () -> StickyRedstone.stickyRedstoneTorch.asItem());

    @RegistryEntryAcceptor(namespace = MODID, identifier = "sticky_redstone_torch", registry = RegistryEntryAcceptor.Registry.BLOCKS)
    public static StickyRedstoneTorchBlock stickyRedstoneTorch;
    @RegistryEntryAcceptor(namespace = MODID, identifier = "sticky_redstone_dust", registry = RegistryEntryAcceptor.Registry.ITEMS)
    public static StickyRedstoneDustItem stickyRedstoneDust;
    @RegistryEntryAcceptor(namespace = MODID, identifier = "single_sticky_redstone_dust", registry = RegistryEntryAcceptor.Registry.BLOCKS)
    public static SingleStickyRedstoneDust singleStickyRedstoneDust;
    @RegistryEntryAcceptor(namespace = MODID, identifier = "dense_sticky_redstone_dust", registry = RegistryEntryAcceptor.Registry.BLOCKS)
    public static DenseStickyRedstoneDust denseStickyRedstoneDust;
    @RegistryEntryAcceptor(namespace = MODID, identifier = "dense_sticky_redstone_dust", registry = RegistryEntryAcceptor.Registry.BLOCK_ENTITY_TYPES)
    public static BaseBlockEntityType<DenseStickyRedstoneDustBlockEntity> denseStickyRedstoneDustEntity;
    @RegistryEntryAcceptor(namespace = MODID, identifier = "sticky_repeater", registry = RegistryEntryAcceptor.Registry.BLOCKS)
    public static StickyRepeater stickyRepeater;
    @RegistryEntryAcceptor(namespace = MODID, identifier = "sticky_comparator", registry = RegistryEntryAcceptor.Registry.BLOCKS)
    public static StickyComparator stickyComparator;

    @Override
    public void onInitialize(){
        register();
        registerGenerators();
    }

    private static void register(){
        RegistrationHandler handler = RegistrationHandler.get(MODID);
        handler.registerBlock("sticky_redstone_torch", StickyRedstoneTorchBlock::new);
        handler.registerItem("sticky_redstone_torch", () -> new StickBlockItem(stickyRedstoneTorch, ItemProperties.create().group(CREATIVE_GROUP)));
        handler.registerBlock("single_sticky_redstone_dust", SingleStickyRedstoneDust::new);
        handler.registerBlock("dense_sticky_redstone_dust", DenseStickyRedstoneDust::new);
        handler.registerBlockEntityType("dense_sticky_redstone_dust", () -> BaseBlockEntityType.create(DenseStickyRedstoneDustBlockEntity::new, denseStickyRedstoneDust));
        handler.registerItem("sticky_redstone_dust", () -> new StickyRedstoneDustItem(singleStickyRedstoneDust, ItemProperties.create().group(CREATIVE_GROUP)));
        handler.registerBlock("sticky_repeater", StickyRepeater::new);
        handler.registerItem("sticky_repeater", () -> new StickBlockItem(stickyRepeater, ItemProperties.create().group(CREATIVE_GROUP)));
        handler.registerBlock("sticky_comparator", StickyComparator::new);
        handler.registerItem("sticky_comparator", () -> new StickBlockItem(stickyComparator, ItemProperties.create().group(CREATIVE_GROUP)));
        Registry.register(BuiltInRegistries.LOOT_NUMBER_PROVIDER_TYPE, identifier("dust_count"), DustCountNumberProvider.CODEC);
        LootContextParamSets.REGISTRY.put(identifier("dust_count"), DenseStickyRedstoneDust.DUST_COUNT_PARAM_SET);
    }

    public static void registerGenerators(){
        GeneratorRegistrationHandler handler = GeneratorRegistrationHandler.get(MODID);
        handler.addGenerator(cache -> new StickyRedstoneBlockStateGenerator(MODID, cache));
        handler.addGenerator(cache -> new StickyRedstoneItemInfoGenerator(MODID, cache));
        handler.addGenerator(cache -> new StickyRedstoneLanguageGenerator(MODID, cache));
        handler.addGenerator(cache -> new StickyRedstoneLootTableGenerator(MODID, cache));
        handler.addGenerator(cache -> new StickyRedstoneModelGenerator(MODID, cache));
        handler.addGenerator(cache -> new StickyRedstoneRecipeGenerator(MODID, cache));
    }
}
