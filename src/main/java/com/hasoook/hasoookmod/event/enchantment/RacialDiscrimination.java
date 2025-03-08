package com.hasoook.hasoookmod.event.enchantment;

import com.hasoook.hasoookmod.HasoookMod;
import com.hasoook.hasoookmod.effect.ModEffects;
import com.hasoook.hasoookmod.enchantment.ModEnchantmentHelper;
import com.hasoook.hasoookmod.enchantment.ModEnchantments;
import com.hasoook.hasoookmod.entity.ModEntityHelper;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.Nameable;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.*;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@EventBusSubscriber(modid = HasoookMod.MOD_ID)
public class RacialDiscrimination {
    @SubscribeEvent
    public static void LivingEntityUseItemEvent(LivingEntityUseItemEvent.Stop event) {
        LivingEntity livingEntity = event.getEntity();

        if (!livingEntity.level().isClientSide) {
            // 获取看着的实体
            Entity firstEntityInSight = ModEntityHelper.getFirstInSight(livingEntity, 30);
            ItemStack itemStack = livingEntity.getMainHandItem();
            int racialDiscrimination = ModEnchantmentHelper.getEnchantmentLevel(ModEnchantments.RACIAL_DISCRIMINATION, itemStack);

            if (firstEntityInSight != null && racialDiscrimination > 0 && ModEntityHelper.isWhiteMob(firstEntityInSight)) {
                event.setCanceled(true);
                if (livingEntity instanceof Player player) {
                    player.displayClientMessage(Component.literal("目标不合法！"), false);
                }
            }
        }
    }

    @SubscribeEvent
    public static void LivingEntityUseItemEvent(LivingEntityUseItemEvent.Tick event) {
        LivingEntity livingEntity = event.getEntity();
        Level level = livingEntity.level();

        if (!level.isClientSide) {
            Entity firstEntityInSight = ModEntityHelper.getFirstInSight(livingEntity, 20);
            int duration = event.getDuration();
            ItemStack usedItem = event.getItem();
            int racialDiscrimination = ModEnchantmentHelper.getEnchantmentLevel(ModEnchantments.RACIAL_DISCRIMINATION, usedItem);
            boolean isUsingLongEnough = (72000 - duration) >= 15;

            if (racialDiscrimination > 0 && firstEntityInSight != null && ModEntityHelper.isBlackMob(firstEntityInSight) && isUsingLongEnough) {
                // 检查弹药和无限材料
                if (livingEntity.getProjectile(usedItem).isEmpty() && !livingEntity.hasInfiniteMaterials()) {
                    event.setCanceled(true);
                }

                // 使用实际附魔的弓触发释放逻辑
                if (usedItem.getItem() instanceof BowItem bowItem) {
                    // 保留弓的附魔数据
                    ItemStack enchantedBow = usedItem.copy();

                    // 调用releaseUsing方法并传递剩余时间（影响箭的威力）
                    int useDuration = usedItem.getUseDuration(livingEntity) - duration;
                    bowItem.releaseUsing(enchantedBow, level, livingEntity, useDuration);

                    // 同步客户端动画（可选）
                    if (livingEntity instanceof ServerPlayer serverPlayer) {
                        CriteriaTriggers.USING_ITEM.trigger(serverPlayer, enchantedBow);
                    }
                }
                // 消耗耐久
                usedItem.hurtAndBreak(16, livingEntity, event.getEntity().getUsedItemHand() == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND);
            }
        }
    }

    @SubscribeEvent
    public static void PlayerTickEvent(PlayerTickEvent.Post event){
        Player player = event.getEntity();
        ItemStack itemStack = player.getMainHandItem();
        int racialDiscrimination = ModEnchantmentHelper.getEnchantmentLevel(ModEnchantments.RACIAL_DISCRIMINATION, itemStack);
        Entity firstEntityInSight = ModEntityHelper.getFirstInSight(player, 20);

        if (racialDiscrimination > 0 && firstEntityInSight != null && ModEntityHelper.isBlackMob(firstEntityInSight)) {
            player.attackStrengthTicker = (int) player.getCurrentItemAttackStrengthDelay();
        }
    }

