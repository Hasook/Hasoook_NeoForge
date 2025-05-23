package com.hasoook.hasoookmod;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

// An example config class. This is not required, but it's a good idea to have one to keep your config organized.
// Demonstrates how to use Neo's config APIs
@EventBusSubscriber(modid = HasoookMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class Config
{
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    private static final ModConfigSpec.IntValue HUGE_DIAMOND_PICK_MINING_RANGE = BUILDER
            .comment("超大钻石镐增加的破坏范围（半径），默认值：2")
            .defineInRange("hugeDiamondPickMiningRange", 2, 0, Integer.MAX_VALUE);

    private static final ModConfigSpec.IntValue HUGE_DIAMOND_PICK_INTERACTION_RANGE = BUILDER
            .comment("超大钻石镐增加的交互距离，默认值：6")
            .defineInRange("hugeDiamondPickInteractionRange", 6, 0, Integer.MAX_VALUE);

    private static final ModConfigSpec.IntValue DURATION_OF_TORNADO = BUILDER
            .comment("龙卷风持续时间，默认值：200")
            .defineInRange("durationOfTornado", 200, 0, Integer.MAX_VALUE);

    private static final ModConfigSpec.BooleanValue WATER_BOOTS_LOSE_DURABILITY = BUILDER
            .comment("水靴子在寒冷或热带群系中会持续减少耐久值，默认值：开")
            .define("waterBootsLoseDurability", true);

    private static final ModConfigSpec.BooleanValue LAMA_GIVE_SPIT = BUILDER
            .comment("羊驼口水攻击玩家时，是否会给予玩家一个口水物品，默认值：开")
            .define("lamaGiveSpit", true);

    private static final ModConfigSpec.BooleanValue LOUIS_XVI_BLINDNESS = BUILDER
            .comment("斩首状态使玩家失明，默认值：关")
            .define("louisXviBlindness", false);

    private static final ModConfigSpec.BooleanValue LOUIS_XVI_HEAD = BUILDER
            .comment("斩首状态限制使用头盔槽，默认值：开")
            .define("louisXviHead", true);

    public static final ModConfigSpec.ConfigValue<String> MAGIC_NUMBER_INTRODUCTION = BUILDER
            .comment("这只是一个模板，没什么用")
            .define("magicNumberIntroduction", "...");

    private static final ModConfigSpec.BooleanValue ENCHANTMENT_VALUE = BUILDER
            .comment("给予物品附魔能力值，默认值：开")
            .define("enchantmentValue", true);

    private static final ModConfigSpec.ConfigValue<List<? extends String>> ITEM_STRINGS = BUILDER
            .comment("这只是一个模板，没什么用")
            .defineListAllowEmpty("items", List.of("minecraft:iron_ingot"), Config::validateItemName);

    static final ModConfigSpec SPEC = BUILDER.build();

    public static int hugeDiamondPickMiningRange;
    public static int hugeDiamondPickInteractionRange;
    public static int durationOfTornado;
    public static boolean waterBootsLoseDurability;
    public static boolean lamaGiveSpit;
    public static boolean louisXviBlindness;
    public static boolean louisXviHead;
    public static boolean enchantmentValue;
    public static String magicNumberIntroduction;
    public static Set<Item> items;

    private static boolean validateItemName(final Object obj)
    {
        return obj instanceof String itemName && BuiltInRegistries.ITEM.containsKey(ResourceLocation.parse(itemName));
    }

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event)
    {
        hugeDiamondPickMiningRange = HUGE_DIAMOND_PICK_MINING_RANGE.get();
        hugeDiamondPickInteractionRange = HUGE_DIAMOND_PICK_INTERACTION_RANGE.get();
        durationOfTornado = DURATION_OF_TORNADO.get();
        waterBootsLoseDurability = WATER_BOOTS_LOSE_DURABILITY.get();
        lamaGiveSpit = LAMA_GIVE_SPIT.get();
        louisXviBlindness = LOUIS_XVI_BLINDNESS.get();
        louisXviHead = LOUIS_XVI_HEAD.get();
        enchantmentValue = ENCHANTMENT_VALUE.get();
        magicNumberIntroduction = MAGIC_NUMBER_INTRODUCTION.get();

        // convert the list of strings into a set of items
        items = ITEM_STRINGS.get().stream()
                .map(itemName -> BuiltInRegistries.ITEM.get(ResourceLocation.parse(itemName)))
                .collect(Collectors.toSet());
    }
}
