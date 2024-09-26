package com.hasoook.hasoookmod.entity.custom;

import com.hasoook.hasoookmod.item.ModItems;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.*;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import org.jetbrains.annotations.NotNull;

public class PokerProjectileEntity extends ThrowableItemProjectile {
    private final int hurt;
    private final int firetick;
    private final boolean isExplosive;
    private final boolean isPiercing;

    public PokerProjectileEntity(EntityType<? extends ThrowableItemProjectile> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        this.hurt = 1;
        this.firetick = 0;
        this.isExplosive = false;
        this.isPiercing = false;
    }

    public PokerProjectileEntity(Level pLevel, LivingEntity pShooter, int hurt, int fireTick, boolean isExplosive, boolean isPiercing) {
        super(EntityType.SNOWBALL, pShooter, pLevel);
        this.hurt = hurt;
        this.firetick = fireTick;
        this.isExplosive = isExplosive;
        this.isPiercing = isPiercing;
    }

    @Override
    protected @NotNull Item getDefaultItem() {
        return ModItems.POKER.get();
    }

    @Override
    protected void onHitBlock(@NotNull BlockHitResult result) {
        if(isExplosive) {
            this.level().explode(this,this.getX(),this.getY(),this.getZ(),3,Level.ExplosionInteraction.MOB);
        }
        this.discard();
        super.onHitBlock(result);
    }

    @Override
    protected void onHitEntity(@NotNull EntityHitResult result) {
        LivingEntity entity = (LivingEntity) result.getEntity();
        if (isExplosive) {
            this.level().explode(this,this.getX(),this.getY(0.0625),this.getZ(),3,Level.ExplosionInteraction.MOB);
        } else {
            if (this.getItem().is(ModItems.POKER_HEART.get())) {
                entity.setRemainingFireTicks(entity.getRemainingFireTicks() + 40); // 着火
            }
            entity.setRemainingFireTicks(entity.getRemainingFireTicks() + firetick); // 着火

            if (this.getItem().is(ModItems.POKER_SPADE.get())) {
                entity.hurt(this.damageSources().thrown(this, this.getOwner()), 4);
            }
            entity.hurt(this.damageSources().thrown(this, this.getOwner()), hurt);
        }
        if (!isPiercing) {
            this.discard();
        }
        super.onHitEntity(result);
    }
}
