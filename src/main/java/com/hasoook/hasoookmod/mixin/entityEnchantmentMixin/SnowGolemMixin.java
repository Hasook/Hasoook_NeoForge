package com.hasoook.hasoookmod.mixin.entityEnchantmentMixin;

import com.hasoook.hasoookmod.entityEnchantment.EntityEnchantmentHelper;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Shearable;
import net.minecraft.world.entity.animal.AbstractGolem;
import net.minecraft.world.entity.animal.SnowGolem;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.projectile.Snowball;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SnowGolem.class)
public abstract class SnowGolemMixin extends AbstractGolem implements Shearable, RangedAttackMob {
    protected SnowGolemMixin(EntityType<? extends AbstractGolem> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    @Inject(method = "performRangedAttack", at = @At("RETURN"))
    public void performRangedAttack(LivingEntity pTarget, float pDistanceFactor, CallbackInfo ci) {
        int multishot = EntityEnchantmentHelper.getEnchantmentLevel(this,"minecraft:multishot");
        if (multishot > 1) {
            for (int i = 0; i < multishot + 1; i++) {
                Snowball snowball = new Snowball(this.level(), this);

                double d0 = pTarget.getEyeY() - 1.1F;
                double d1 = pTarget.getX() - this.getX();
                double d2 = d0 - snowball.getY();
                double d3 = pTarget.getZ() - this.getZ();
                double d4 = Math.sqrt(d1 * d1 + d3 * d3) * 0.2F;
                snowball.shoot(d1, d2 + d4, d3, 1.6F, 12.0F);
                this.playSound(SoundEvents.SNOW_GOLEM_SHOOT, 1.0F, 0.4F / (this.getRandom().nextFloat() * 0.4F + 0.8F));
                this.level().addFreshEntity(snowball);
            }
        }

    }
}
