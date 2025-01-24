package com.hasoook.hasoookmod.mixin.item;

import com.hasoook.hasoookmod.enchantment.ModEnchantmentHelper;
import com.hasoook.hasoookmod.enchantment.ModEnchantments;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.windcharge.WindCharge;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileItem;
import net.minecraft.world.item.WindChargeItem;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(WindChargeItem.class)
public class WindChargeItemMixin extends Item implements ProjectileItem {
    public WindChargeItemMixin(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public Projectile asProjectile(Level pLevel, Position pPos, ItemStack pStack, Direction pDirection) {
        return null;
    }

    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    public void use(Level p_326306_, Player p_326042_, InteractionHand p_326470_, CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir) {
        if (!p_326306_.isClientSide()) {
            int tornadoLvl = ModEnchantmentHelper.getEnchantmentLevel(ModEnchantments.TORNADO, p_326042_.getItemInHand(p_326470_));
            WindCharge windcharge = new WindCharge(p_326042_, p_326306_, p_326042_.position().x(), p_326042_.getEyePosition().y(), p_326042_.position().z());
            windcharge.shootFromRotation(p_326042_, p_326042_.getXRot(), p_326042_.getYRot(), 0.0F, 1.5F, 1.0F);
            windcharge.getPersistentData().putInt("fission",tornadoLvl);
            p_326306_.addFreshEntity(windcharge);
        }

        p_326306_.playSound(
                null,
                p_326042_.getX(),
                p_326042_.getY(),
                p_326042_.getZ(),
                SoundEvents.WIND_CHARGE_THROW,
                SoundSource.NEUTRAL,
                0.5F,
                0.4F / (p_326306_.getRandom().nextFloat() * 0.4F + 0.8F)
        );

        ItemStack itemstack = p_326042_.getItemInHand(p_326470_);
        p_326042_.getCooldowns().addCooldown(this, 10);
        p_326042_.awardStat(Stats.ITEM_USED.get(this));
        itemstack.consume(1, p_326042_);

        cir.setReturnValue(InteractionResultHolder.sidedSuccess(itemstack, p_326306_.isClientSide()));
    }
}
