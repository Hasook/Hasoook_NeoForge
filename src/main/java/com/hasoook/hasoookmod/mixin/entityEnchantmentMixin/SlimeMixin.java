package com.hasoook.hasoookmod.mixin.entityEnchantmentMixin;

import com.hasoook.hasoookmod.entityEnchantment.EntityEnchantmentHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Slime.class)
public abstract class SlimeMixin extends Mob implements Enemy {

    @Shadow public abstract int getSize();

    protected SlimeMixin(EntityType<? extends Mob> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    @Inject(method = "remove", at = @At("HEAD"), cancellable = true)
    public void remove(RemovalReason pReason, CallbackInfo ci) {
        int fortune = EntityEnchantmentHelper.getEnchantmentLevel(this,"minecraft:fortune");
        if (fortune > 0) {
            int i = this.getSize();
            if (!this.level().isClientSide && i > 1 && this.isDeadOrDying()) {
                Component component = this.getCustomName();
                boolean flag = this.isNoAi();
                float f = this.getDimensions(this.getPose()).width();
                float f1 = f / 2.0F;
                int j = i / 2;
                int k = fortune + 2 + this.random.nextInt(3 + fortune);

                var children = new java.util.ArrayList<Mob>();

                for (int l = 0; l < k; l++) {
                    float f2 = ((float)(l % 2) - 0.5F) * f1;
                    float f3 = ((float)(l / 2) - 0.5F) * f1;
                    Slime slime = (Slime) this.getType().create(this.level());
                    if (slime != null) {
                        if (this.isPersistenceRequired()) {
                            slime.setPersistenceRequired();
                        }

                        slime.setCustomName(component);
                        slime.setNoAi(flag);
                        slime.setInvulnerable(this.isInvulnerable());
                        slime.setSize(j, true);
                        slime.moveTo(this.getX() + (double)f2, this.getY() + 0.5, this.getZ() + (double)f3, this.random.nextFloat() * 360.0F, 0.0F);
                        EntityEnchantmentHelper.addEnchantment(slime,"minecraft:fortune",fortune);

                        children.add(slime); // Neo: Record the slime until after event firing.
                    }
                }

                if (!net.neoforged.neoforge.event.EventHooks.onMobSplit(this, children).isCanceled()) {
                    children.forEach(this.level()::addFreshEntity);
                }
            }
            super.remove(pReason);
            ci.cancel();
        }
    }
}
