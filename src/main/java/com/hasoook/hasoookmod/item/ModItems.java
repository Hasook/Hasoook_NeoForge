package com.hasoook.hasoookmod.item;

import com.hasoook.hasoookmod.HasoookMod;
import com.hasoook.hasoookmod.item.custom.*;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.Rarity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(HasoookMod.MOD_ID);

    public static final Supplier<Item> TOTEM_OF_SURRENDER = ITEMS.register("totem_of_surrender",() -> new TotemOfSurrender(new Item.Properties()));
    public static final Supplier<Item> ENCHANTMENT_BRUSH = ITEMS.register("enchantment_brush",() -> new EnchantmentBrush(new Item.Properties()));
    public static final Supplier<Item> WATER_BUCKET = ITEMS.register("water_bucket",() -> new WaterBucket(new Item.Properties()));
    public static final Supplier<Item> SPIT = ITEMS.register("spit",() -> new Spit(new Item.Properties()));
    public static final Supplier<Item> RIPEN_FLINT_AND_STEEL = ITEMS.register("ripen_flint_and_steel",() -> new RipenFlintAndSteel(new Item.Properties().food(ModFoods.RIPENFlintAndSteel)));

    public static final DeferredItem<Item> WATER_BOOTS = ITEMS.register("water_boots",
            () -> new WaterBoots(ModArmorMaterials.WATER, ArmorItem.Type.BOOTS,
                    new Item.Properties().durability(ArmorItem.Type.BOOTS.getDurability(5))));

    public static final DeferredItem<PickaxeItem> HUGE_DIAMOND_PICKAXE = ITEMS.register("huge_diamond_pickaxe",
            () -> new HugeDiamondPickaxe(ModToolTiers.Huge_Diamond, new Item.Properties()
                    .attributes(PickaxeItem.createAttributes(ModToolTiers.Huge_Diamond, 11.0F, -3.2f))
                    .rarity(Rarity.RARE)));

    public static void register(IEventBus eventBus){
        ITEMS.register(eventBus);
    }
}
