package com.hasoook.hasoookmod.mixin.item;

import com.hasoook.hasoookmod.enchantment.ModEnchantmentHelper;
import com.hasoook.hasoookmod.enchantment.ModEnchantments;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import static net.minecraft.world.item.BowItem.getPowerForTime;

@Mixin(BowItem.class)
public class BowMixin extends Item {
    public BowMixin(Properties pProperties) {
        super(pProperties);
    }

    @Inject(at = @At("HEAD"), method = "releaseUsing", cancellable = true)
    public void releaseUsing(ItemStack pStack, Level pLevel, LivingEntity pEntityLiving, int pTimeLeft, CallbackInfo ci) {
        int i = ModEnchantmentHelper.getEnchantmentLevel(ModEnchantments.RANDOM_BULLETS, pStack);
        if (i >= 1) {
            Random random = new Random();
            int number = random.nextInt(10);
            hasoookNeoForge$randomBullets(pStack, pLevel, pEntityLiving, pTimeLeft);
            ci.cancel();
        }
    }

    @Unique
    List<EntityType<? extends Entity>> possibleEntities = Arrays.asList(
            EntityType.EXPERIENCE_BOTTLE,
            EntityType.SPECTRAL_ARROW,
            EntityType.TNT,
            EntityType.SNOWBALL,
            EntityType.EGG,
            EntityType.LLAMA_SPIT,
            EntityType.SHULKER_BULLET,
            EntityType.DRAGON_FIREBALL,
            EntityType.FIREBALL,
            EntityType.SMALL_FIREBALL,
            EntityType.PUFFERFISH,
            EntityType.TRIDENT,
            EntityType.ARROW,
            EntityType.WIND_CHARGE
    );

    @Unique
    public void hasoookNeoForge$randomBullets(ItemStack pStack, Level pLevel, LivingEntity pEntityLiving, int pTimeLeft) {
        Random randomEntity = new Random();
        EntityType<? extends Entity> randomEntityType = possibleEntities.get(randomEntity.nextInt(possibleEntities.size()));
        Entity entity = randomEntityType.create(pLevel);
        int i = this.getUseDuration(pStack, pEntityLiving) - pTimeLeft;
        float time = getPowerForTime(i);
        if (entity != null) {
            entity.setPos(pEntityLiving.getX(), pEntityLiving.getY() + pEntityLiving.getEyeHeight(pEntityLiving.getPose()) - 0.2, pEntityLiving.getZ());
            Vec3 lookVec = pEntityLiving.getLookAngle(); // 获取玩家的视线方向向量
            double speed = 3.0 * time; // 设置速度倍率（乘以拉弓时间）
            entity.setDeltaMovement(lookVec.x * speed, lookVec.y * speed, lookVec.z * speed);
            pLevel.addFreshEntity(entity);
        }
    }

}
