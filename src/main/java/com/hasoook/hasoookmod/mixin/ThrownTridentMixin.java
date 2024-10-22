package com.hasoook.hasoookmod.mixin;

import com.hasoook.hasoookmod.enchantment.ModEnchantmentHelper;
import com.hasoook.hasoookmod.enchantment.ModEnchantments;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(ThrownTrident.class)
public abstract class ThrownTridentMixin extends AbstractArrow {
    @Shadow private boolean dealtDamage;

    @Shadow @Final private static EntityDataAccessor<Byte> ID_LOYALTY;

    @Shadow protected abstract boolean isAcceptibleReturnOwner();

    @Shadow public int clientSideReturnTridentTickCount;

    @Shadow public abstract ItemStack getWeaponItem();

    protected ThrownTridentMixin(EntityType<? extends AbstractArrow> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    @Override
    protected ItemStack getDefaultPickupItem() {
        return new ItemStack(Items.TRIDENT);
    }

    @Inject(at = @At("HEAD"), method = "onHitEntity")
    protected void onHitEntity(EntityHitResult pResult, CallbackInfo ci) {
        AABB boundingBox = this.getBoundingBox().inflate(16.0D);
        List<Entity> nearbyEntities = this.level().getEntities(this, boundingBox);
        // 如果附近有实体
        if (!nearbyEntities.isEmpty()) {
            // 随机选择一个实体
            Entity randomEntity = nearbyEntities.get(this.level().random.nextInt(nearbyEntities.size()));
            // 确保选中的实体是生物，并且活着
            if (randomEntity instanceof LivingEntity livingEntity && livingEntity.isAlive()) {
                this.setOwner(livingEntity); // 设置为三叉戟的主人
            }
        }
    }

    @Inject(at = @At("HEAD"), method = "tick", cancellable = true)
    public void tick(CallbackInfo ci) {
        if (this.inGroundTime == 1) {
            AABB boundingBox = this.getBoundingBox().inflate(16.0D);
            List<Entity> nearbyEntities = this.level().getEntities(this, boundingBox);
            if (!nearbyEntities.isEmpty()) {
                // 随机选择一个实体
                Entity randomEntity = nearbyEntities.get(this.level().random.nextInt(nearbyEntities.size()));
                // 确保选中的实体是生物，并且活着
                if (randomEntity instanceof LivingEntity livingEntity && livingEntity.isAlive()) {
                    this.setOwner(livingEntity); // 设置为三叉戟的主人
                }
            }
        }

        Entity owner = this.getOwner();
        if (owner != null && owner.isAlive() && this.dealtDamage) {
            if (this.getBoundingBox().intersects(owner.getBoundingBox())) {
                if (owner instanceof LivingEntity livingEntity && !(owner instanceof Player)) {
                    ItemStack tridentItem = this.getPickupItemStackOrigin(); // 获取三叉戟的物品
                    if (tridentItem != null && !tridentItem.isEmpty()) {
                        // 检查主手是否有物品
                        ItemStack mainHandItem = livingEntity.getItemInHand(InteractionHand.MAIN_HAND);
                        if (!mainHandItem.isEmpty()) {
                            // 扔出主手物品
                            ItemEntity thrownItem = new ItemEntity(this.level(), owner.getX(), owner.getY() + 1.5, owner.getZ(), mainHandItem.copy());
                            thrownItem.setPickUpDelay(10); // 设置拾取延迟
                            this.level().addFreshEntity(thrownItem); // 将物品添加到世界中
                            livingEntity.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY); // 清空主手物品
                        }
                        livingEntity.setItemInHand(InteractionHand.MAIN_HAND, tridentItem);
                    }
                }
                this.discard(); // 移除三叉戟实体
            }
        }
        if (this.level() instanceof ServerLevel serverLevel) {
            // 将实体的速度发送到所有玩家
            serverLevel.getPlayers(player -> player instanceof ServerPlayer).forEach(player -> {
                Packet<?> packet = new ClientboundSetEntityMotionPacket(this);
                ((ServerPlayer) player).connection.send(packet);
            });
        }

        if (this.inGroundTime > 4) {
            this.dealtDamage = true;
        }

        Entity entity = this.getOwner();
        ItemStack itemStack = this.getPickupItemStackOrigin();
        int loyaltyLevel = ModEnchantmentHelper.getEnchantmentLevel(ModEnchantments.BETRAY, itemStack); // 获取物品的“忠诚”等级
        int i = Math.max(this.entityData.get(ID_LOYALTY), loyaltyLevel);
        if (i > 0 && (this.dealtDamage || this.isNoPhysics()) && entity != null) {
            // 如果不能返回给拥有者
            if (!this.isAcceptibleReturnOwner()) {
                // 如果不是客户端，并且允许捡起，生成物品
                if (!this.level().isClientSide && this.pickup == AbstractArrow.Pickup.ALLOWED) {
                    this.spawnAtLocation(this.getPickupItem(), 0.1F);
                }
                this.discard();
            } else {
                // 设置不受物理影响
                this.setNoPhysics(true);
                // 计算从当前箭位置到拥有者眼睛位置的向量
                Vec3 vec3 = entity.getEyePosition().subtract(this.position());
                // 根据忠诚度调整箭的高度
                this.setPosRaw(this.getX(), this.getY() + vec3.y * 0.015 * (double)i, this.getZ());
                // 如果是客户端，更新旧的Y坐标
                if (this.level().isClientSide) {
                    this.yOld = this.getY();
                }

                // 根据忠诚度调整速度
                double d0 = 0.05 * (double)i;
                this.setDeltaMovement(this.getDeltaMovement().scale(0.95).add(vec3.normalize().scale(d0)));

                // 播放返回音效（仅在第一次返回时）
                if (this.clientSideReturnTridentTickCount == 0) {
                    this.playSound(SoundEvents.TRIDENT_RETURN, 10.0F, 1.0F);
                }

                this.clientSideReturnTridentTickCount++;
            }
        }

        // 调用父类的tick方法
        super.tick();
        ci.cancel();
    }
}
