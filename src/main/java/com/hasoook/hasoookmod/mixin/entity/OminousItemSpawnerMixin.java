package com.hasoook.hasoookmod.mixin.entity;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.OminousItemSpawner;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(OminousItemSpawner.class)
public abstract class OminousItemSpawnerMixin extends Entity {
    @Shadow public abstract ItemStack getItem();

    @Shadow protected abstract void setItem(ItemStack pItem);

    @Shadow private long spawnItemAfterTicks;

    @Shadow @Final private static EntityDataAccessor<ItemStack> DATA_ITEM;

    public OminousItemSpawnerMixin(EntityType<?> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        this.noPhysics = true;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder pBuilder) {
        pBuilder.define(DATA_ITEM, ItemStack.EMPTY);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag pCompound) {
        ItemStack itemstack = pCompound.contains("item", 10)
                ? ItemStack.parse(this.registryAccess(), pCompound.getCompound("item")).orElse(ItemStack.EMPTY)
                : ItemStack.EMPTY;
        this.setItem(itemstack);
        this.spawnItemAfterTicks = pCompound.getLong("spawn_item_after_ticks");
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag pCompound) {
        if (!this.getItem().isEmpty()) {
            pCompound.put("item", this.getItem().save(this.registryAccess()).copy());
        }

        pCompound.putLong("spawn_item_after_ticks", this.spawnItemAfterTicks);
    }

    // neoforge的代码会使setOwner设置一个null对象，导致游戏崩溃
    @Inject(method = "spawnItem", at = @At("HEAD"), cancellable = true)
    private void spawnItem(CallbackInfo ci) {
        Level level = this.level();
        ItemStack itemstack = this.getItem();
        if (!itemstack.isEmpty()) {
            Entity entity;
            if (itemstack.getItem() instanceof ProjectileItem projectileitem) {
                Direction direction = Direction.DOWN;
                // 尝试创建投射物
                Projectile projectile = projectileitem.asProjectile(level, this.position(), itemstack, direction);

                // 检查投射物是否成功创建
                if (projectile != null) {
                    projectile.setOwner(this); // 只有在投射物不为null时才设置拥有者
                    ProjectileItem.DispenseConfig projectileitem$dispenseconfig = projectileitem.createDispenseConfig();
                    projectileitem.shoot(
                            projectile,
                            (double)direction.getStepX(),
                            (double)direction.getStepY(),
                            (double)direction.getStepZ(),
                            projectileitem$dispenseconfig.power(),
                            projectileitem$dispenseconfig.uncertainty()
                    );
                    projectileitem$dispenseconfig.overrideDispenseEvent().ifPresent(p_352709_ -> level.levelEvent(p_352709_, this.blockPosition(), 0));
                    entity = projectile;
                } else {
                    // 如果投射物创建失败，创建一个普通的物品实体
                    entity = new ItemEntity(level, this.getX(), this.getY(), this.getZ(), itemstack);
                }
            } else {
                // 如果不是投射物，直接创建物品实体
                entity = new ItemEntity(level, this.getX(), this.getY(), this.getZ(), itemstack);
            }

            // 将实体添加到世界
            level.addFreshEntity(entity);
            level.levelEvent(3021, this.blockPosition(), 1);
            level.gameEvent(entity, GameEvent.ENTITY_PLACE, this.position());
            // 清空当前物品
            this.setItem(ItemStack.EMPTY);
        }
        ci.cancel();
    }
}
