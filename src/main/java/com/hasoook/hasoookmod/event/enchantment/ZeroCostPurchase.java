package com.hasoook.hasoookmod.event.enchantment;

import com.hasoook.hasoookmod.HasoookMod;
import com.hasoook.hasoookmod.effect.ModEffects;
import com.hasoook.hasoookmod.enchantment.ModEnchantmentHelper;
import com.hasoook.hasoookmod.enchantment.ModEnchantments;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.WanderingTrader;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.Path;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.List;
import java.util.Objects;
import java.util.Random;

@EventBusSubscriber(modid = HasoookMod.MOD_ID)
public class ZeroCostPurchase {
    @SubscribeEvent
    public static void PlayerTickEvent(PlayerTickEvent.Post event){
        Player player = event.getEntity();
        ItemStack itemStack = player.getMainHandItem();
        int ZCPLvl = ModEnchantmentHelper.getEnchantmentLevel(ModEnchantments.ZERO_COST_PURCHASE, itemStack);
        if (ZCPLvl > 0 && !player.level().isClientSide) {
            // 获取玩家当前所在的世界
            Level level = player.level();

            // 获取玩家周围5格的所有实体，包含村民和流浪商人
            List<Entity> entities = level.getEntitiesOfClass(Entity.class, player.getBoundingBox().inflate(10), EntitySelector.NO_SPECTATORS);

            // 对所有实体进行处理
            for (Entity entity : entities) {
                // 判断实体是否为村民或流浪商人
                if (entity instanceof Villager || entity instanceof WanderingTrader) {
                    Mob mobEntity = (Mob) entity;

                    // 计算逃离玩家的逻辑
                    double escapeDistance = 10.0; // 逃离的最小距离
                    double dx = mobEntity.getX() - player.getX();
                    double dy = mobEntity.getY() - player.getY();
                    double dz = mobEntity.getZ() - player.getZ();
                    double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);

                    // 如果实体距离玩家小于逃离距离
                    if (distance < escapeDistance) {
                        // 计算逃离方向
                        double escapeX = mobEntity.getX() + dx / distance * escapeDistance;
                        double escapeY = mobEntity.getY() + dy / distance * escapeDistance;
                        double escapeZ = mobEntity.getZ() + dz / distance * escapeDistance;

                        // 让实体逃离玩家
                        Path path = mobEntity.getNavigation().createPath(escapeX, escapeY, escapeZ, 1);
                        if (path != null) {
                            mobEntity.getNavigation().moveTo(path, 0.65D);

                            if (mobEntity.level() instanceof ServerLevel serverLevel && Math.random() > 0.5) {
                                serverLevel.sendParticles(ParticleTypes.SPLASH, mobEntity.getX(), mobEntity.getY() + mobEntity.getBbHeight(), mobEntity.getZ(), 1, 0.2, 0.2, 0.2, 0.02);
                            }
                        }
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void betrayAttack(LivingIncomingDamageEvent event) {
        LivingEntity entity = event.getEntity(); // 获取实体
        if (event.getSource().getEntity() instanceof LivingEntity sourceEntity) {

            int ZCPLvl = 0;

            ItemStack itemStack = sourceEntity.getMainHandItem();
            ZCPLvl = ModEnchantmentHelper.getEnchantmentLevel(ModEnchantments.ZERO_COST_PURCHASE, itemStack);

            if ((ZCPLvl > 0) && (entity instanceof Villager || entity instanceof WanderingTrader)) {

                MerchantOffers offers = null;
                if (entity instanceof Villager villager) {
                    offers = villager.getOffers(); // 获取村民交易列表
                } else if (entity instanceof WanderingTrader wanderingTrader) {
                    offers = wanderingTrader.getOffers(); // 获取流浪商人交易列表
                }

                if (!offers.isEmpty()) {
                    // 随机选择一个交易项
                    Random random = new Random();
                    MerchantOffer selectedOffer = offers.get(random.nextInt(offers.size()));

                    ItemStack resultItem = selectedOffer.getResult(); // 获取交易项中的物品
                    ItemStack dropItem = resultItem.copy(); // 复制物品

                    // 生成掉落物
                    if (!dropItem.isEmpty()) {
                        ItemEntity itemEntity = new ItemEntity(entity.level(), entity.getX(), entity.getY(), entity.getZ(), dropItem);
                        itemEntity.setPickUpDelay(10);
                        entity.level().addFreshEntity(itemEntity);
                    }

                    if (Math.random() > 0.5) {
                        offers.remove(selectedOffer); // 删除交易项
                    }

                    // 判断是否有 “罪恶” 效果，如果有则设置为 等级+1 ，没有则设置为0
                    int amplifier = (sourceEntity.getEffect(ModEffects.SIN) != null) ? Objects.requireNonNull(sourceEntity.getEffect(ModEffects.SIN)).getAmplifier() + 1 : 0;
                    sourceEntity.addEffect(new MobEffectInstance(ModEffects.SIN, 1200, amplifier));
                }
            }
        }
    }

    @SubscribeEvent
    public static void Sin(LivingIncomingDamageEvent event) {
        LivingEntity entity = event.getEntity(); // 获取实体
        if (entity.getEffect(ModEffects.SIN) != null) { // 检查是否有罪恶效果
            int lvl = Objects.requireNonNull(entity.getEffect(ModEffects.SIN)).getAmplifier();
            float amount = event.getAmount();
            if (lvl > 0) {
                event.setAmount(amount + (lvl * 0.2F * amount)); // 根据效果的等级修改伤害值
            }
        }
    }
}
