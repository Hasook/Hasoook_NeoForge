package com.hasoook.hasoookmod.event.enchantment;

import com.hasoook.hasoookmod.HasoookMod;
import com.hasoook.hasoookmod.enchantment.ModEnchantmentHelper;
import com.hasoook.hasoookmod.enchantment.ModEnchantments;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

import java.util.List;
import java.util.Random;

@EventBusSubscriber(modid = HasoookMod.MODID)
public class ChainDamageAttack {
    private static final ThreadLocal<Boolean> IS_CHAIN_DAMAGE_PROCESSING = ThreadLocal.withInitial(() -> false);

    @SubscribeEvent
    public static void onEntityAttack(LivingIncomingDamageEvent event) {
        if (IS_CHAIN_DAMAGE_PROCESSING.get()) {
            return;
        }

        IS_CHAIN_DAMAGE_PROCESSING.set(true);

        try {
            LivingEntity target = event.getEntity();
            DamageSource damageSource = event.getSource();
            Entity sourceEntity = damageSource.getEntity();

            boolean isDirectAttack = sourceEntity instanceof LivingEntity attacker && damageSource.getDirectEntity() == attacker;
            // 判断是否为直接伤害

            if (isDirectAttack && sourceEntity instanceof LivingEntity attacker && sourceEntity.level() instanceof ServerLevel serverLevel) {
                ItemStack attackerMainHandItem = attacker.getMainHandItem();
                Random random = new Random();
                int chainDamageLevel = ModEnchantmentHelper.getEnchantmentLevel(ModEnchantments.CHAIN_DAMAGE, attackerMainHandItem);
                int fireDamageLevel = ModEnchantmentHelper.getEnchantmentLevel(Enchantments.FIRE_ASPECT, attackerMainHandItem);
                int unbreakingLevel = ModEnchantmentHelper.getEnchantmentLevel(Enchantments.UNBREAKING, attackerMainHandItem);

                if (chainDamageLevel > 0) {
                    float damage = event.getAmount(); // 获取造成的伤害值
                    Level world = target.level();
                    double radius = 4.0 * chainDamageLevel; // 范围半径

                    List<LivingEntity> entitiesInRange = world.getEntitiesOfClass(LivingEntity.class,
                            target.getBoundingBox().inflate(radius),
                            entity -> !entity.equals(target) && entity.getClass() == target.getClass());
                    // 选择范围内的所有实体

                    for (LivingEntity entityInRange : entitiesInRange) {
                        if (entityInRange != target) {
                            entityInRange.hurt(event.getSource(), damage);
                            // 对范围内的每个相同类型的实体（不包括被攻击的实体）造成等量伤害
                            if (fireDamageLevel > 0) {
                                entityInRange.setRemainingFireTicks(fireDamageLevel * 80);
                                // 火焰附加
                            }
                        }
                        if (attacker instanceof Player player && !player.isCreative() && random.nextInt(200 / (1 + unbreakingLevel)) == 0) {
                            attackerMainHandItem.hurtAndBreak(1, player, LivingEntity.getSlotForHand(InteractionHand.MAIN_HAND));
                            // 减少物品的耐久（概率受耐久附魔影响）
                        }
                    }
                }
            }
        } finally {
            IS_CHAIN_DAMAGE_PROCESSING.set(false);
        }
    }
}
