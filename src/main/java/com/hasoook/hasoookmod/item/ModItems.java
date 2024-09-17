package com.hasoook.hasoookmod.item;

import com.hasoook.hasoookmod.HasoookMod;
import com.hasoook.hasoookmod.item.custom.EnchantmentBrush;
import com.hasoook.hasoookmod.item.custom.Poker;
import com.hasoook.hasoookmod.item.custom.TotemOfSurrender;
import com.hasoook.hasoookmod.item.custom.WaterBucket;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Registries.ITEM, HasoookMod.MODID);

    public static final Supplier<Item> TOTEM_OF_SURRENDER = ITEMS.register("totem_of_surrender",() -> new TotemOfSurrender(new Item.Properties()));
    public static final Supplier<Item> ENCHANTMENT_BRUSH = ITEMS.register("enchantment_brush",() -> new EnchantmentBrush(new Item.Properties()));
    public static final Supplier<Item> WATER_BUCKET = ITEMS.register("water_bucket",() -> new WaterBucket(new Item.Properties()));
    public static final Supplier<Item> POKER = ITEMS.register("poker",() -> new Poker(new Item.Properties()));

    public static void register(IEventBus eventBus){
        ITEMS.register(eventBus);
    }
}
