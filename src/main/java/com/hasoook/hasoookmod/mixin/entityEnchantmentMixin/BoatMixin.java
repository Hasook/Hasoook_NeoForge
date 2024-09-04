package com.hasoook.hasoookmod.mixin.entityEnchantmentMixin;

import com.hasoook.hasoookmod.entityEnchantment.EntityEnchantmentHelper;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Leashable;
import net.minecraft.world.entity.VariantHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.entity.vehicle.VehicleEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Boat.class)
public abstract class BoatMixin extends VehicleEntity implements Leashable, VariantHolder<Boat.Type>, net.neoforged.neoforge.common.extensions.IBoatExtension {
    @Shadow public abstract void tick();

    @Shadow private float invFriction;

    @Shadow private Boat.Status oldStatus;

    @Shadow private Boat.Status status;

    @Shadow private float deltaRotation;
    private double lavaLevel;

    public BoatMixin(EntityType<?> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    @Inject(method = "tick", at = @At("HEAD"))
    public void tick(CallbackInfo ci) {
        int fireProtectionLevel = EntityEnchantmentHelper.getEnchantmentLevel(this,"minecraft:fire_protection");
        if (fireProtectionLevel > 0) {

        }

    }

    public boolean hurt(@NotNull DamageSource pSource, float pAmount) {
        int fireProtectionLevel = EntityEnchantmentHelper.getEnchantmentLevel(this,"minecraft:fire_protection");
        if (fireProtectionLevel > 0){
            String damage = pSource.getMsgId();
            if (damage.equals("lava") || damage.equals("inFire") || damage.equals("onFire")) {
                this.setRemainingFireTicks(0);
                return false;
            }
        }
        return super.hurt(pSource, pAmount);
    }

}
