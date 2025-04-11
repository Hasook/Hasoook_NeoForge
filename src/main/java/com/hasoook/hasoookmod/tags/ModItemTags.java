package com.hasoook.hasoookmod.tags;

import com.hasoook.hasoookmod.HasoookMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public class ModItemTags {
    public static final TagKey<Item> SEPARATION_ITEMS = bind("separation_items");
    public static final TagKey<Item> ENTITY_ENCHANTMENT = bind("entity_enchantment");
    public static final TagKey<Item> LOUIS_XVI = bind("louis_xvi");
    public static final TagKey<Item> TORNADO_ITEMS = bind("tornado_items");
    public static final TagKey<Item> RACIAL_DISCRIMINATION_ITEMS = bind("racial_discrimination_items");
    public static final TagKey<Item> GRAVITY_GLOVE = bind("gravity_glove");
    private static TagKey<Item> bind(String name) {
        return TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(HasoookMod.MOD_ID,name));
    }
}
