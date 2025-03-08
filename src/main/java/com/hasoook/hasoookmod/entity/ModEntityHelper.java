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
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class ModEntityHelper {
    /**
     * 获取玩家视线中的第一个实体（自动过滤被方块遮挡的情况）
     *
     * @param livingEntity 观察者实体
     * @param maxDistance  最大视距
     * @return 第一个未被阻挡的实体，或 null
     */
    public static Entity getFirstInSight(LivingEntity livingEntity, double maxDistance) {
        Level level = livingEntity.level();
        Vec3 eyePos = livingEntity.getEyePosition(1.0F);
        Vec3 viewVec = livingEntity.getViewVector(1.0F);
        Vec3 rayEnd = eyePos.add(viewVec.scale(maxDistance));

        // 收集所有可能被射线击中的实体（按距离排序）
        List<Entity> entities = level.getEntities(
                livingEntity,
                livingEntity.getBoundingBox().expandTowards(viewVec.scale(maxDistance)).inflate(1.0),
                e -> e != livingEntity && e.isPickable()
        );

        // 按距离排序（从近到远）
        entities.sort(Comparator.comparingDouble(e -> e.distanceToSqr(livingEntity)));

        // 遍历每个实体，检查玩家到实体之间的路径是否有方块阻挡
        for (Entity entity : entities) {
            // 计算实体碰撞箱的最近交点
            AABB aabb = entity.getBoundingBox().inflate(entity.getPickRadius());
            Optional<Vec3> hitPos = aabb.clip(eyePos, rayEnd);

            if (hitPos.isPresent()) {
                // 检测玩家眼睛到实体交点之间的方块
                BlockHitResult blockHit = level.clip(new ClipContext(
                        eyePos,
                        hitPos.get(),  // 射线终点设为实体碰撞点，而非最大距离
                        ClipContext.Block.COLLIDER,
                        ClipContext.Fluid.NONE,
                        livingEntity
                ));

                // 如果路径无方块阻挡，返回该实体
                if (blockHit.getType() == HitResult.Type.MISS) {
                    return entity;
                }
            }
        }

        return null;
    }

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
