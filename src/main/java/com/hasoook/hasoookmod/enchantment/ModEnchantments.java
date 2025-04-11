package com.hasoook.hasoookmod.enchantment;

import com.hasoook.hasoookmod.HasoookMod;
import com.hasoook.hasoookmod.tags.ModItemTags;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;

// 自定义附魔类，用于定义和注册新的附魔
public class ModEnchantments {
    // 自定义附魔资源键
    public static final ResourceKey<Enchantment> RANDOM_BULLETS = key("random_bullets");
    public static final ResourceKey<Enchantment> SEPARATION_EXPLOSION = key("separation_explosion");
    public static final ResourceKey<Enchantment> DISDAIN = key("disdain");
    public static final ResourceKey<Enchantment> SWAP = key("swap");
    public static final ResourceKey<Enchantment> CHAIN_DAMAGE = key("chain_damage");
    public static final ResourceKey<Enchantment> GIVE = key("give");
    public static final ResourceKey<Enchantment> UNYIELDING = key("unyielding");
    public static final ResourceKey<Enchantment> SEVEN_STEP_SNAKE_VENOM = key("seven_step_snake_venom");
    public static final ResourceKey<Enchantment> KILL_A_MAN_EVERY_TEN_PACES = key("kill_a_man_every_ten_paces");
    public static final ResourceKey<Enchantment> BACKSTAB = key("backstab");
    public static final ResourceKey<Enchantment> BETRAY = key("betray");
    public static final ResourceKey<Enchantment> RACIAL_DISCRIMINATION = key("racial_discrimination"); // 种族歧视
    public static final ResourceKey<Enchantment> MIDDLE_EAST_BEST_PILOT = key("middle_east_best_pilot"); // 中东最好的飞行员
    public static final ResourceKey<Enchantment> DE_URBANIZATION = key("de_urbanization"); // 去城市化
    public static final ResourceKey<Enchantment> SPOTLIGHT = key("spotlight"); // 聚光
    public static final ResourceKey<Enchantment> ZERO_COST_PURCHASE = key("zero_cost_purchase"); // 零元购
    public static final ResourceKey<Enchantment> POLITICAL_CORRECTNESS = key("political_correctness"); // 政治正确
    public static final ResourceKey<Enchantment> FISSION = key("fission"); // 分裂
    public static final ResourceKey<Enchantment> TORNADO = key("tornado"); // 龙卷
    public static final ResourceKey<Enchantment> LOUIS_XVI = key("louis_xvi"); // 路易十六
    public static final ResourceKey<Enchantment> MIND_CONTROL = key("mind_control"); // 心灵控制
    public static final ResourceKey<Enchantment> TELEKINESIS = key("telekinesis"); // 隔空取物

