package com.hasoook.hasoookmod.mixin.item;

import com.hasoook.hasoookmod.enchantment.ModEnchantmentHelper;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ThrownEgg;
import net.minecraft.world.item.EggItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileItem;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EggItem.class)
public class EggItemMixin extends Item implements ProjectileItem {

    public EggItemMixin(Properties pProperties) {
        super(pProperties);
    }

    @Inject(method = "use", at = @At("HEAD"))
    public void use(Level pLevel, Player pPlayer, InteractionHand pHand, CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir) {
        ItemStack itemstack = pPlayer.getItemInHand(pHand);
        int multishot = ModEnchantmentHelper.getEnchantmentLevel(Enchantments.MULTISHOT, itemstack);
        if (!pLevel.isClientSide && multishot > 0) {
            for (int i = 0; i < multishot + 1; i++) {
                ThrownEgg thrownegg = new ThrownEgg(pLevel, pPlayer);
                thrownegg.setItem(itemstack);
                thrownegg.shootFromRotation(pPlayer, pPlayer.getXRot(), pPlayer.getYRot(), 0.0F, 1.5F, 4.0F);
                pLevel.addFreshEntity(thrownegg);
            }
        }
    }

    @Override
    public @NotNull Projectile asProjectile(@NotNull Level pLevel, Position pPos, @NotNull ItemStack pStack, @NotNull Direction pDirection) {
        ThrownEgg thrownegg = new ThrownEgg(pLevel, pPos.x(), pPos.y(), pPos.z());
        thrownegg.setItem(pStack);
        return thrownegg;
    }
}
