package com.hasoook.hasoookmod.mixin.entity;

import com.hasoook.hasoookmod.entity.ModEntities;
import com.hasoook.hasoookmod.entity.custom.TornadoEntity;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.windcharge.AbstractWindCharge;
import net.minecraft.world.entity.projectile.windcharge.WindCharge;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WindCharge.class)
public class WindChargeMixin extends AbstractWindCharge {
    public WindChargeMixin(EntityType<? extends AbstractWindCharge> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    @Override
    protected void explode(Vec3 pPos) {
        this.level()
                .explode(
                        this,
                        null,
                        EXPLOSION_DAMAGE_CALCULATOR,
                        pPos.x(),
                        pPos.y(),
                        pPos.z(),
                        1.2F,
                        false,
                        Level.ExplosionInteraction.TRIGGER,
                        ParticleTypes.GUST_EMITTER_SMALL,
                        ParticleTypes.GUST_EMITTER_LARGE,
                        SoundEvents.WIND_CHARGE_BURST
                );
    }

    @Inject(method = "explode", at = @At("HEAD"), cancellable = true)
    protected void explode(Vec3 pPos, CallbackInfo ci) {
        int fissionLvl = this.getPersistentData().getInt("fission");
        Level level = this.level();
        if (fissionLvl > 0) {
            TornadoEntity tornado = new TornadoEntity(ModEntities.TORNADO.get(), level);
            tornado.setPos(pPos.x, pPos.y - 0.25, pPos.z);
            level.addFreshEntity(tornado);

            // 粒子和音效
            if (level instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(
                        ParticleTypes.GUST,
                        this.getX(),
                        this.getY(),
                        this.getZ(),
                        1,
                        0.0,
                        0.0,
                        0.0,
                        0.0
                );
                serverLevel.playSound(
                        null, this.getX(), this.getY(), this.getZ(), SoundEvents.WIND_CHARGE_BURST, this.getSoundSource(), 1.0F, 1.0F
                );
            }

            ci.cancel();
        }
    }
}
