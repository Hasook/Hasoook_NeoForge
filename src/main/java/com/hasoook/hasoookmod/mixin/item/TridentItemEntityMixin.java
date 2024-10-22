package com.hasoook.hasoookmod.mixin.item;

import com.hasoook.hasoookmod.enchantment.ModEnchantmentHelper;
import com.hasoook.hasoookmod.enchantment.ModEnchantments;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TraceableEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(ItemEntity.class)
public abstract class TridentItemEntityMixin extends Entity implements TraceableEntity {
    @Shadow public abstract ItemStack getItem();

    public TridentItemEntityMixin(EntityType<?> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    @Inject(at = @At("HEAD"), method = "tick", cancellable = true)
    public void tick(CallbackInfo ci) {
        ItemStack itemStack = this.getItem(); // 获取物品栈
        int count = itemStack.getCount(); // 获取物品数量
        Entity owner = this.getOwner(); // 获取物品的主人
        int betrayLevel = ModEnchantmentHelper.getEnchantmentLevel(ModEnchantments.BETRAY, itemStack);
        // 获取物品的“背叛”等级

        // 背叛
        if (betrayLevel > 0 && owner != null && this.onGround() && !this.level().isClientSide) {
            // 检查主人是否在看着生物
            if (!isOwnerLookingAt(owner)) {
                // 获取附近其他生物
                List<Entity> nearbyEntities = this.level().getEntities(this, this.getBoundingBox().inflate(10)); // 修改半径根据需要
                Entity target = null;

                // 找到一个最近的生物作为目标
                for (Entity entity : nearbyEntities) {
                    if (entity != this && entity != owner) {
                        if (target == null || this.distanceTo(entity) < this.distanceTo(target)) {
                            target = entity;
                        }
                    }
                }

                // 如果找到了目标生物，计算运动向量
                if (target != null) {
                    Vec3 moveVector = target.position().subtract(this.position()).normalize().scale(0.1); // 调整速度因子
                    this.setDeltaMovement(moveVector);

                    // 如果与目标实体距离小于 1 格，则设置目标实体的主手物品
                    if (this.distanceTo(target) < 1) {
                        if (target instanceof LivingEntity livingTarget) {
                            // 检查主手是否有物品
                            ItemStack mainHandItem = livingTarget.getItemInHand(InteractionHand.MAIN_HAND);
                            if (!mainHandItem.isEmpty()) {
                                // 扔出主手物品
                                ItemEntity thrownItem = new ItemEntity(this.level(), livingTarget.getX(), livingTarget.getY() + 1.5, livingTarget.getZ(), mainHandItem.copy());
                                thrownItem.setPickUpDelay(10); // 设置拾取延迟
                                this.level().addFreshEntity(thrownItem); // 将物品添加到世界中
                                livingTarget.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY); // 清空主手物品
                            }
                            livingTarget.setItemInHand(InteractionHand.MAIN_HAND, this.getItem()); // 设置目标的主手物品为当前生物的物品
                            this.discard();
                        }
                    }

                    if (this.level() instanceof ServerLevel serverLevel) {
                        // 将实体的速度发送到所有玩家
                        serverLevel.getPlayers(player -> player instanceof ServerPlayer).forEach(player -> {
                            Packet<?> packet = new ClientboundSetEntityMotionPacket(this);
                            ((ServerPlayer) player).connection.send(packet);
                        });
                    }
                }
            }
        }
    }

    private boolean isOwnerLookingAt(Entity owner) {
        Vec3 ownerEyePosition = owner.position().add(0, owner.getEyeHeight(), 0);
        Vec3 direction = owner.getViewVector(1.0F).normalize();
        Vec3 toTarget = this.position().subtract(ownerEyePosition).normalize();

        double distanceSquared = ownerEyePosition.distanceToSqr(this.position());
        double maxDistanceSquared = 648; // 设定最大视距

        // 判断目标是否在视野范围内和距离内
        if (distanceSquared < maxDistanceSquared) {
            double dotProduct = direction.dot(toTarget);
            return dotProduct > Math.cos(Math.toRadians(45)); // 30度视野
        }
        return false;
    }
}
