package com.hasoook.hasoookmod.mixin.item;

import com.hasoook.hasoookmod.enchantment.ModEnchantmentHelper;
import com.hasoook.hasoookmod.enchantment.ModEnchantments;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.Position;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TridentItem.class)
public abstract class TridentItemMixin extends Item implements ProjectileItem {
    @Shadow
    private static boolean isTooDamagedToUse(ItemStack pStack) {
        return false;
    }

    public TridentItemMixin(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public @NotNull Projectile asProjectile(Level pLevel, Position pPos, ItemStack pStack, @NotNull Direction pDirection) {
        ThrownTrident throwntrident = new ThrownTrident(pLevel, pPos.x(), pPos.y(), pPos.z(), pStack.copyWithCount(1));
        throwntrident.pickup = AbstractArrow.Pickup.ALLOWED;
        return throwntrident;
    }

    @Inject(at = @At("HEAD"), method = "releaseUsing", cancellable = true)
    public void releaseUsing(ItemStack pStack, Level pLevel, LivingEntity pEntityLiving, int pTimeLeft, CallbackInfo ci) {
        int multishotLevel = ModEnchantmentHelper.getEnchantmentLevel(Enchantments.MULTISHOT, pStack);
        if (multishotLevel > 0 && pEntityLiving instanceof Player player) {
            int i = this.getUseDuration(pStack, pEntityLiving) - pTimeLeft; // 计算使用时间
            if (i >= 10) { // 满足投掷条件
                float f = EnchantmentHelper.getTridentSpinAttackStrength(pStack, player); // 获取旋转攻击的强度
                if (!(f > 0.0F) || player.isInWaterOrRain()) {
                    if (!isTooDamagedToUse(pStack)) {
                        // 获取音效
                        Holder<SoundEvent> holder = EnchantmentHelper.pickHighestLevel(pStack, EnchantmentEffectComponents.TRIDENT_SOUND)
                                .orElse(SoundEvents.TRIDENT_THROW);
                        if (!pLevel.isClientSide) {
                            // 减少物品耐久
                            pStack.hurtAndBreak(1, player, LivingEntity.getSlotForHand(pEntityLiving.getUsedItemHand()));
                            if (f == 0.0F) {
                                for (int j = 0; j < multishotLevel + 2; j++) {
                                    // 创建抛掷三叉戟实例并发射
                                    ThrownTrident throwntrident = new ThrownTrident(pLevel, player, pStack);
                                    throwntrident.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 2.5F, 1.0F + j * 5F);
                                    if (player.hasInfiniteMaterials()) {
                                        throwntrident.pickup = AbstractArrow.Pickup.CREATIVE_ONLY; // 创造模式只能捡起
                                    }

                                    pLevel.addFreshEntity(throwntrident); // 添加到世界中
                                    pLevel.playSound(null, throwntrident, holder.value(), SoundSource.PLAYERS, 1.0F, 1.0F); // 播放音效

                                }

                                if (!player.hasInfiniteMaterials()) {
                                    player.getInventory().removeItem(pStack); // 移除物品
                                }
                            }
                        }

                        player.awardStat(Stats.ITEM_USED.get(this)); // 奖励使用统计
                        if (f > 0.0F) {
                            // 处理旋转攻击
                            float f7 = player.getYRot();
                            float f1 = player.getXRot();
                            float f2 = -Mth.sin(f7 * (float) (Math.PI / 180.0)) * Mth.cos(f1 * (float) (Math.PI / 180.0));
                            float f3 = -Mth.sin(f1 * (float) (Math.PI / 180.0));
                            float f4 = Mth.cos(f7 * (float) (Math.PI / 180.0)) * Mth.cos(f1 * (float) (Math.PI / 180.0));
                            float f5 = Mth.sqrt(f2 * f2 + f3 * f3 + f4 * f4);
                            f2 *= f / f5;
                            f3 *= f / f5;
                            f4 *= f / f5;
                            player.push((double)f2, (double)f3, (double)f4); // 推送玩家
                            player.startAutoSpinAttack(20, 8.0F, pStack); // 开始自动旋转攻击
                            if (player.onGround()) {
                                float f6 = 1.1999999F; // 向上移动
                                player.move(MoverType.SELF, new Vec3(0.0, 1.1999999F, 0.0));
                            }

                            pLevel.playSound(null, player, holder.value(), SoundSource.PLAYERS, 1.0F, 1.0F); // 播放音效
                        }
                    }
                }
            }
            ci.cancel();
        }
    }
}
