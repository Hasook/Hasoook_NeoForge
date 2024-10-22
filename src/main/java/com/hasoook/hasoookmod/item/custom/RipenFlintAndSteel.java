package com.hasoook.hasoookmod.item.custom;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class RipenFlintAndSteel extends Item {
    public RipenFlintAndSteel(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack pStack, Level pLevel, LivingEntity pLivingEntity) {

        return super.finishUsingItem(pStack, pLevel, pLivingEntity);
    }
}