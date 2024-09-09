package com.hasoook.hasoookmod.mixin.entityEnchantmentMixin;

import com.hasoook.hasoookmod.entityEnchantment.EntityEnchantmentHelper;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.SimpleExplosionDamageCalculator;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;
import java.util.function.Function;

@Mixin(Creeper.class)
public abstract class CreeperMixin extends Monster implements PowerableMob {
    protected CreeperMixin(EntityType<? extends Monster> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    @Shadow
    private void spawnLingeringCloud() {}

    @Shadow @Final private static EntityDataAccessor<Boolean> DATA_IS_POWERED;

    @Shadow @Final private static EntityDataAccessor<Boolean> DATA_IS_IGNITED;

    @Shadow public abstract void setTarget(@Nullable LivingEntity pTarget);

    @Shadow private int swell;

    @Shadow public abstract void setSwellDir(int pState);

    @Shadow @Final private static EntityDataAccessor<Integer> DATA_SWELL_DIR;
    @Unique
    private static final ExplosionDamageCalculator EXPLOSION_DAMAGE_CALCULATOR = new SimpleExplosionDamageCalculator(
            true, false, Optional.of(1.22F), BuiltInRegistries.BLOCK.getTag(BlockTags.BLOCKS_WIND_CHARGE_EXPLOSIONS).map(Function.identity())
    );

    @Inject(method = "tick", at = @At("HEAD"))
    public void tick(CallbackInfo ci) {
        int channelingLevel = EntityEnchantmentHelper.getEnchantmentLevel(this, "minecraft:channeling");
        if (channelingLevel > 0 && !this.isPowered()) {
            this.entityData.set(DATA_IS_POWERED, true);
            EntityEnchantmentHelper.removeEnchantment(this,"minecraft:channeling");
        }
    }

    @Inject(method = "explodeCreeper", at = @At("HEAD"), cancellable = true)
    private void explodeCreeper(CallbackInfo ci) {
        int power = EntityEnchantmentHelper.getEnchantmentLevel(this, "minecraft:power");
        int windBurst = EntityEnchantmentHelper.getEnchantmentLevel(this, "minecraft:wind_burst");
        int fireAspect = EntityEnchantmentHelper.getEnchantmentLevel(this, "minecraft:fire_aspect");
        int unbreaking = EntityEnchantmentHelper.getEnchantmentLevel(this, "minecraft:unbreaking");

        int size = EntityEnchantmentHelper.getEnchantmentSize(this);

        if (size > 0 && !this.level().isClientSide) {
            float f = this.isPowered() ? 2.0F : 1.0F; // 获取爆炸倍率
            this.dead = true;
            this.spawnLingeringCloud(); // 生成药水云

            if (unbreaking > 0) {
                this.setInvulnerable(true); // 设置为无敌
            }

            if (windBurst > 0) {
                Entity target = this.getTarget();
                for (int i = 0; i < 1 + power; i++) {
                    this.level().explode(this, null, EXPLOSION_DAMAGE_CALCULATOR, this.getX(), this.getY() + 1, this.getZ(),
                            windBurst,
                            false,
                            Level.ExplosionInteraction.TRIGGER,
                            ParticleTypes.GUST_EMITTER_SMALL,
                            ParticleTypes.GUST_EMITTER_LARGE,
                            SoundEvents.WIND_CHARGE_BURST
                    );
                    if (target != null) {
                        this.level().explode(this, null, EXPLOSION_DAMAGE_CALCULATOR, target.getX(), target.getY(), target.getZ(),
                                windBurst,
                                false,
                                Level.ExplosionInteraction.TRIGGER,
                                ParticleTypes.GUST_EMITTER_SMALL,
                                ParticleTypes.GUST_EMITTER_LARGE,
                                SoundEvents.WIND_CHARGE_BURST
                        );
                    }
                }
            } else if (fireAspect > 0) {
                this.level().explode(this, this.getX(), this.getY(), this.getZ(), (3 + power) * f, true, Level.ExplosionInteraction.MOB);
            } else {
                this.level().explode(this, this.getX(), this.getY(), this.getZ(), (3 + power) * f, Level.ExplosionInteraction.MOB);
            }

            int ran = random.nextInt(10);
            if (ran >= unbreaking) {
                this.triggerOnDeathMobEffects(Entity.RemovalReason.KILLED);
                this.discard();
            } else {
                this.setInvulnerable(false); // 取消无敌状态
                this.entityData.set(DATA_IS_IGNITED, false); // 将点燃状态设置为false
                this.entityData.set(DATA_SWELL_DIR, -10); // 设置实体膨胀方向
                this.swell = -10; // 设置膨胀值
            }

            ci.cancel();
        }
    }

}
