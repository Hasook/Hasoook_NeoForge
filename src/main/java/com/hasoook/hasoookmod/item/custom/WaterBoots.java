package com.hasoook.hasoookmod.item.custom;

import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.Random;

public class WaterBoots extends ArmorItem {
    public WaterBoots(Holder<ArmorMaterial> pMaterial, Type pType, Properties pProperties) {
        super(pMaterial, pType, pProperties);
    }

    @Override
    public void inventoryTick(ItemStack pStack, Level pLevel, Entity pEntity, int pSlotId, boolean pIsSelected) {
        if (pSlotId == 36 && pLevel instanceof ServerLevel serverlevel) {
            Random random = new Random();
            if (random.nextInt(20) == 0) { // 1/20的概率
                serverlevel.sendParticles(
                        ParticleTypes.FALLING_WATER,
                        pEntity.getX(),
                        pEntity.getY() + 0.2,
                        pEntity.getZ(),
                        1,
                        pEntity.getBbWidth() / 2,
                        pEntity.getBbHeight() / 5,
                        pEntity.getBbWidth() / 2,
                        0
                );
            }
        }
    }
}
