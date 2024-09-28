package com.hasoook.hasoookmod.item;

import com.hasoook.hasoookmod.HasoookMod;
import com.hasoook.hasoookmod.item.custom.*;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.Rarity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(HasoookMod.MODID);

    public static final Supplier<Item> TOTEM_OF_SURRENDER = ITEMS.register("totem_of_surrender",() -> new TotemOfSurrender(new Item.Properties()));
    public static final Supplier<Item> ENCHANTMENT_BRUSH = ITEMS.register("enchantment_brush",() -> new EnchantmentBrush(new Item.Properties()));
    public static final Supplier<Item> WATER_BUCKET = ITEMS.register("water_bucket",() -> new WaterBucket(new Item.Properties()));
    public static final Supplier<Item> POKER = ITEMS.register("poker",() -> new Poker(new Item.Properties()));
    public static final Supplier<Item> POKER_CLUB = ITEMS.register("poker_club",() -> new Poker(new Item.Properties()));
    public static final Supplier<Item> POKER_DIAMOND = ITEMS.register("poker_diamond",() -> new Poker(new Item.Properties()));
    public static final Supplier<Item> POKER_HEART = ITEMS.register("poker_heart",() -> new Poker(new Item.Properties()));
    public static final Supplier<Item> POKER_SPADE = ITEMS.register("poker_spade",() -> new Poker(new Item.Properties()));
    public static final Supplier<Item> WATER_BOOT = ITEMS.register("water_boot",() -> new WaterBoot(new Item.Properties()));

    public static final DeferredItem<PickaxeItem> HUGE_DIAMOND_PICKAXE = ITEMS.register("huge_diamond_pickaxe",
            () -> new HugeDiamondPickaxe(ModToolTiers.Huge_Diamond, new Item.Properties()
                    .attributes(PickaxeItem.createAttributes(ModToolTiers.Huge_Diamond, 11.0F, -3.2f))
                    .rarity(Rarity.RARE)));

    public static void register(IEventBus eventBus){
        ITEMS.register(eventBus);
    }
}
