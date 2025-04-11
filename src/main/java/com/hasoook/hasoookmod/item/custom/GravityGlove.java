package com.hasoook.hasoookmod.item.custom;

import com.hasoook.hasoookmod.enchantment.ModEnchantmentHelper;
import com.hasoook.hasoookmod.enchantment.ModEnchantments;
import com.hasoook.hasoookmod.entity.ModEntities;
import com.hasoook.hasoookmod.entity.ModEntityHelper;
import com.hasoook.hasoookmod.entity.custom.MeteoriteEntity;
import com.hasoook.hasoookmod.event.itemEvent.GravityGloveHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundTeleportEntityPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public class GravityGlove extends Item {
    private static final int THROW_THRESHOLD = 5;  // 最小有效蓄力时间
    private static final int MAX_CHARGE_TIME = 180; // 最大有效蓄力时间
    private static final int METEORITE_MAX_CHARGE_TIME = 300; // 陨石蓄力时间
    private static final float BASE_FORCE = 1f;  // 基础投掷力度
    private static final float MAX_FORCE = 20.0f;   // 最大投掷力度

    public GravityGlove(Properties pProperties) {
        super(pProperties);
    }

    // 物品属性
    public static ItemAttributeModifiers createAttributes() {
        return ItemAttributeModifiers.builder()
                // 攻击伤害
                .add(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_ID, 4.0, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                // 攻击速度
                .add(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_ID, -2.4F, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                .build();
    }

    @Override
    public @NotNull InteractionResult interactLivingEntity(@NotNull ItemStack stack, Player player, @NotNull LivingEntity entity, InteractionHand hand) {
        if (GravityGloveHandler.getHeldEntity(player) != null) {
            return InteractionResult.PASS;
        }

        if (!player.level().isClientSide()) {
            GravityGloveHandler.startHolding(player, entity);
            stack.hurtAndBreak(1, player, hand == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND);
        }
        return InteractionResult.sidedSuccess(player.level().isClientSide());
    }

    // 使用
    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(Level level, Player player, @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (hand == InteractionHand.OFF_HAND) {
            // 如果主手已经在蓄力，则副手直接返回pass
            if (player.getMainHandItem().getItem() instanceof GravityGlove &&
                    player.getUseItemRemainingTicks() > 0) {
                return InteractionResultHolder.pass(stack);
            }
        }

        if (level.isClientSide) return InteractionResultHolder.pass(stack);
        if (GravityGloveHandler.getHeldEntity(player) != null) {
            player.startUsingItem(hand);
            stack.hurtAndBreak(1, player, hand == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND);
            return InteractionResultHolder.consume(stack);
        }

        // 如果有隔空取物附魔，就获取玩家视线中的第一个实体
        Entity entity = null;
        int telekinesisLvl = ModEnchantmentHelper.getEnchantmentLevel(ModEnchantments.TELEKINESIS, stack);
        if (telekinesisLvl > 0) {
            entity = ModEntityHelper.getFirstInSight(player, 5 + telekinesisLvl * 3);
        }

        if (entity != null) {
            if (entity instanceof ItemEntity itemEntity) {
                // 计算玩家到掉落物的方向向量
                Vec3 playerPos = player.position()
                        .add(0, player.getEyeHeight(), 0); // 以玩家眼睛位置为终点
                Vec3 itemPos = itemEntity.position();
                Vec3 direction = playerPos.subtract(itemPos).normalize();

                // 根据附魔等级设置移动速度
                double speed = 0.5 + telekinesisLvl * 0.2; // 基础速度 0.5，每级增加 0.2
                itemEntity.setDeltaMovement(direction.scale(speed));

                // 发送速度网络包
                if (level instanceof ServerLevel serverLevel) {
                    // 将实体的速度发送到所有玩家
                    serverLevel.getPlayers(player1 -> player1 instanceof ServerPlayer).forEach(player1 -> {
                        Packet<?> packet = new ClientboundSetEntityMotionPacket(itemEntity);
                        (player1).connection.send(packet);
                    });
                }
            } else {
                GravityGloveHandler.startHolding(player, entity);
                player.startUsingItem(hand);
            }

            stack.hurtAndBreak(1, player, hand == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND);

            return InteractionResultHolder.consume(stack);
        } else {
            Vec3 lookVec = player.getLookAngle();
            int mainHandDurability = ModEnchantmentHelper.getEnchantmentLevel(ModEnchantments.TELEKINESIS, player.getMainHandItem());
            int offHandDurability = ModEnchantmentHelper.getEnchantmentLevel(ModEnchantments.TELEKINESIS, player.getOffhandItem());
            if (mainHandDurability > 2 && offHandDurability > 2 && lookVec.y > 0.7) {
                player.startUsingItem(hand);
                return InteractionResultHolder.consume(stack);
            }
        }
        return InteractionResultHolder.pass(stack);
    }

    // 长按
    @Override
    public void onUseTick(Level level, LivingEntity entity, ItemStack stack, int remainingTicks) {
        if (!(entity instanceof Player player)) return;

        Entity heldEntity = GravityGloveHandler.getHeldEntity(player);
        int useDuration = this.getUseDuration(stack, entity) - remainingTicks;

        // 普通蓄力（投掷实体）
        if (heldEntity != null) {
            return;
        }

        // 召唤陨石蓄力（需双持隔空取物附魔且抬头）
        int mainHandEnchant = ModEnchantmentHelper.getEnchantmentLevel(ModEnchantments.TELEKINESIS, player.getMainHandItem());
        int offHandEnchant = ModEnchantmentHelper.getEnchantmentLevel(ModEnchantments.TELEKINESIS, player.getOffhandItem());
        Vec3 lookVec = player.getLookAngle();

        boolean isMeteorMode = mainHandEnchant > 2 && offHandEnchant > 2 && lookVec.y > 0.7;
        if (!isMeteorMode && !level.isClientSide) {
            // 不满足蓄力条件时，直接中断蓄力
            player.stopUsingItem();
            player.displayClientMessage(Component.literal(""), true);
            return;
        }

        // 是陨石蓄力（显示蓄力条）
        float progress = Math.min((float) useDuration / METEORITE_MAX_CHARGE_TIME, 1.0f);
        player.displayClientMessage(createChargeBar(progress), true);

        // 粒子效果
        summonMeteoriteParticles(level, entity, useDuration);

        // 蓄力完成召唤陨石
        if (useDuration >= METEORITE_MAX_CHARGE_TIME && !level.isClientSide) {
            ServerLevel serverLevel = (ServerLevel) level;
            player.getMainHandItem().hurtAndBreak(200, player, EquipmentSlot.MAINHAND);
            player.getOffhandItem().hurtAndBreak(200, player, EquipmentSlot.OFFHAND);

            MeteoriteEntity meteor = new MeteoriteEntity(ModEntities.METEORITE.get(), serverLevel);
            meteor.setPos(player.position().add(lookVec.scale(50)).add(0, 321, 0));
            meteor.setSize(5);
            serverLevel.addFreshEntity(meteor);

            player.stopUsingItem();
            player.getCooldowns().addCooldown(stack.getItem(), 400);
            player.displayClientMessage(Component.literal(""), true); // 清除蓄力条

            // 音效和提示
            serverLevel.playSound(
                    null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.ELDER_GUARDIAN_CURSE, SoundSource.PLAYERS, 1.5F, 0.8F + player.getRandom().nextFloat() * 0.4F
            );

            if (player instanceof ServerPlayer serverPlayer) {
                serverPlayer.connection.send(new ClientboundSetTitleTextPacket(
                        Component.translatable("hasoook.message.gravity_glove.meteor_summoned")
                                .withStyle(ChatFormatting.RED, ChatFormatting.BOLD)
                ));
            }
        }
    }

    // 松开右键时的处理
    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeLeft) {
        if (entity instanceof Player player && !level.isClientSide()) {
            Entity heldEntity = GravityGloveHandler.getHeldEntity(player);

            if (heldEntity != null) {
                int useDuration = this.getUseDuration(stack, entity) - timeLeft;

                if (useDuration > THROW_THRESHOLD) {
                    int effectiveTime = Math.max(0, useDuration - THROW_THRESHOLD);
                    float power = calculateThrowPower(effectiveTime);

                    if (heldEntity instanceof FallingBlockEntity fallingBlock &&
                            fallingBlock.getBlockState().is(Blocks.TNT) &&
                            ModEnchantmentHelper.getEnchantmentLevel(Enchantments.FLAME, stack) > 0) {

                        // 移除原实体
                        heldEntity.discard();

                        // 创建即将爆炸的TNT实体
                        PrimedTnt primedTnt = new PrimedTnt(level, heldEntity.getX(), heldEntity.getY(), heldEntity.getZ(), player);

                        // 添加到世界
                        level.addFreshEntity(primedTnt);

                        heldEntity = primedTnt;
                    }

                    // 设置方向速度
                    Vec3 look = player.getLookAngle().scale(power);
                    heldEntity.setDeltaMovement(look);
                    heldEntity.hurtMarked = true;

                    if (player.level() instanceof ServerLevel serverLevel) {
                        // 同时发送位置和速度数据包
                        Entity finalHeldEntity = heldEntity;
                        serverLevel.getPlayers(player1 -> true).forEach(serverPlayer -> {
                            serverPlayer.connection.send(new ClientboundSetEntityMotionPacket(finalHeldEntity));
                            serverPlayer.connection.send(new ClientboundTeleportEntityPacket(finalHeldEntity));
                        });
                    }

                    // 释放
                    GravityGloveHandler.stopHolding(player);
                }
            }
        }
    }

    @Override
    public @NotNull InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        BlockPos pos = context.getClickedPos();

        if (player == null || level.isClientSide) return InteractionResult.PASS;

        if (GravityGloveHandler.getHeldEntity(player) == null) {
            BlockState state = level.getBlockState(pos);
            if (state.isAir()) return InteractionResult.PASS;

            if (level.removeBlock(pos, false)) {
                FallingBlockEntity fallingBlock = FallingBlockEntity.fall(level, pos, state);
                fallingBlock.setNoGravity(true); // 禁用重力
                fallingBlock.setHurtsEntities(1.0F, 20);
                level.addFreshEntity(fallingBlock);

                GravityGloveHandler.startHolding(player, fallingBlock);
                player.swing(context.getHand());
                player.startUsingItem(context.getHand()); // 进入蓄力状态
                return InteractionResult.SUCCESS;
            }
        }

        return InteractionResult.PASS;
    }

    // 生成蓄力条文字
    private Component createChargeBar(float progress) {
        int totalSegments = 10; // 总共有10个方块
        int filledSegments = (int) (totalSegments * progress);
        // 绿色方块部分
        String filled = "█".repeat(filledSegments)
                .replaceAll("█", "§c█");
        // 灰色方块部分
        String empty = "█".repeat(totalSegments - filledSegments)
                .replaceAll("█", "§7█");
        return Component.literal(filled + empty);
    }

    public void summonMeteoriteParticles(Level level, LivingEntity entity, int useDuration) {
        Player player = (Player) entity;
        Vec3 playerPos = player.position().add(0, 1.5, 0);

        float chargeProgress = Math.min((float) useDuration / METEORITE_MAX_CHARGE_TIME, 1.0f);

        // 天际起始高度（随着蓄力逐渐降低）
        double startHeight = 320 - (chargeProgress * 100); // 从y=320开始下降到y=40

        Vec3 skyStart = new Vec3(playerPos.x, startHeight + 20, playerPos.z);
        Vec3 toPlayer = playerPos.subtract(skyStart).scale(0.02); // 运动方向向量

        // 牵引光束
        for(int i=0; i<15; i++){
            double ratio = i / 15.0;
            Vec3 pos = skyStart.add(
                    (playerPos.x - skyStart.x) * ratio,
                    (playerPos.y - skyStart.y) * ratio,
                    (playerPos.z - skyStart.z) * ratio
            );

            level.addParticle(
                    ParticleTypes.REVERSE_PORTAL,
                    true,
                    pos.x + (level.random.nextDouble()-0.5)*2,
                    pos.y,
                    pos.z + (level.random.nextDouble()-0.5)*2,
                    toPlayer.x * 3,
                    toPlayer.y * 0.5,
                    toPlayer.z * 3
            );
        }
    }

    private float calculateThrowPower(int effectiveChargeTime) {
        float progress = Mth.clamp(
                (float) effectiveChargeTime / (MAX_CHARGE_TIME - THROW_THRESHOLD),
                0.0F,
                1.0F
        );
        return BASE_FORCE + (MAX_FORCE - BASE_FORCE) * progress * progress; // 平方曲线
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        return true;
    }

    @Override
    public void postHurtEnemy(ItemStack pStack, LivingEntity pTarget, LivingEntity pAttacker) {
        pStack.hurtAndBreak(1, pAttacker, EquipmentSlot.MAINHAND); // 每次攻击消耗1耐久
    }

    @Override
    public boolean isValidRepairItem(ItemStack pStack, ItemStack pRepairCandidate) {
        return pRepairCandidate.is(Items.DIAMOND); // 修复材料
    }

    @Override
    public int getEnchantmentValue() {
        return 20; // 附魔能力
    }

    @Override
    public int getUseDuration(ItemStack pStack, LivingEntity pEntity) {
        return 72000;
    }

    @Override
    public @NotNull UseAnim getUseAnimation(@NotNull ItemStack stack) {
        return UseAnim.BOW;
    }
}
