package com.hasoook.hasoookmod.mixin.entityEnchantmentMixin;

import com.hasoook.hasoookmod.entityEnchantment.EntityEnchantmentHelper;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PowerableMob;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.SimpleExplosionDamageCalculator;
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

    @Unique
    private static final ExplosionDamageCalculator EXPLOSION_DAMAGE_CALCULATOR = new SimpleExplosionDamageCalculator(
            true, false, Optional.of(1.22F), BuiltInRegistries.BLOCK.getTag(BlockTags.BLOCKS_WIND_CHARGE_EXPLOSIONS).map(Function.identity())
    );

    @Inject(method = "explodeCreeper", at = @At("HEAD"), cancellable = true)
    private void explodeCreeper(CallbackInfo ci) {
        int power = EntityEnchantmentHelper.getEnchantmentLevel(this, "minecraft:power");
        int windBurst = EntityEnchantmentHelper.getEnchantmentLevel(this, "minecraft:wind_burst");
        int size = EntityEnchantmentHelper.getEnchantmentSize(this);

        if (size > 0 && !this.level().isClientSide) {
            float f = this.isPowered() ? 2.0F : 1.0F;
            this.dead = true;
            this.spawnLingeringCloud();
            if (windBurst > 0) {
                for (int i = 0; i < windBurst; i++) {
                    this.level().explode(this, null, EXPLOSION_DAMAGE_CALCULATOR, this.getX(), this.getY() + 1, this.getZ(),
                            3 + power,
                            false,
                            Level.ExplosionInteraction.TRIGGER,
                            ParticleTypes.GUST_EMITTER_SMALL,
                            ParticleTypes.GUST_EMITTER_LARGE,
                            SoundEvents.WIND_CHARGE_BURST
                    );
                }
            } else {
                this.level().explode(this, this.getX(), this.getY(), this.getZ(), (3 + power) * f, Level.ExplosionInteraction.MOB);
            }
            this.triggerOnDeathMobEffects(Entity.RemovalReason.KILLED);
            this.discard();
            ci.cancel();
        }
    }

}
