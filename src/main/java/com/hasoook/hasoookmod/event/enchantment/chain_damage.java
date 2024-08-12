package com.hasoook.hasoookmod.event.enchantment;

import com.hasoook.hasoookmod.HasoookMod;
import com.hasoook.hasoookmod.enchantment.ModEnchantmentHelper;
import com.hasoook.hasoookmod.enchantment.ModEnchantments;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
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
    private static final ThreadLocal<Boolean> IS_CHAIN_DAMAGE_PROCESSING = ThreadLocal.withInitial(() -> false);

    @SubscribeEvent
    public static void onEntityAttack(LivingIncomingDamageEvent event) {
        if (IS_CHAIN_DAMAGE_PROCESSING.get()) {
            return;
        }

        IS_CHAIN_DAMAGE_PROCESSING.set(true);

        try {
            LivingEntity target = event.getEntity();
            Entity sourceEntity = event.getSource().getEntity();

            if (sourceEntity instanceof LivingEntity attacker) {
                ItemStack attackerMainHandItem = attacker.getMainHandItem();
                int chainDamageLevel = ModEnchantmentHelper.getEnchantmentLevel(ModEnchantments.CHAIN_DAMAGE, attackerMainHandItem);

                if (chainDamageLevel > 0) {
                    float damage = event.getAmount();
                    Level world = target.level();
                    double radius = 5.0;

                    // 选择范围内的所有实体
                    List<LivingEntity> entitiesInRange = world.getEntitiesOfClass(LivingEntity.class,
                            target.getBoundingBox().inflate(radius),
                            entity -> !entity.equals(target) && entity.getClass() == target.getClass());

                    // 给范围内的每个相同类型的实体造成伤害
                    for (LivingEntity entityInRange : entitiesInRange) {
                        if (entityInRange != target) {
                            entityInRange.hurt(event.getSource(), damage);
                        }
                        // 检查攻击者是否是玩家并且是否处于创造模式
                        if (attacker instanceof Player player && !player.isCreative()) {
                            // 获取玩家手中使用的物品
                            ItemStack itemStack = player.getMainHandItem(); // 或者 player.getOffhandItem()，根据实际情况

                            if (!itemStack.isEmpty()) {
                                // 减少物品的耐久
                                itemStack.hurtAndBreak(1, player, LivingEntity.getSlotForHand(InteractionHand.MAIN_HAND));
                                // 这里的 1 表示每次攻击减少1点耐久
                            }
                        }
                    }
                }
            }
        } finally {
            IS_CHAIN_DAMAGE_PROCESSING.set(false);
        }
    }
}
