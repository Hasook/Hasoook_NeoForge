package com.hasoook.hasoookmod.mixin;

import com.hasoook.hasoookmod.enchantment.ModEnchantmentHelper;
import com.hasoook.hasoookmod.enchantment.ModEnchantments;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.network.protocol.game.ClientboundTeleportEntityPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.TraceableEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.SimpleExplosionDamageCalculator;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
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

    @Shadow public abstract void setItem(ItemStack p_32046_);

    public ItemEntityMixin(EntityType<?> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    @Unique
    private static final ExplosionDamageCalculator EXPLOSION_DAMAGE_CALCULATOR = new SimpleExplosionDamageCalculator(
            true, false, Optional.of(1.22F), BuiltInRegistries.BLOCK.getTag(BlockTags.BLOCKS_WIND_CHARGE_EXPLOSIONS).map(Function.identity())
    );

    @Inject(at = @At("HEAD"), method = "tick", cancellable = true)
    public void tick(CallbackInfo ci) {

        ItemStack itemStack = this.getItem(); // 获取物品栈
        int count = itemStack.getCount(); // 获取物品数量
        Entity owner = this.getOwner(); // 获取物品的主人
        int separationLevel = ModEnchantmentHelper.getEnchantmentLevel(ModEnchantments.SEPARATION_EXPLOSION, itemStack);
        // 获取物品的“分离爆炸”等级
        int windLevel = ModEnchantmentHelper.getEnchantmentLevel(Enchantments.WIND_BURST, itemStack);
        // 获取物品的“风爆”等级
        int disdainLevel = ModEnchantmentHelper.getEnchantmentLevel(ModEnchantments.DISDAIN, itemStack);
        // 获取物品的“嫌弃”等级

        // 人机分离十米自动爆炸
        if (separationLevel > 0 && owner != null && !this.level().isClientSide) { // 如果“分离爆炸”等级大于0 而且 主人不为空
            double distance = this.distanceTo(owner); // 获取和主人的距离
            if (distance > 11 - separationLevel) { // 判断两者的距离
                hasoookNeoForge$explodeOnSeparation(count, windLevel, itemStack); //调用方法
                this.discard(); // 移除目标
                ci.cancel(); // 取消执行
            }
        }

        // 嫌弃
        if (disdainLevel > 0 && owner != null && !this.level().isClientSide) {
            double distance = this.distanceTo(owner);
            // 如果距离小于 5 * level 的距离
            if (distance < 5 * disdainLevel && this.onGround()) {
                hasoookNeoForge$disdain(owner);
            }
        }
    }

    @Unique
    private void hasoookNeoForge$explodeOnSeparation(int count, int wb, ItemStack itemStack) {
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
    }

    @Unique
    private void hasoookNeoForge$disdain(Entity owner) {
        double dx = this.getX() - owner.getX();
        double dz = this.getZ() - owner.getZ();
        double magnitude = Math.sqrt(dx * dx + dz * dz);
        dx /= magnitude;
        dz /= magnitude;

        double speed = 0.3; // 速度
        Vec3 deltaMovement = new Vec3(dx * speed, this.getDeltaMovement().y, dz * speed);
        this.setDeltaMovement(deltaMovement);

        // 发送速度网络包
        if (this.level() instanceof ServerLevel serverLevel) {
            // 将实体的速度发送到所有玩家
            serverLevel.getPlayers(player -> player instanceof ServerPlayer).forEach(player -> {
                Packet<?> packet = new ClientboundSetEntityMotionPacket(this);
                ((ServerPlayer) player).connection.send(packet);
            });
        }
    }

}
