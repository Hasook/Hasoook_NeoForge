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
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.block.Block;
import net.neoforged.fml.common.Mod;

// 自定义附魔类，用于定义和注册新的附魔
public class ModEnchantments {
    // 自定义附魔资源键
    public static final ResourceKey<Enchantment> RANDOM_BULLETS = key("random_bullets");
    public static final ResourceKey<Enchantment> SEPARATION_EXPLOSION = key("separation_explosion");
    public static final ResourceKey<Enchantment> DISDAIN = key("disdain");
    public static final ResourceKey<Enchantment> SWAP = key("swap");
    public static final ResourceKey<Enchantment> CHAIN_DAMAGE = key("chain_damage");
    public static final ResourceKey<Enchantment> GIVE = key("give");

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
                                holdergetter2.getOrThrow(ModItemTags.DISDAIN_ITEMS),
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
                                holdergetter2.getOrThrow(ItemTags.WEAPON_ENCHANTABLE),
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
                GIVE, // 连锁打怪
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
    }

    // 注册附魔的方法
    private static void register(BootstrapContext<Enchantment> context, ResourceKey<Enchantment> key, Enchantment.Builder builder) {
        context.register(key, builder.build(key.location()));
    }

    // 创建附魔资源键的方法
    private static ResourceKey<Enchantment> key(String name)
    {
        return ResourceKey.create(Registries.ENCHANTMENT, ResourceLocation.fromNamespaceAndPath(HasoookMod.MODID,name));
    }
}
