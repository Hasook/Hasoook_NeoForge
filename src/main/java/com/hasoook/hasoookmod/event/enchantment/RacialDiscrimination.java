package com.hasoook.hasoookmod.event.enchantment;

import com.hasoook.hasoookmod.HasoookMod;
import com.hasoook.hasoookmod.effect.ModEffects;
import com.hasoook.hasoookmod.enchantment.ModEnchantmentHelper;
import com.hasoook.hasoookmod.enchantment.ModEnchantments;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.animal.*;
import net.minecraft.world.entity.monster.Shulker;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.*;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.stream.Collectors;

@EventBusSubscriber(modid = HasoookMod.MOD_ID)
public class RacialDiscrimination {
    @SubscribeEvent
    public static void LivingEntityUseItemEvent(LivingEntityUseItemEvent.Stop event) {
        LivingEntity livingEntity = event.getEntity();

        if (!livingEntity.level().isClientSide) {
            // 获取看着的实体
            Entity firstEntityInSight = getFirstEntityInSight(livingEntity, 30.0);
            ItemStack itemStack = livingEntity.getMainHandItem();
            int racialDiscrimination = ModEnchantmentHelper.getEnchantmentLevel(ModEnchantments.RACIAL_DISCRIMINATION, itemStack);

            if (firstEntityInSight != null && racialDiscrimination > 0 && isWhiteMob(firstEntityInSight)) {
                event.setCanceled(true);
                livingEntity.sendSystemMessage(Component.nullToEmpty("目标不合法！"));
            }
        }
    }

    @SubscribeEvent
    public static void LivingEntityUseItemEvent(LivingEntityUseItemEvent.Tick event) {
        LivingEntity livingEntity = event.getEntity();
        if (!livingEntity.level().isClientSide) {
            Entity firstEntityInSight = getFirstEntityInSight(livingEntity, 20.0);
            int duration = event.getDuration();
            ItemStack itemStack = livingEntity.getMainHandItem();
            int racialDiscrimination = ModEnchantmentHelper.getEnchantmentLevel(ModEnchantments.RACIAL_DISCRIMINATION, itemStack);

            // 如果看向的生物是黑色的生物
            if (racialDiscrimination > 0 && firstEntityInSight != null && isBlackMob(firstEntityInSight) && 72000 - duration >= 15) {
                // 检查是否有箭或无限材料
                if (livingEntity.getProjectile(itemStack).isEmpty() && !livingEntity.hasInfiniteMaterials()) {
                    event.setCanceled(true); // 取消事件
                }

                // 调用弓的releaseUsing方法
                ItemStack Bow = new ItemStack(Items.BOW);
                BowItem bowItem = (BowItem) Bow.getItem();
                bowItem.releaseUsing(Bow, livingEntity.level(), livingEntity, 0);

                // 损失耐久度
                itemStack.hurtAndBreak(1, livingEntity, event.getEntity().getUsedItemHand() == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND);
            }
        }
    }

    @SubscribeEvent
    public static void PlayerTickEvent(PlayerTickEvent.Post event){
        Player player = event.getEntity();
        ItemStack itemStack = player.getMainHandItem();
        int racialDiscrimination = ModEnchantmentHelper.getEnchantmentLevel(ModEnchantments.RACIAL_DISCRIMINATION, itemStack);
        Entity firstEntityInSight = getFirstEntityInSight(player, 20.0);

        if (racialDiscrimination > 0 && firstEntityInSight != null && isBlackMob(firstEntityInSight)) {
            player.attackStrengthTicker = (int) player.getCurrentItemAttackStrengthDelay();
        }
    }

    @SubscribeEvent
    public static void onEntityAttack(LivingIncomingDamageEvent event) {
        LivingEntity entity = event.getEntity();
        // 确保来源实体是 LivingEntity 类型
        if (event.getSource().getEntity() instanceof LivingEntity source) {
            if (!entity.level().isClientSide) {
                ItemStack attackerMainHandItem = source.getMainHandItem();
                int racialDiscrimination = ModEnchantmentHelper.getEnchantmentLevel(ModEnchantments.RACIAL_DISCRIMINATION, attackerMainHandItem);

                if (racialDiscrimination > 0) {
                    if (isBlackMob(entity)) {
                        entity.invulnerableTime = 0;
                        if (source.getMainHandItem().is(Items.LEAD) && event.getSource().getDirectEntity() == source) {
                            // 判断是否有 “去工作” 效果，如果有则设置为 等级+1 ，没有则设置为0级（0级在游戏里为1级）
                            int goWorkAmplifier = (entity.getEffect(ModEffects.GO_WORK) != null) ? Objects.requireNonNull(entity.getEffect(ModEffects.GO_WORK)).getAmplifier() + 1 : 0;
                            int goWorkTime = (entity.getEffect(ModEffects.GO_WORK) != null) ? Objects.requireNonNull(entity.getEffect(ModEffects.GO_WORK)).getDuration() + 200 : 200;
                            entity.addEffect(new MobEffectInstance(ModEffects.GO_WORK, Math.min(goWorkTime, 600), goWorkAmplifier));
                        }
                    }
                    if (isWhiteMob(entity)) {
                        event.setCanceled(true);
                        source.sendSystemMessage(Component.nullToEmpty("目标不合法！"));
                    }
                }
            }
        }
    }

    /**
     * 获取玩家视线中的第一个实体
     *
     * @param livingEntity 观察者实体
     * @param maxDistance  最大视距
     * @return 第一个被射线检测到的实体，或 null
     */
    public static Entity getFirstEntityInSight(LivingEntity livingEntity, double maxDistance) {
        Vec3 vec3 = livingEntity.getViewVector(1.0F).normalize();
        Vec3 playerPos = livingEntity.getEyePosition(1.0F); // 获取玩家眼睛位置
        Vec3 rayEnd = playerPos.add(vec3.x * maxDistance, vec3.y * maxDistance, vec3.z * maxDistance); // 计算射线的终点
        // 获取射线与实体的交点
        List<Entity> nearbyEntities = livingEntity.level().getEntities(livingEntity, livingEntity.getBoundingBox().inflate(maxDistance), entity -> entity != livingEntity);

        // 遍历所有在范围内的实体
        for (Entity entity : nearbyEntities) {
            AABB entityBoundingBox = entity.getBoundingBox(); // 获取实体的碰撞箱

            if (entityBoundingBox.clip(playerPos, rayEnd).isPresent()) {
                // 如果射线与实体的碰撞箱相交，返回该实体
                return entity;
            }
        }

        // 如果没有找到符合条件的实体，返回null
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
    private static boolean isBlackMob(Entity entity) {
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
                return sheep.getColor() == DyeColor.BLACK;
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