    @SubscribeEvent
    public static void onEntityAttack(LivingIncomingDamageEvent event) {
        LivingEntity entity = event.getEntity();
        // 确保来源实体是 LivingEntity 类型
        if (event.getSource().getEntity() instanceof LivingEntity source && !entity.level().isClientSide) {
            ItemStack attackerMainHandItem = source.getMainHandItem();
            int racialDiscrimination = ModEnchantmentHelper.getEnchantmentLevel(ModEnchantments.RACIAL_DISCRIMINATION, attackerMainHandItem);
            int swap = ModEnchantmentHelper.getEnchantmentLevel(ModEnchantments.SWAP, attackerMainHandItem);
            if (racialDiscrimination > 0) {
                // 如果是黑色生物
                if (ModEntityHelper.isBlackMob(entity)) {
                    entity.invulnerableTime = 0; // 重置无敌帧
                    if (source.getMainHandItem().is(Items.LEAD) && event.getSource().getDirectEntity() == source) {
                        // 判断是否有 “去工作” 效果，如果有则设置为 等级+1 ，没有则设置为0级（0级在游戏里为1级）
                        int goWorkAmplifier = (entity.getEffect(ModEffects.GO_WORK) != null) ? Objects.requireNonNull(entity.getEffect(ModEffects.GO_WORK)).getAmplifier() + 1 : 0;
                        int goWorkTime = (entity.getEffect(ModEffects.GO_WORK) != null) ? Objects.requireNonNull(entity.getEffect(ModEffects.GO_WORK)).getDuration() + 200 : 200;
                        entity.addEffect(new MobEffectInstance(ModEffects.GO_WORK, Math.min(goWorkTime, 500), goWorkAmplifier));

                        if (source instanceof Player player) {
                            if (goWorkAmplifier > 0) {
                                player.displayClientMessage(Component.literal("§c鞭打连击！§lx"  + (goWorkAmplifier + 1)), true);
                            } else {
                                source.sendSystemMessage(Component.nullToEmpty("<" + source.getName().getString() + "> 去工作！"));
                            }
                        }
                    }
                } else if (ModEntityHelper.isWhiteMob(entity) && swap < 1) {
                    event.setCanceled(true);
                    if (source instanceof Player player) {
                        player.displayClientMessage(Component.literal("目标不合法！"), false);
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void RightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getEntity();
        BlockPos pos = event.getPos();
        Level level = event.getLevel();
        BlockEntity blockEntity = level.getBlockEntity(pos);

        if (!level.isClientSide && blockEntity != null && ModEntityHelper.isBlackMob(player)) {
            int lvl = ModEnchantmentHelper.getBlockEnchantmentLevel(ModEnchantments.RACIAL_DISCRIMINATION, blockEntity);
            if (lvl > 0) {
                boolean success = teleportBlock(level, pos, player);
                if (success) {
                    event.setCanceled(true);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        BlockPos pos = event.getPos();
        Player player = event.getPlayer();
        Level level = player.level();
        BlockEntity blockEntity = level.getBlockEntity(pos);

        if (!level.isClientSide && blockEntity != null && ModEntityHelper.isBlackMob(player)) {
            int lvl = ModEnchantmentHelper.getBlockEnchantmentLevel(ModEnchantments.RACIAL_DISCRIMINATION, blockEntity);
            if (lvl > 0) {
                boolean success = teleportBlock(level, pos, player);
                if (success) {
                    event.setCanceled(true);
                }
            }
        }
    }

    private static boolean  teleportBlock(Level level, BlockPos pos, @Nullable Player player) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity == null) return false;

        // 收集原始数据
        BlockState originalState = level.getBlockState(pos);
        DataComponentMap originalComponents = blockEntity.collectComponents();

        // 获取周围的空气方块
        List<BlockPos> airPositions = new ArrayList<>();
        for (int x = -2; x <= 2; x++) {
            for (int y = -2; y <= 2; y++) {
                for (int z = -2; z <= 2; z++) {
                    BlockPos currentPos = pos.offset(x, y, z);
                    if (level.isEmptyBlock(currentPos)) {
                        airPositions.add(currentPos);
                    }
                }
            }
        }

        if (airPositions.isEmpty()) {
            return false;
        }

        BlockPos targetPos = airPositions.get(level.getRandom().nextInt(airPositions.size()));

        // 放置新方块
        level.setBlock(targetPos, originalState, Block.UPDATE_ALL);

        // 复制组件数据
        BlockEntity newBlockEntity = level.getBlockEntity(targetPos);
        if (newBlockEntity != null) {
            ItemStack dummyItem = new ItemStack(originalState.getBlock().asItem());
            dummyItem.applyComponents(originalComponents);
            newBlockEntity.applyComponentsFromItemStack(dummyItem);
            newBlockEntity.setChanged();
        }

        // 清空原方块的存储物品
        if (blockEntity instanceof Container container) {
            for (int slot = 0; slot < container.getContainerSize(); slot++) {
                container.setItem(slot, ItemStack.EMPTY);
            }
            container.setChanged();
        }

        // 移除原方块
        level.removeBlock(pos, false);
        level.removeBlockEntity(pos);
        level.levelEvent(2001, pos, Block.getId(originalState));

        // 粒子效果和音效
        if (level instanceof ServerLevel serverLevel) {
            Vec3 start = Vec3.atCenterOf(pos);
            Vec3 end = Vec3.atCenterOf(targetPos);
            Vec3 direction = end.subtract(start);
            double distance = direction.length();
            direction = direction.normalize();

            int particleCount = (int) (distance * 6);
            for (int i = 0; i < particleCount; i++) {
                double ratio = (double) i / particleCount;
                Vec3 currentPos = start.add(
                        direction.x * distance * ratio,
                        direction.y * distance * ratio,
                        direction.z * distance * ratio
                );
                serverLevel.sendParticles(
                        ParticleTypes.PORTAL,
                        currentPos.x, currentPos.y, currentPos.z,
                        1, 0.1, 0.1, 0.1, 0.02
                );
            }
            serverLevel.sendParticles(ParticleTypes.PORTAL, end.x, end.y, end.z, 10, 0.2, 0.2, 0.2, 0.5);
            level.playSound(null, end.x, end.y, end.z,
                    SoundEvents.ENDERMAN_TELEPORT,
                    player != null ? player.getSoundSource() : SoundSource.BLOCKS,
                    1.0F, 1.0F
            );
        }

        return true;
    }
}
