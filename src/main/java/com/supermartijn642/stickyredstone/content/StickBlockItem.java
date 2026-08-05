package com.supermartijn642.stickyredstone.content;

import com.supermartijn642.core.TextComponents;
import com.supermartijn642.core.item.BaseBlockItem;
import com.supermartijn642.core.item.ItemProperties;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

import java.util.function.Consumer;

/**
 * Created 05/08/2026 by SuperMartijn642
 */
public class StickBlockItem extends BaseBlockItem {

    public StickBlockItem(Block block, ItemProperties properties){
        super(block, properties);
    }

    @Override
    protected void appendItemInformation(ItemStack stack, Consumer<Component> info, boolean advanced){
        info.accept(TextComponents.translation("stickyredstone.item_hint").color(ChatFormatting.GRAY).get());
    }
}
