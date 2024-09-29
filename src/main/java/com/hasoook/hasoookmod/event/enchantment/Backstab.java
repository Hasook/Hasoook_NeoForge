package com.hasoook.hasoookmod.event.enchantment;

import com.hasoook.hasoookmod.HasoookMod;
import com.hasoook.hasoookmod.enchantment.ModEnchantmentHelper;
import com.hasoook.hasoookmod.enchantment.ModEnchantments;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

@EventBusSubscriber(modid = HasoookMod.MODID)
public class Backstab {
    @SubscribeEvent
    public static void onPlayerInteractEntity(PlayerInteractEvent.EntityInteract event) {
        Player entity = event.getEntity();
        if (!(event.getTarget() instanceof LivingEntity target)) {
            return;
        }

        ItemStack itemStack = entity.getMainHandItem();
        int backstabLevel = ModEnchantmentHelper.getEnchantmentLevel(ModEnchantments.BACKSTAB, itemStack);

        if (backstabLevel > 0 && entity.getCooldowns().getCooldownPercent(itemStack.getItem(), 0) == 0 && !entity.level().isClientSide) {
            event.setCanceled(true);
            double deltaX = target.getLookAngle().x * -2;
            double deltaZ = target.getLookAngle().z * -2;
            double targetX = target.getX() + deltaX;
            double targetZ = target.getZ();

            entity.getCooldowns().addCooldown(itemStack.getItem(), 40);
            itemStack.hurtAndBreak(1, entity, LivingEntity.getSlotForHand(InteractionHand.MAIN_HAND));

            if (event.getLevel() instanceof ServerLevel serverlevel) {
                serverlevel.sendParticles(
                        ParticleTypes.WITCH,
                        entity.getX(),
                        entity.getY() + entity.getBbHeight() / 2,
                        entity.getZ(),
                        20,
                        entity.getBbWidth() / 10,
                        entity.getBbHeight() / 3,
                        entity.getBbWidth() / 10,
                        1
                );
            }

            entity.teleportTo(targetX, target.getY(), targetZ);
            entity.level().playSound(null, entity.getX(), entity.getY(), entity.getZ(), SoundEvents.PLAYER_TELEPORT, SoundSource.PLAYERS);
            entity.lookAt(EntityAnchorArgument.Anchor.EYES, new Vec3(target.getX(), target.getY() + target.getBbHeight() / 2, target.getZ()));
        }
    }

    @SubscribeEvent
    public static void onEntityAttack(LivingIncomingDamageEvent event) {
        LivingEntity entity = event.getEntity(); // 被攻击的实体
        DamageSource damageSource = event.getSource();
        LivingEntity sourceEntity = (LivingEntity) damageSource.getEntity(); // 造成伤害的实体

        ItemStack itemStack = null;
        if (sourceEntity != null) {
            itemStack = sourceEntity.getMainHandItem();
        }

        if (itemStack != null && !sourceEntity.level().isClientSide) {
            int backstabLevel = ModEnchantmentHelper.getEnchantmentLevel(ModEnchantments.BACKSTAB, itemStack);

            // 确保攻击者存在且为生物实体
            if (backstabLevel > 0 && sourceEntity instanceof LivingEntity attacker) {

                // 计算方向
                Vec3 attackerPos = attacker.position();
                Vec3 targetPos = entity.position();

                // 计算朝向
                Vec3 targetDirection = entity.getLookAngle(); // 被攻击实体的朝向
                Vec3 toAttacker = attackerPos.subtract(targetPos).normalize(); // 从被攻击者到攻击者的方向

                // 判断攻击者是否在被攻击者身后
                if (targetDirection.dot(toAttacker) < -0.2) {
                    float amount = event.getAmount();
                    event.setAmount(amount * 2);
                    itemStack.hurtAndBreak(1, entity, LivingEntity.getSlotForHand(InteractionHand.MAIN_HAND));

                    // 粒子效果
                    if (entity.level() instanceof ServerLevel serverLevel) {
                        serverLevel.sendParticles(
                                ParticleTypes.CRIT,
                                entity.getX(),
                                entity.getY() + entity.getBbHeight() / 2,
                                entity.getZ(),
                                10,
                                entity.getBbWidth() / 2,
                                entity.getBbHeight() / 2,
                                entity.getBbWidth() / 2,
                                0.0
                        );
                    }
                }
            }
        }
    }

}
