package com.hasoook.hasoookmod.event.enchantment;

import com.hasoook.hasoookmod.HasoookMod;
import com.hasoook.hasoookmod.enchantment.ModEnchantmentHelper;
import com.hasoook.hasoookmod.enchantment.ModEnchantments;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

import java.util.Objects;

@EventBusSubscriber(modid = HasoookMod.MOD_ID)
public class Fission {
    @SubscribeEvent
    public static void onEntityAttack(LivingIncomingDamageEvent event) {
        LivingEntity entity = event.getEntity();
        Entity source = event.getSource().getEntity();

        if (!entity.level().isClientSide && entity.getType() != EntityType.PLAYER && source instanceof LivingEntity attacker) {
            ItemStack attackerMainHandItem = attacker.getMainHandItem();
            int fissionLvl = ModEnchantmentHelper.getEnchantmentLevel(ModEnchantments.FISSION, attackerMainHandItem);

            float scale = entity.getScale(); // 获取实体尺寸

            if (fissionLvl > 0 && scale > 0.0625 && entity.getHealth() < entity.getMaxHealth() / 2) {
                // 获取攻击目标的实体类型
                EntityType<?> targetEntityType = entity.getType();

                for (int i = 0; i < 2; i++) {
                    // 创建新实体
                    LivingEntity newEntity = (LivingEntity) targetEntityType.create(entity.level());

                    if (newEntity != null) {
                        // 设置新实体的位置
                        newEntity.moveTo(entity.getX(), entity.getY(), entity.getZ(), entity.getYRot(), entity.getXRot());

                        // 给新实体添加随机的运动向量
                        double randomX = (Math.random() - 0.5) * 0.5;
                        double randomZ = (Math.random() - 0.5) * 0.5;
                        newEntity.setDeltaMovement(randomX, 0.2, randomZ);

                        // 处理新实体的属性
                        AttributeInstance scaleAttr = Objects.requireNonNull(newEntity.getAttribute(Attributes.SCALE));
                        scaleAttr.removeModifier(ResourceLocation.withDefaultNamespace("ee_power"));
                        scaleAttr.addPermanentModifier(new AttributeModifier(ResourceLocation.withDefaultNamespace("ee_power"), scale - 1.2, AttributeModifier.Operation.ADD_VALUE));

                        // 将新实体添加到世界中
                        entity.level().addFreshEntity(newEntity);

                        // 获取和设置持久数据值
                        // entity.getPersistentData().get("AAA");
                        // entity.getPersistentData().putInt("AAA",66666666);
                    }
                }
                // 粒子效果
                if (entity.level() instanceof ServerLevel serverlevel) {
                    serverlevel.sendParticles(
                            ParticleTypes.EXPLOSION,
                            entity.getX(),
                            entity.getY() + entity.getBbHeight() / 2,
                            entity.getZ(),
                            1,
                            0,
                            0,
                            0,
                            0.1
                    );
                }
                entity.discard(); // 抛弃原始实体
            }
        }
    }
}
