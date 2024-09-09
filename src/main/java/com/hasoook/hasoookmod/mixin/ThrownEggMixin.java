package com.hasoook.hasoookmod.mixin;

import com.hasoook.hasoookmod.enchantment.ModEnchantmentHelper;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Chicken;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.entity.projectile.ThrownEgg;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ThrownEgg.class)
public abstract class ThrownEggMixin extends ThrowableItemProjectile {
    public ThrownEggMixin(EntityType<? extends ThrowableItemProjectile> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    @Unique
    private static final EntityDimensions ZERO_SIZED_DIMENSIONS = EntityDimensions.fixed(0.0F, 0.0F);

    @Override
    protected void onHit(HitResult pResult) {
        super.onHit(pResult);
        ItemStack itemStack = this.getItem(); // 获取鸡蛋的 ItemStack
        int fortuneLevel = ModEnchantmentHelper.getEnchantmentLevel(Enchantments.FORTUNE, itemStack);
        // 获取物品的“时运”等级
        if (!this.level().isClientSide && fortuneLevel > 0) {
            if (this.random.nextInt(8) == 0) {
                int i = 1;
                if (this.random.nextInt(32) == 0) { // 1/32的概率生成4只鸡
                    i = 4;
                }

                i += this.random.nextInt(fortuneLevel * 2); // 加上随机值（0 ~ 2倍时运等级）

                for (int j = 0; j < i; j++) {
                    Chicken chicken = EntityType.CHICKEN.create(this.level());
                    if (chicken != null) {
                        chicken.setAge(-24000);
                        chicken.moveTo(this.getX(), this.getY(), this.getZ(), this.getYRot(), 0.0F);

                        if (i > 1) { // 如果鸡的数量大于1
                            double randomX = (this.random.nextDouble() - 0.5) * 0.2;
                            double randomY = (this.random.nextDouble() - 0.5) * 0.2;
                            double randomZ = (this.random.nextDouble() - 0.5) * 0.2;
                            chicken.setDeltaMovement(randomX, randomY, randomZ);
                            // 设置随机方向的运动向量 范围 -0.1 ~ 0.1
                            double randomJ = (double) j / 6; // 位置偏移
                            chicken.moveTo(this.getX() + randomX * randomJ, this.getY(), this.getZ() + randomZ * randomJ, this.getYRot(), 0.0F);
                        }

                        if (!chicken.fudgePositionAfterSizeChange(ZERO_SIZED_DIMENSIONS)) {
                            break;
                        }
                        this.level().addFreshEntity(chicken);
                    }
                }
            }
        }
        this.level().broadcastEntityEvent(this, (byte)3);
        this.discard();
    }

}
