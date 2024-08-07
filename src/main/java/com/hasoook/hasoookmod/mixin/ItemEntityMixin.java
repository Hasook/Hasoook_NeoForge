package com.hasoook.hasoookmod.mixin;

import com.hasoook.hasoookmod.enchantment.ModEnchantmentHelper;
import com.hasoook.hasoookmod.enchantment.ModEnchantments;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.TraceableEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.SimpleExplosionDamageCalculator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.function.Function;

@Mixin(ItemEntity.class)
public abstract class ItemEntityMixin extends Entity implements TraceableEntity {

    @Shadow public abstract ItemStack getItem();

    @Shadow @Nullable public abstract Entity getOwner();

    public ItemEntityMixin(EntityType<?> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    private static final ExplosionDamageCalculator EXPLOSION_DAMAGE_CALCULATOR = new SimpleExplosionDamageCalculator(
            true, false, Optional.of(1.22F), BuiltInRegistries.BLOCK.getTag(BlockTags.BLOCKS_WIND_CHARGE_EXPLOSIONS).map(Function.identity())
    );

    @Inject(at = @At("HEAD"), method = "tick", cancellable = true)
    public void tick(CallbackInfo ci) {
        // 人机分离十米自动爆炸
        ItemStack itemStack = this.getItem(); // 获取物品栈
        int count = itemStack.getCount(); // 获取物品数量
        Entity owner = this.getOwner(); // 获取物品的主人
        int se = ModEnchantmentHelper.getEnchantmentLevel(ModEnchantments.SEPARATION_EXPLOSION, itemStack);
        // 获取物品的“分离爆炸”等级
        int wb = ModEnchantmentHelper.getEnchantmentLevel(Enchantments.WIND_BURST, itemStack);
        // 获取物品的“风爆”等级

        if (se > 0 && owner != null) { // 如果“分离爆炸”等级大于0 而且 主人不为空
            double distance = this.distanceTo(owner); // 获取和主人的距离
            if (distance > 5 + se * 5) { // 判断两者的距离
                if (wb > 0 || itemStack.getItem() == Items.WIND_CHARGE) {
                    // 如果物品附魔有“风爆” 或者 物品是风弹，就产生风弹爆炸
                    for (int i = 0; i < count; i++) {
                        this.level().explode(this, null, EXPLOSION_DAMAGE_CALCULATOR, this.getX(), this.getY(), this.getZ(),
                                1.2F,
                                false,
                                Level.ExplosionInteraction.TRIGGER,
                                ParticleTypes.GUST_EMITTER_SMALL,
                                ParticleTypes.GUST_EMITTER_LARGE,
                                SoundEvents.WIND_CHARGE_BURST
                        );
                    }
                } else {
                    // 否则产生普通爆炸
                    this.level().explode(this, this.getX(), this.getY(), this.getZ(), count, Level.ExplosionInteraction.MOB);
                }
                this.discard(); // 移除目标
                ci.cancel(); // 取消执行
            }
        }

    }

}
