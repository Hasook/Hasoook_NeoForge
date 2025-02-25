package com.hasoook.hasoookmod.item.custom;

import com.hasoook.hasoookmod.effect.ModEffects;
import com.hasoook.hasoookmod.entity.ModEntityHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class DiskCricketMotionBadge extends Item {
    public DiskCricketMotionBadge(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public @NotNull InteractionResult interactLivingEntity(@NotNull ItemStack pStack, Player pPlayer, @NotNull LivingEntity pInteractionTarget, @NotNull InteractionHand pUsedHand) {
        if (!pPlayer.level().isClientSide && ModEntityHelper.isWhiteMob(pInteractionTarget) && !pInteractionTarget.hasEffect(ModEffects.GO_WORK)) {
            pInteractionTarget.addEffect(new MobEffectInstance(ModEffects.GO_WORK, MobEffectInstance.INFINITE_DURATION, 0, true, false, true));
            pStack.shrink(1);
            pPlayer.displayClientMessage(Component.literal("恭喜" + pInteractionTarget.getName().getString() + "成为了FBI的一员！"), false);
        }
        pPlayer.swing(pUsedHand);
        return super.interactLivingEntity(pStack, pPlayer, pInteractionTarget, pUsedHand);
    }
}
