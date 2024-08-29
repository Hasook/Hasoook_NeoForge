package com.hasoook.hasoookmod.mixin.entityEnchantmentMixin;

import com.hasoook.hasoookmod.entityEnchantment.EntityEnchantmentHelper;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.Shearable;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RangedAttackGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.AbstractGolem;
import net.minecraft.world.entity.animal.SnowGolem;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.SmallFireball;
import net.minecraft.world.entity.projectile.Snowball;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SnowGolem.class)
public abstract class SnowGolemMixin extends AbstractGolem implements Shearable, RangedAttackMob {
    protected SnowGolemMixin(EntityType<? extends AbstractGolem> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new RangedAttackGoal(this, 1.25, 20, 10.0F));
        this.goalSelector.addGoal(2, new WaterAvoidingRandomStrollGoal(this, 1.0, 1.0000001E-5F));
        this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 6.0F));
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Mob.class, 10, true, false, p_29932_ -> p_29932_ instanceof Enemy));
    }

    @Inject(method = "performRangedAttack", at = @At("HEAD"), cancellable = true)
    public void performRangedAttack(LivingEntity pTarget, float pDistanceFactor, CallbackInfo ci) {
        int multishot = EntityEnchantmentHelper.getEnchantmentLevel(this,"minecraft:multishot");
        int fire_aspect = EntityEnchantmentHelper.getEnchantmentLevel(this,"minecraft:fire_aspect");
        int x = 1;
        if (multishot > 0) {
            x = multishot + 2;
        }

        for (int i = 0; i < x; i++) {
            if (fire_aspect > 0) {
                double d0 = this.distanceToSqr(pTarget);
                double d1 = pTarget.getX() - this.getX();
                double d2 = pTarget.getY(0.5) - this.getY(0.5);
                double d3 = pTarget.getZ() - this.getZ();
                double d4 = Math.sqrt(Math.sqrt(d0)) * 0.5;
                Vec3 vec3 = new Vec3(this.getRandom().triangle(d1, 2.297 * d4), d2, this.getRandom().triangle(d3, 1.5 * d4));
                SmallFireball smallfireball = new SmallFireball(this.level(), this, vec3.normalize()); // 创建火球
                smallfireball.setPos(smallfireball.getX(), this.getY(0.5) + 0.5, smallfireball.getZ()); // 设置火球位置
                this.level().addFreshEntity(smallfireball); // 添加火球到世界
            } else {
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
        ci.cancel();
    }
}
