package com.hasoook.hasoookmod.item.custom;

import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.Random;

public class WaterBoots extends ArmorItem {
    public WaterBoots(Holder<ArmorMaterial> pMaterial, Type pType, Properties pProperties) {
        super(pMaterial, pType, pProperties);
    }

    @Override
    public void inventoryTick(ItemStack pStack, Level pLevel, Entity pEntity, int pSlotId, boolean pIsSelected) {
        LivingEntity entity = (LivingEntity) pEntity;
        if (pSlotId == 36) {
            Random random = new Random();
            if (random.nextInt(20) == 0 && pLevel instanceof ServerLevel serverlevel) { // 1/20的概率

                float temperature = pLevel.getBiome(entity.blockPosition()).value().getBaseTemperature();
                if (temperature <= 0 || temperature >= 2) {
                    pStack.hurtAndBreak(1, entity, EquipmentSlot.FEET);
                }

                serverlevel.sendParticles(
                        ParticleTypes.FALLING_WATER,
                        entity.getX(),
                        entity.getY() + 0.2,
                        entity.getZ(),
                        1,
                        entity.getBbWidth() / 2,
                        0.2,
                        entity.getBbWidth() / 2,
                        0
                );
            }

            if (pLevel.dimensionType().ultraWarm()) {
                entity.setItemSlot(EquipmentSlot.FEET, ItemStack.EMPTY);
                if (entity.level() instanceof ServerLevel serverlevel) {
                    serverlevel.sendParticles(
                            ParticleTypes.LARGE_SMOKE,
                            entity.getX(),
                            entity.getY() + 0.2,
                            entity.getZ(),
                            10,
                            entity.getBbWidth() / 2,
                            0.2,
                            entity.getBbWidth() / 2,
                            0
                    );
                    entity.level().playSound(null, entity.getX(), entity.getY(), entity.getZ(),
                            SoundEvents.FIRE_EXTINGUISH, SoundSource.PLAYERS, 1.0F, 1.0F
                    );
                }
            }

        }
    }
}