    // 引导方法，用于初始化附魔注册
    public static <DamageType> void bootstrap(BootstrapContext<Enchantment> context)
    {
        // 获取各种注册表的持有者获取器
        HolderGetter<net.minecraft.world.damagesource.DamageType> holdergetter = context.lookup(Registries.DAMAGE_TYPE);
        var enchantments = context.lookup(Registries.ENCHANTMENT);
        var items = context.lookup(Registries.ITEM);
        var blocks = context.lookup(Registries.BLOCK);

        // 注册自定义附魔
        // 随机子弹
        register(context, RANDOM_BULLETS,Enchantment.enchantment(Enchantment.definition(
                items.getOrThrow(ItemTags.BOW_ENCHANTABLE),
                2,
                1,
                Enchantment.constantCost(25),
                Enchantment.constantCost(50),
                8,
                EquipmentSlotGroup.MAINHAND))
                .exclusiveWith(enchantments.getOrThrow(EnchantmentTags.DAMAGE_EXCLUSIVE)) // 附魔排斥
        );
        // 分离爆炸
        register(context, SEPARATION_EXPLOSION, Enchantment.enchantment(Enchantment.definition(
                items.getOrThrow(ModItemTags.SEPARATION_ITEMS),
                2,
                1,
                Enchantment.constantCost(25),
                Enchantment.constantCost(50),
                4,
                EquipmentSlotGroup.MAINHAND))
        );
        // 嫌弃
        register(context, DISDAIN, Enchantment.enchantment(Enchantment.definition(
                items.getOrThrow(ItemTags.DURABILITY_ENCHANTABLE),
                2,
                2,
                Enchantment.constantCost(25),
                Enchantment.constantCost(50),
                8,
                EquipmentSlotGroup.MAINHAND))
        );
        // 交换
        register(context, SWAP, Enchantment.enchantment(Enchantment.definition(
                items.getOrThrow(ItemTags.DURABILITY_ENCHANTABLE),
                2,
                1,
                Enchantment.constantCost(25),
                Enchantment.constantCost(50),
                8,
                EquipmentSlotGroup.MAINHAND))
        );
        // 连锁打怪
        register(context, CHAIN_DAMAGE, Enchantment.enchantment(Enchantment.definition(
                items.getOrThrow(ItemTags.WEAPON_ENCHANTABLE),
                2,
                2,
                Enchantment.constantCost(25),
                Enchantment.constantCost(50),
                20,
                EquipmentSlotGroup.MAINHAND))
        );
        // 给予
        register(context, GIVE, Enchantment.enchantment(Enchantment.definition(
                items.getOrThrow(ItemTags.DURABILITY_ENCHANTABLE),
                2,
                1,
                Enchantment.constantCost(25),
                Enchantment.constantCost(50),
                2,
                EquipmentSlotGroup.MAINHAND))
        );
        // 不屈
        register(context, UNYIELDING, Enchantment.enchantment(Enchantment.definition(
                items.getOrThrow(ItemTags.ARMOR_ENCHANTABLE),
                2,
                5,
                Enchantment.constantCost(25),
                Enchantment.constantCost(50),
                2,
                EquipmentSlotGroup.MAINHAND))
        );
        // 七步蛇毒
        register(context, SEVEN_STEP_SNAKE_VENOM, Enchantment.enchantment(Enchantment.definition(
                items.getOrThrow(ItemTags.FOOT_ARMOR_ENCHANTABLE),
                2,
                3,
                Enchantment.constantCost(25),
                Enchantment.constantCost(50),
                2,
                EquipmentSlotGroup.FEET))
        );
        // 十步杀一人
        register(context, KILL_A_MAN_EVERY_TEN_PACES, Enchantment.enchantment(Enchantment.definition(
                items.getOrThrow(ItemTags.FOOT_ARMOR_ENCHANTABLE),
                2,
                1,
                Enchantment.constantCost(25),
                Enchantment.constantCost(50),
                2,
                EquipmentSlotGroup.FEET))
        );
        // 背刺
        register(context, BACKSTAB, Enchantment.enchantment(Enchantment.definition(
                items.getOrThrow(ItemTags.WEAPON_ENCHANTABLE),
                2,
                1,
                Enchantment.constantCost(25),
                Enchantment.constantCost(50),
                10,
                EquipmentSlotGroup.MAINHAND))
        );
        // 背叛
        register(context, BETRAY, Enchantment.enchantment(Enchantment.definition(
                items.getOrThrow(ItemTags.TRIDENT_ENCHANTABLE),
                2,
                3,
                Enchantment.constantCost(25),
                Enchantment.constantCost(50),
                2,
                EquipmentSlotGroup.MAINHAND))
        );
        // 种族歧视
        register(context, RACIAL_DISCRIMINATION, Enchantment.enchantment(Enchantment.definition(
                items.getOrThrow(ModItemTags.RACIAL_DISCRIMINATION_ITEMS),
                2,
                1,
                Enchantment.constantCost(25),
                Enchantment.constantCost(50),
                8,
                EquipmentSlotGroup.MAINHAND))
        );
        // 中东最好的飞行员
        register(context, MIDDLE_EAST_BEST_PILOT, Enchantment.enchantment(Enchantment.definition(
                items.getOrThrow(ItemTags.WEAPON_ENCHANTABLE),
                2,
                1,
                Enchantment.constantCost(25),
                Enchantment.constantCost(50),
                8,
                EquipmentSlotGroup.MAINHAND))
        );
        // 去城市化
        register(context, DE_URBANIZATION, Enchantment.enchantment(Enchantment.definition(
                items.getOrThrow(ItemTags.WEAPON_ENCHANTABLE),
                2,
                1,
                Enchantment.constantCost(25),
                Enchantment.constantCost(50),
                8,
                EquipmentSlotGroup.MAINHAND))
        );
        // 聚光
        register(context, SPOTLIGHT, Enchantment.enchantment(Enchantment.definition(
                items.getOrThrow(ItemTags.WEAPON_ENCHANTABLE),
                2,
                3,
                Enchantment.constantCost(25),
                Enchantment.constantCost(50),
                8,
                EquipmentSlotGroup.MAINHAND))
        );
        // 零元购
        register(context, ZERO_COST_PURCHASE, Enchantment.enchantment(Enchantment.definition(
                items.getOrThrow(ItemTags.SWORD_ENCHANTABLE),
                2,
                1,
                Enchantment.constantCost(25),
                Enchantment.constantCost(50),
                8,
                EquipmentSlotGroup.MAINHAND))
        );
        // 政治正确
        register(context, POLITICAL_CORRECTNESS, Enchantment.enchantment(Enchantment.definition(
                items.getOrThrow(ItemTags.SWORD_ENCHANTABLE),
                2,
                1,
                Enchantment.constantCost(25),
                Enchantment.constantCost(50),
                8,
                EquipmentSlotGroup.MAINHAND))
        );
        // 分裂
        register(context, FISSION, Enchantment.enchantment(Enchantment.definition(
                items.getOrThrow(ItemTags.SWORD_ENCHANTABLE),
                2,
                1,
                Enchantment.constantCost(25),
                Enchantment.constantCost(50),
                8,
                EquipmentSlotGroup.MAINHAND))
        );
        // 龙卷
        register(context, TORNADO, Enchantment.enchantment(Enchantment.definition(
                items.getOrThrow(ModItemTags.TORNADO_ITEMS),
                10,
                1,
                Enchantment.constantCost(1),
                Enchantment.constantCost(1),
                1,
                EquipmentSlotGroup.MAINHAND))
        );
        // 路易十六
        register(context, LOUIS_XVI, Enchantment.enchantment(Enchantment.definition(
                items.getOrThrow(ModItemTags.LOUIS_XVI),
                1,
                1,
                Enchantment.constantCost(20),
                Enchantment.constantCost(50),
                1,
                EquipmentSlotGroup.MAINHAND))
        );
        // 心灵控制
        register(context, MIND_CONTROL, Enchantment.enchantment(Enchantment.definition(
                items.getOrThrow(ItemTags.FISHES),
                1,
                1,
                Enchantment.constantCost(20),
                Enchantment.constantCost(50),
                1,
                EquipmentSlotGroup.MAINHAND))
        );
        // 隔空取物
        register(context, TELEKINESIS, Enchantment.enchantment(Enchantment.definition(
                items.getOrThrow(ModItemTags.GRAVITY_GLOVE),
                10,
                3,
                Enchantment.constantCost(20),
                Enchantment.constantCost(50),
                1,
                EquipmentSlotGroup.HAND))
        );
    }

    // 注册附魔的方法
    private static void register(BootstrapContext<Enchantment> context, ResourceKey<Enchantment> key, Enchantment.Builder builder) {
        context.register(key, builder.build(key.location()));
    }

    // 创建附魔资源键的方法
    private static ResourceKey<Enchantment> key(String name)
    {
        return ResourceKey.create(Registries.ENCHANTMENT, ResourceLocation.fromNamespaceAndPath(HasoookMod.MOD_ID, name));
    }
}
