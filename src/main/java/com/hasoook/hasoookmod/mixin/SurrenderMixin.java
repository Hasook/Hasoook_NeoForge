package com.hasoook.hasoookmod.mixin;

import com.hasoook.hasoookmod.item.ModItems;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class SurrenderMixin extends Entity {

    @Shadow public abstract void setHealth(float pHealth);

    protected SurrenderMixin(EntityType<? extends LivingEntity> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    @Inject(method = "checkTotemDeathProtection", at = @At("HEAD"))
    public void hurt(DamageSource pDamageSource, CallbackInfoReturnable<Boolean> cir) {
        ItemStack itemstack = null;
        boolean surrender = false;
        // 遍历所有手持物品槽
        for (InteractionHand interactionhand : InteractionHand.values()) {
            // 获取当前手持物品
            ItemStack itemstack1 = this.getItemInHand(interactionhand);
            // 如果是不死图腾，并且使用时触发了事件
            if (itemstack1.is(ModItems.TOTEM_OF_SURRENDER.get())) {
                surrender = true;
                itemstack = itemstack1.copy();
                itemstack1.shrink(1);
                System.out.println("111111");
                this.setHealth(1.0F);
                break;
            }
        }
    }

    public ItemStack getItemInHand(InteractionHand pHand) {
        if (pHand == InteractionHand.MAIN_HAND) {
            return this.getItemBySlot(EquipmentSlot.MAINHAND);
        } else if (pHand == InteractionHand.OFF_HAND) {
            return this.getItemBySlot(EquipmentSlot.OFFHAND);
        } else {
            throw new IllegalArgumentException("Invalid hand " + pHand);
        }
    }

    public ItemStack getMainHandItem() {
        return this.getItemBySlot(EquipmentSlot.MAINHAND);
    }

    public ItemStack getOffhandItem() {
        return this.getItemBySlot(EquipmentSlot.OFFHAND);
    }

    public boolean hasItemInSlot(EquipmentSlot pSlot) {
        return !this.getItemBySlot(pSlot).isEmpty();
    }

    public abstract ItemStack getItemBySlot(EquipmentSlot pSlot);

}