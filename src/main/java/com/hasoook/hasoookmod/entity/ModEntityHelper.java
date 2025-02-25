package com.hasoook.hasoookmod.entity;

import com.hasoook.hasoookmod.enchantment.ModEnchantmentHelper;
import com.hasoook.hasoookmod.enchantment.ModEnchantments;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.*;
import net.minecraft.world.entity.monster.Shulker;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.Arrays;
import java.util.List;

public class ModEntityHelper {
    // 判断是否为白色生物
    public static boolean isWhiteMob(Entity entity) {
        List<EntityType<?>> invalidEntities = Arrays.asList(
                EntityType.SKELETON,
                EntityType.SKELETON_HORSE,
                EntityType.GOAT,
                EntityType.POLAR_BEAR,
                EntityType.IRON_GOLEM,
                EntityType.SNOW_GOLEM,
                EntityType.CHICKEN,
                EntityType.GHAST
        );

        // 检查实体是否属于无效实体类型
        if (invalidEntities.contains(entity.getType())) {
            return true;
        }

        switch (entity) {
            // 判断羊的颜色
            case Sheep sheep -> {
                return sheep.getColor() == DyeColor.WHITE;
            }

            // 判断潜影贝的颜色
            case Shulker shulker -> {
                return shulker.getColor() == DyeColor.WHITE;
            }

            // 判断猫的品种
            case Cat cat -> {
                Holder<CatVariant> catType = cat.getVariant();
                return catType.is(CatVariant.WHITE) || catType.is(CatVariant.RAGDOLL);
            }

            // 判断狼的品种
            case Wolf wolf -> {
                Holder<WolfVariant> wolfVariant = wolf.getVariant();
                return wolfVariant.is(WolfVariants.SNOWY);
            }
            default -> {
            }
        }

        // 检查装备
        if (entity instanceof LivingEntity livingEntity) {
            ItemStack itemStack = livingEntity.getMainHandItem();
            int ZCPLvl = ModEnchantmentHelper.getEnchantmentLevel(ModEnchantments.ZERO_COST_PURCHASE, itemStack);
            boolean head = livingEntity.getItemBySlot(EquipmentSlot.HEAD).is(Items.SKELETON_SKULL);
            return ZCPLvl > 0 || head;
        }

        return false;
    }

    // 判断是否为黑色生物
    public static boolean isBlackMob(Entity entity) {
        List<EntityType<?>> invalidEntities = Arrays.asList(
                EntityType.WITHER_SKELETON,
                EntityType.WITHER,
                EntityType.ENDER_DRAGON,
                EntityType.MULE,
                EntityType.SPIDER,
                EntityType.BAT,
                EntityType.ENDERMAN
        );

        // 检查实体是否属于无效实体类型
        if (invalidEntities.contains(entity.getType())) {
            return true;
        }

        switch (entity) {
            // 判断羊的颜色
            case Sheep sheep -> {
                return sheep.getColor() == DyeColor.BLACK || sheep.getColor() == DyeColor.GRAY ;
            }

            // 判断潜影贝的颜色
            case Shulker shulker -> {
                return shulker.getColor() == DyeColor.BLACK;
            }

            // 判断猫的品种
            case Cat cat -> {
                Holder<CatVariant> catType = cat.getVariant();
                return catType.is(CatVariant.BLACK) || catType.is(CatVariant.ALL_BLACK);
            }

            // 判断狼的品种
            case Wolf wolf -> {
                Holder<WolfVariant> wolfVariant = wolf.getVariant();
                return wolfVariant.is(WolfVariants.BLACK);
            }
            default -> {
            }
        }

        // 检查装备
        if (entity instanceof LivingEntity livingEntity) {
            return livingEntity.getItemBySlot(EquipmentSlot.HEAD).is(Items.WITHER_SKELETON_SKULL);
        }

        return false;
    }
}
