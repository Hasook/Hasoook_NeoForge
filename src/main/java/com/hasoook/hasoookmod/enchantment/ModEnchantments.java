package com.hasoook.hasoookmod.enchantment;

import com.hasoook.hasoookmod.HasoookMod;
import com.hasoook.hasoookmod.tags.ModItemTags;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.block.Block;

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
    public static final ResourceKey<Enchantment> HEARTLESS = key("heartless");
    public static final ResourceKey<Enchantment> BACKSTAB = key("backstab");
    public static final ResourceKey<Enchantment> BETRAY = key("betray");
    public static final ResourceKey<Enchantment> FLYING_THUNDER_GOD = key("flying_thunder_god"); // 飞雷神
    public static final ResourceKey<Enchantment> RACIAL_DISCRIMINATION = key("racial_discrimination"); // 种族歧视
    public static final ResourceKey<Enchantment> MIDDLE_EAST_BEST_PILOT = key("middle_east_best_pilot"); // 中东最好的飞行员
    public static final ResourceKey<Enchantment> DE_URBANIZATION = key("de_urbanization"); // 去城市化
    public static final ResourceKey<Enchantment> SPOTLIGHT = key("spotlight"); // 聚光
    public static final ResourceKey<Enchantment> ZERO_COST_PURCHASE = key("zero_cost_purchase"); // 零元购
    public static final ResourceKey<Enchantment> POLITICAL_CORRECTNESS = key("political_correctness"); // 政治正确
    public static final ResourceKey<Enchantment> FISSION = key("fission"); // 分裂

    // 引导方法，用于初始化附魔注册
    public static <DamageType> void bootstrap(BootstrapContext<Enchantment> context)
    {
        // 获取各种注册表的持有者获取器
        HolderGetter<net.minecraft.world.damagesource.DamageType> holdergetter = context.lookup(Registries.DAMAGE_TYPE);
        HolderGetter<Enchantment> holdergetter1 = context.lookup(Registries.ENCHANTMENT);
        HolderGetter<Item> holdergetter2 = context.lookup(Registries.ITEM);
        HolderGetter<Block> holdergetter3 = context.lookup(Registries.BLOCK);

        // 注册自定义附魔
        register(
                context,
                RANDOM_BULLETS, // 随机子弹
                Enchantment.enchantment(
                        Enchantment.definition(
                                holdergetter2.getOrThrow(ItemTags.BOW_ENCHANTABLE),
                                2,
                                1,
                                Enchantment.constantCost(25),
                                Enchantment.constantCost(50),
                                8,
                                EquipmentSlotGroup.MAINHAND
                        )
                )
        );
        register(
                context,
                SEPARATION_EXPLOSION, // 分离爆炸
                Enchantment.enchantment(
                        Enchantment.definition(
                                holdergetter2.getOrThrow(ModItemTags.SEPARATION_ITEMS),
                                2,
                                1,
                                Enchantment.constantCost(25),
                                Enchantment.constantCost(50),
                                4,
                                EquipmentSlotGroup.MAINHAND
                        )
                )
        );
        register(
                context,
                DISDAIN, // 嫌弃
                Enchantment.enchantment(
                        Enchantment.definition(
                                holdergetter2.getOrThrow(ModItemTags.COMMON_TAG),
                                2,
                                2,
                                Enchantment.constantCost(25),
                                Enchantment.constantCost(50),
                                8,
                                EquipmentSlotGroup.MAINHAND
                        )
                )
        );
        register(
                context,
                SWAP, // 交换
                Enchantment.enchantment(
                        Enchantment.definition(
                                holdergetter2.getOrThrow(ModItemTags.COMMON_TAG),
                                2,
                                1,
                                Enchantment.constantCost(25),
                                Enchantment.constantCost(50),
                                8,
                                EquipmentSlotGroup.MAINHAND
                        )
                )
        );
        register(
                context,
                CHAIN_DAMAGE, // 连锁打怪
                Enchantment.enchantment(
                        Enchantment.definition(
                                holdergetter2.getOrThrow(ItemTags.WEAPON_ENCHANTABLE),
                                2,
                                2,
                                Enchantment.constantCost(25),
                                Enchantment.constantCost(50),
                                20,
                                EquipmentSlotGroup.MAINHAND
                        )
                )
        );
        register(
                context,
                GIVE, // 给予
                Enchantment.enchantment(
                        Enchantment.definition(
                                holdergetter2.getOrThrow(ModItemTags.COMMON_TAG),
                                2,
                                1,
                                Enchantment.constantCost(25),
                                Enchantment.constantCost(50),
                                2,
                                EquipmentSlotGroup.MAINHAND
                        )
                )
        );
        register(
                context,
                UNYIELDING, // 不屈
                Enchantment.enchantment(
                        Enchantment.definition(
                                holdergetter2.getOrThrow(ItemTags.ARMOR_ENCHANTABLE),
                                2,
                                5,
                                Enchantment.constantCost(25),
                                Enchantment.constantCost(50),
                                2,
                                EquipmentSlotGroup.MAINHAND
                        )
                )
        );
        register(
                context,
                SEVEN_STEP_SNAKE_VENOM, // 七步蛇毒
                Enchantment.enchantment(
                        Enchantment.definition(
                                holdergetter2.getOrThrow(ItemTags.FOOT_ARMOR_ENCHANTABLE),
                                2,
                                3,
                                Enchantment.constantCost(25),
                                Enchantment.constantCost(50),
                                2,
                                EquipmentSlotGroup.FEET
                        )
                )
        );
        register(
                context,
                KILL_A_MAN_EVERY_TEN_PACES, // 十步杀一人
                Enchantment.enchantment(
                        Enchantment.definition(
                                holdergetter2.getOrThrow(ItemTags.FOOT_ARMOR_ENCHANTABLE),
                                2,
                                1,
                                Enchantment.constantCost(25),
                                Enchantment.constantCost(50),
                                2,
                                EquipmentSlotGroup.FEET
                        )
                )
        );
        register(
                context,
                HEARTLESS, // 绝情
                Enchantment.enchantment(
                        Enchantment.definition(
                                holdergetter2.getOrThrow(ItemTags.WEAPON_ENCHANTABLE),
                                2,
                                1,
                                Enchantment.constantCost(25),
                                Enchantment.constantCost(50),
                                2,
                                EquipmentSlotGroup.MAINHAND
                        )
                )
        );
        register(
                context,
                BACKSTAB, // 背刺
                Enchantment.enchantment(
                        Enchantment.definition(
                                holdergetter2.getOrThrow(ItemTags.WEAPON_ENCHANTABLE),
                                2,
                                1,
                                Enchantment.constantCost(25),
                                Enchantment.constantCost(50),
                                10,
                                EquipmentSlotGroup.MAINHAND
                        )
                )
        );
        register(
                context,
                BETRAY, // 背叛
                Enchantment.enchantment(
                        Enchantment.definition(
                                holdergetter2.getOrThrow(ItemTags.TRIDENT_ENCHANTABLE),
                                2,
                                3,
                                Enchantment.constantCost(25),
                                Enchantment.constantCost(50),
                                2,
                                EquipmentSlotGroup.MAINHAND
                        )
                )
        );register(
                context,
                FLYING_THUNDER_GOD, // 飞雷神
                Enchantment.enchantment(
                        Enchantment.definition(
                                holdergetter2.getOrThrow(ItemTags.TRIDENT_ENCHANTABLE),
                                2,
                                3,
                                Enchantment.constantCost(25),
                                Enchantment.constantCost(50),
                                2,
                                EquipmentSlotGroup.MAINHAND
                        )
                )
        );register(
            context,
            RACIAL_DISCRIMINATION, // 种族歧视
            Enchantment.enchantment(
                    Enchantment.definition(
                            holdergetter2.getOrThrow(ItemTags.BOW_ENCHANTABLE),
                            2,
                            1,
                            Enchantment.constantCost(25),
                            Enchantment.constantCost(50),
                            8,
                            EquipmentSlotGroup.MAINHAND
                    )
            )
        );register(
            context,
            MIDDLE_EAST_BEST_PILOT, // 中东最好的飞行员
            Enchantment.enchantment(
                    Enchantment.definition(
                            holdergetter2.getOrThrow(ItemTags.WEAPON_ENCHANTABLE),
                            2,
                            1,
                            Enchantment.constantCost(25),
                            Enchantment.constantCost(50),
                            8,
                            EquipmentSlotGroup.MAINHAND
                    )
            )
        );register(
            context,
            DE_URBANIZATION, // 去城市化
            Enchantment.enchantment(
                    Enchantment.definition(
                            holdergetter2.getOrThrow(ItemTags.WEAPON_ENCHANTABLE),
                            2,
                            1,
                            Enchantment.constantCost(25),
                            Enchantment.constantCost(50),
                            8,
                            EquipmentSlotGroup.MAINHAND
                    )
            )
        );register(
            context,
            SPOTLIGHT, // 聚光
            Enchantment.enchantment(
                    Enchantment.definition(
                            holdergetter2.getOrThrow(ItemTags.WEAPON_ENCHANTABLE),
                            2,
                            3,
                            Enchantment.constantCost(25),
                            Enchantment.constantCost(50),
                            8,
                            EquipmentSlotGroup.MAINHAND
                    )
            )
        );register(
            context,
            ZERO_COST_PURCHASE, // 零元购
            Enchantment.enchantment(
                    Enchantment.definition(
                            holdergetter2.getOrThrow(ItemTags.SWORD_ENCHANTABLE),
                            2,
                            1,
                            Enchantment.constantCost(25),
                            Enchantment.constantCost(50),
                            8,
                            EquipmentSlotGroup.MAINHAND
                    )
            )
        );register(
            context,
            POLITICAL_CORRECTNESS, // 政治正确
            Enchantment.enchantment(
                    Enchantment.definition(
                            holdergetter2.getOrThrow(ItemTags.SWORD_ENCHANTABLE),
                            2,
                            1,
                            Enchantment.constantCost(25),
                            Enchantment.constantCost(50),
                            8,
                            EquipmentSlotGroup.MAINHAND
                    )
            )
        );register(
            context,
            FISSION, // 分裂
            Enchantment.enchantment(
                    Enchantment.definition(
                            holdergetter2.getOrThrow(ItemTags.SWORD_ENCHANTABLE),
                            2,
                            1,
                            Enchantment.constantCost(25),
                            Enchantment.constantCost(50),
                            8,
                            EquipmentSlotGroup.MAINHAND
                    )
            )
        );
    }

    // 注册附魔的方法
    private static void register(BootstrapContext<Enchantment> context, ResourceKey<Enchantment> key, Enchantment.Builder builder) {
        context.register(key, builder.build(key.location()));
    }

    // 创建附魔资源键的方法
    private static ResourceKey<Enchantment> key(String name)
    {
        return ResourceKey.create(Registries.ENCHANTMENT, ResourceLocation.fromNamespaceAndPath(HasoookMod.MOD_ID,name));
    }
}
