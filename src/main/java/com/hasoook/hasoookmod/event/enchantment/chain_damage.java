package com.hasoook.hasoookmod.event.enchantment;

import com.hasoook.hasoookmod.HasoookMod;
import com.hasoook.hasoookmod.enchantment.ModEnchantmentHelper;
import com.hasoook.hasoookmod.enchantment.ModEnchantments;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

import java.util.List;
import java.util.Objects;

@EventBusSubscriber(modid = HasoookMod.MODID)
public class chain_damage {
    private static final String CHAIN_DAMAGE_TAG = "hasoookmod:chain_damage";

    @SubscribeEvent
    public static void onEntityAttack(LivingIncomingDamageEvent event) {
        LivingEntity target = event.getEntity();
        Entity sourceEntity = event.getSource().getEntity();

        if (sourceEntity instanceof LivingEntity attacker) {
            ItemStack attackerMainHandItem = attacker.getMainHandItem();
            int swapLevel = ModEnchantmentHelper.getEnchantmentLevel(ModEnchantments.CHAIN_DAMAGE, attackerMainHandItem);

            if (swapLevel > 0) {
                float damage = event.getAmount();
                Level world = target.level();
                double radius = 5.0;

                // 获取周围与目标实体相同类型的实体
                AABB searchBox = target.getBoundingBox().inflate(radius);
                List<LivingEntity> nearbyEntities = world.getEntitiesOfClass(LivingEntity.class, searchBox, e -> e.getClass().equals(target.getClass()));

                for (LivingEntity entity : nearbyEntities) {
                    if (entity != target && entity != sourceEntity) {
                        boolean hasTag = entity.getPersistentData().getBoolean(CHAIN_DAMAGE_TAG);
                        if (!hasTag) {
                            entity.getPersistentData().putBoolean(CHAIN_DAMAGE_TAG, true);
                            // 给予标记
                            entity.hurt(sourceEntity.damageSources().thrown(sourceEntity, sourceEntity), damage);
                            // 造成伤害
                            Objects.requireNonNull(world.getServer()).execute(() -> entity.getPersistentData().remove(CHAIN_DAMAGE_TAG));
                            // 清除标记
                        }
                    }
                }
            }
        }
    }
}
