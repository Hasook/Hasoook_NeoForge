package com.hasoook.hasoookmod.mixin;

import com.hasoook.hasoookmod.enchantment.ModEnchantmentHelper;
import com.hasoook.hasoookmod.enchantment.ModEnchantments;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.TraceableEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;

@Mixin(ItemEntity.class)
public abstract class ItemEntityMixin extends Entity implements TraceableEntity {

    @Shadow public abstract ItemStack getItem();

    @Shadow @Nullable public abstract Entity getOwner();

    public ItemEntityMixin(EntityType<?> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    @Inject(at = @At("HEAD"), method = "tick", cancellable = true)
    public void tick(CallbackInfo ci) {
        ItemStack itemStack = this.getItem(); // 获取物品栈
        int count = itemStack.getCount(); // 获取物品数量
        Entity owner = this.getOwner(); // 获取物品的主人
        int se = ModEnchantmentHelper.getEnchantmentLevel(ModEnchantments.SEPARATION_EXPLOSION, itemStack);
        // 获取物品的“分离爆炸”等级

        if (se > 0 && owner != null) { // 如果“分离爆炸”等级大于0 而且 主人不为空
            double distance = this.distanceTo(owner); // 获取和主人的距离
            if (distance > 5 + se * 5) { // 判断两者的距离
                this.level().explode(this, this.getX(), this.getY(), this.getZ(), count, Level.ExplosionInteraction.MOB);
                this.discard(); // 移除目标
                ci.cancel(); // 取消执行
            }
        }

    }

}
