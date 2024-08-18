package com.hasoook.hasoookmod.mixin;

import com.hasoook.hasoookmod.block.ModBlock;
import com.hasoook.hasoookmod.effect.ModEffects;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Snowball;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Snowball.class)
public abstract class ConfusionFlowerMixin extends ThrowableItemProjectile {
    public ConfusionFlowerMixin(EntityType<? extends ThrowableItemProjectile> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    protected void onHit(HitResult pResult) {
        super.onHit(pResult);
        ItemStack itemStack = this.getItem();
        if (itemStack.getItem() == ModBlock.CONFUSION_FLOWER.asItem() && !this.level().isClientSide) {

            this.level().levelEvent(2002, this.blockPosition(), PotionContents.getColor(Potions.INFESTED));

            this.hurt(this.damageSources().thrown(this, this.getOwner()), (float) 0);
            this.level().broadcastEntityEvent(this, (byte)3);
            this.discard();
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult pResult) {
        super.onHitEntity(pResult);
        ItemStack itemStack = this.getItem();
        if (itemStack.getItem() == ModBlock.CONFUSION_FLOWER.asItem() && !this.level().isClientSide) {
            Entity entity = pResult.getEntity();
            if (entity instanceof LivingEntity livingEntity) {
                livingEntity.addEffect(new MobEffectInstance(ModEffects.CONFUSION, 1200, 0));
            }

            double d0 = this.random.nextGaussian() * 0.02;
            double d1 = this.random.nextGaussian() * 0.02;
            double d2 = this.random.nextGaussian() * 0.02;
            level().addParticle(ParticleTypes.WITCH, d0, d1, d2, 1.0, 0.5, 0.0);

            entity.hurt(this.damageSources().thrown(this, this.getOwner()), (float) 0);
            this.discard();
        }
    }
}
