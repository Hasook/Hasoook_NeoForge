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
    public static final Supplier<Item> PET_COMPASS = ITEMS.register("pet_compass",() -> new PetCompassItem(new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON)));
    public static final Supplier<Item> DISK_CRICKET_MOTION_BADGE = ITEMS.register("disk_cricket_motion_badge",() -> new DiskCricketMotionBadge(new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON)));
    public static final Supplier<Item> INSIGHT_MAGNIFIER = ITEMS.register("insight_magnifier",() -> new InsightMagnifier(new Item.Properties().stacksTo(1).rarity(Rarity.RARE)));
    public static final Supplier<Item> GAMES_CONSOLE = ITEMS.register("games_console",() -> new GamesConsole(new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON)));

    public static final DeferredItem<Item> WATER_BOOTS = ITEMS.register("water_boots",
            () -> new WaterBoots(ModArmorMaterials.WATER, ArmorItem.Type.BOOTS,
                    new Item.Properties().durability(ArmorItem.Type.BOOTS.getDurability(5))));

    public static final DeferredItem<PickaxeItem> HUGE_DIAMOND_PICKAXE = ITEMS.register("huge_diamond_pickaxe",
            () -> new HugeDiamondPickaxe(ModToolTiers.HUGE_DIAMOND, new Item.Properties()
                    .attributes(PickaxeItem.createAttributes(ModToolTiers.HUGE_DIAMOND, 11.0F, -3.2f))
                    .rarity(Rarity.RARE)));

    public static final Supplier<Item> GRAVITY_GLOVE = ITEMS.register("gravity_glove",
            () -> new GravityGlove(new Item.Properties().attributes(GravityGlove.createAttributes())
                    .stacksTo(1).rarity(Rarity.RARE).durability(500)));

    public static void register(IEventBus eventBus){
        ITEMS.register(eventBus);
    }
}
