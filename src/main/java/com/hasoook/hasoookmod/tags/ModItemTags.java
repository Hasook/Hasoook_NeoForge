package com.hasoook.hasoookmod.tags;

import com.hasoook.hasoookmod.HasoookMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public class ModItemTags {
    public static final TagKey<Item> SEPARATION_ITEMS = bind("enchantable/separation_items");
    public static final TagKey<Item> DISDAIN_ITEMS = bind("disdain_items");
    public static final TagKey<Item> COMMON_TAG = bind("common_tag");
    public static final TagKey<Item> ENTITY_ENCHANTMENT = bind("entity_enchantment");
    private static TagKey<Item> bind(String name) {
        return TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(HasoookMod.MOD_ID,name));
    }
}
