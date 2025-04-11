package com.hasoook.hasoookmod.event.enchantment;

import com.hasoook.hasoookmod.HasoookMod;
import com.hasoook.hasoookmod.enchantment.ModEnchantmentHelper;
import com.hasoook.hasoookmod.enchantment.ModEnchantments;
import com.hasoook.hasoookmod.entity.ModEntityHelper;
import com.hasoook.hasoookmod.net.ControlInputPacket;
import com.hasoook.hasoookmod.net.LeftClickAirPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.animal.frog.Frog;
import net.minecraft.world.entity.animal.goat.Goat;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.animal.horse.Donkey;
import net.minecraft.world.entity.animal.horse.Llama;
import net.minecraft.world.entity.animal.horse.Mule;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.monster.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.entity.projectile.LlamaSpit;
import net.minecraft.world.entity.projectile.SmallFireball;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EndPortalFrameBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.pattern.BlockPattern;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.*;
import net.minecraft.world.ticks.TickPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.MovementInputUpdateEvent;
import net.neoforged.neoforge.event.entity.ProjectileImpactEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.*;

@EventBusSubscriber(modid = HasoookMod.MOD_ID)
public class MobControlHandler {
    private static final Map<Player, ControlledMobData> CONTROLLED_MOBS = new HashMap<>();
    private static final float BASE_SPEED = 0.25f;
    private static final float SPRINT_MODIFIER = 1.3f;
    private static final float SNEAK_MODIFIER = 0.5f;
    private static final float JUMP_FORCE = 0.42f;

    // 触发连接
    @SubscribeEvent
    public static void onProjectileImpact(ProjectileImpactEvent event) {
        if (!(event.getProjectile() instanceof FishingHook hook)) return;
        if (!(hook.getOwner() instanceof Player player)) return;
        if ((ModEnchantmentHelper.getEnchantmentLevel(ModEnchantments.MIND_CONTROL, player.getMainHandItem())) < 1) return;
        if (!(event.getRayTraceResult() instanceof EntityHitResult entityHit)) return;
        if (!(entityHit.getEntity() instanceof Mob mob)) return;
        if (player.level().isClientSide()) return;

        mob.targetSelector.setControlFlag(Goal.Flag.TARGET, false);
        mob.setTarget(null); // 立即清除当前目标

        mob.getPersistentData().putBoolean("HasookControlled", true);
        CONTROLLED_MOBS.put(player, new ControlledMobData(mob, hook));
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        CONTROLLED_MOBS.entrySet().removeIf(entry -> {
            ControlledMobData data = entry.getValue();
            if (!data.validate()) {
                releaseMob(data);
                return true;
            }
            updateMobMovement(data);
            return false;
        });
    }

    public static void handleControlInput(ServerPlayer player,
                                          boolean forward,
                                          boolean backward,
                                          boolean left,
                                          boolean right,
                                          boolean jump,
                                          boolean sprinting,
                                          boolean sneaking) {
        ControlledMobData data = CONTROLLED_MOBS.get(player);
        if (data == null || !data.validate()) return;

        data.currentInput = new ControlInput(
                forward, backward, left, right, jump, sprinting, sneaking
        );
    }

    private static void updateMobMovement(ControlledMobData data) {
        Vec3 movement = calculateMovementVector(data);
        boolean shouldJump = data.currentInput.jump();
        boolean onGround = data.mob.onGround();

        // 跳跃处理（保持原样）
        if (shouldJump && onGround) {
            movement = new Vec3(movement.x, JUMP_FORCE, movement.z);
            data.mob.resetFallDistance();
            data.mob.setOnGround(false);
        } else if (!onGround) {
            movement = new Vec3(movement.x, data.mob.getDeltaMovement().y, movement.z);
        }

        // 物理效果应用（保持原样）
        data.mob.setDeltaMovement(
                movement.x * 0.9 + data.mob.getDeltaMovement().x * 0.1,
                movement.y * 0.98f,
                movement.z * 0.9 + data.mob.getDeltaMovement().z * 0.1
        );

        // 更新旋转和位置
        data.mob.setYRot(data.player.getYRot());
        data.mob.setXRot(data.player.getXRot());
        data.mob.yHeadRot = data.player.yHeadRot;
        data.mob.hurtMarked = true;

        // 更新鱼钩位置（保持原样）
        data.hook.setPos(
                data.mob.getX() + data.mob.getLookAngle().x * 0.3,
                data.mob.getEyeY() - 0.25,
                data.mob.getZ() + data.mob.getLookAngle().z * 0.3
        );
    }

    private static Vec3 calculateMovementVector(ControlledMobData data) {
        float speed = BASE_SPEED;
        if (data.currentInput.sneaking()) {
            speed *= SNEAK_MODIFIER;
        } else if (data.currentInput.sprinting()) {
            speed *= SPRINT_MODIFIER;
        }

        float yaw = data.player.getYRot();
        float yawRad = yaw * ((float) Math.PI / 180F);

        // 基础方向向量（玩家面朝方向）
        Vec3 lookVec = new Vec3(
                -Math.sin(yawRad),
                0,
                Math.cos(yawRad)
        ).normalize();

        Vec3 right = lookVec.yRot((float) Math.toRadians(-90));
        Vec3 left = lookVec.yRot((float) Math.toRadians(90));

        Vec3 movement = Vec3.ZERO;
        if (data.currentInput.forward()) movement = movement.add(lookVec);
        if (data.currentInput.backward()) movement = movement.subtract(lookVec);
        if (data.currentInput.left()) movement = movement.add(left);
        if (data.currentInput.right()) movement = movement.add(right);

        if (movement.lengthSqr() > 1.0E-7D) { // 避免除以零
            movement = movement.normalize();
        }

        return movement.scale(speed);
    }

    private static void releaseMob(ControlledMobData data) {
        // 恢复生物原始状态
        data.mob.targetSelector.setControlFlag(Goal.Flag.TARGET, true);
        data.mob.targetSelector.tick();
        data.mob.setTarget(null);
        data.mob.getPersistentData().remove("HasookControlled");

        // 移除鱼钩
        if (!data.hook.isRemoved()) {
            data.hook.discard();
        }
    }

    @SubscribeEvent
    public static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        int lvl = ModEnchantmentHelper.getEnchantmentLevel(ModEnchantments.MIND_CONTROL, event.getEntity().getMainHandItem());
        if (lvl > 0 && !event.getLevel().isClientSide && event.getAction() == PlayerInteractEvent.LeftClickBlock.Action.START) {
            handleLeftClick(event.getEntity());
        }
    }

    @SubscribeEvent
    public static void onLeftClickEmpty(PlayerInteractEvent.LeftClickEmpty event) {
        int lvl = ModEnchantmentHelper.getEnchantmentLevel(ModEnchantments.MIND_CONTROL, event.getEntity().getMainHandItem());
        if (lvl > 0 && event.getLevel().isClientSide) {
            PacketDistributor.sendToServer(new LeftClickAirPacket());
        }
    }

    @SubscribeEvent
    public static void onLeftClickEntity(AttackEntityEvent event) {
        int lvl = ModEnchantmentHelper.getEnchantmentLevel(ModEnchantments.MIND_CONTROL, event.getEntity().getMainHandItem());
        if (lvl > 0 && !event.getEntity().level().isClientSide) {
            handleLeftClick(event.getEntity());
            event.setCanceled(true);
        }
    }

    public static void handleLeftClick(Player player) {
        // 获取玩家的控制数据
        ControlledMobData data = CONTROLLED_MOBS.get(player);
        if (data == null || !data.validate()) return;

        // 获取玩家视野中的第一个目标实体
        Entity target = ModEntityHelper.getFirstInSight(data.mob, 5.0);
        LivingEntity entity = data.mob;

        // 处理不同的生物行为
        switch (entity) {
            case Creeper creeper -> {
                handleCreeper(creeper);
                return;
            }
            case Llama llama -> {
                handleLlama(llama);
                return;
            }
            case Blaze blaze -> {
                handleBlaze(blaze);
                return;
            }
            case Frog frog -> {
                handleFrog(frog, target);
                return;
            }
            case Goat goat -> {
                handleGoatCharge(goat);
                return;
            }
            case EnderMan enderman -> {
                handleEnderMan(enderman);
                return;
            }
            case Endermite endermite -> {
                handleEnderMite(endermite, player);
                return;
            }
            default -> {
            }
        }

        if (entity instanceof AbstractHorse || entity instanceof Donkey || entity instanceof Mule || entity instanceof Strider) {
            handleHorseRiding(entity, target);
            return;
        }

        if (entity.getAttribute(Attributes.ATTACK_DAMAGE) == null) {
            return; // 如果生物没有攻击力，直接返回
        }

        if (target != null) {
            // 执行攻击
            entity.setLastHurtMob(target);
            entity.doHurtTarget(target);
            entity.attackStrengthTicker = 0;
        }
        entity.swing(InteractionHand.MAIN_HAND);
    }

    private static void handleCreeper(Creeper creeper) {
        // 点燃苦力怕并启动自爆
        creeper.ignite();
        creeper.setSwellDir(1);
    }

    private static void handleLlama(Llama llama) {
        LlamaSpit spit = new LlamaSpit(llama.level(), llama);

        Vec3 lookAngle = llama.getLookAngle();

        // 计算羊驼嘴巴位置
        Vec3 mouthPos = llama.getEyePosition().add(llama.getLookAngle().scale(0.5)).add(0, -0.2, 0);
        spit.setPos(mouthPos.x, mouthPos.y, mouthPos.z);

        // 发射羊驼的唾液
        spit.shoot(lookAngle.x, lookAngle.y, lookAngle.z, 1.5F, 10.0F);
        llama.level().addFreshEntity(spit);
    }

    private static void handleBlaze(Entity blaze) {
        Vec3 lookAngle = blaze.getLookAngle();

        // 火球生成位置
        Vec3 fireballPos = blaze.getEyePosition().add(lookAngle.scale(0.5)).add(0, -0.2, 0);

        // 创建小火球实体
        SmallFireball fireball = new SmallFireball(
                blaze.level(),
                fireballPos.x,
                fireballPos.y,
                fireballPos.z,
                lookAngle
        );

        fireball.setPos(fireballPos.x, fireballPos.y, fireballPos.z);

        float speed = 1.5F;  // 基础速度
        float inaccuracy = 1.0F; // 偏移
        fireball.shoot(lookAngle.x, lookAngle.y, lookAngle.z, speed, inaccuracy);

        // 将火球加入世界
        blaze.level().addFreshEntity(fireball);
    }

    private static void handleFrog(Frog frog, Entity target) {
        // 伸舌头动画
        frog.setPose(Pose.STANDING);
        frog.setPose(Pose.USING_TONGUE);

        // 如果是创造模式或旁观者，跳过
        if (target instanceof Player player && (player.isCreative() || player.isSpectator())) return;

        // 1/10 的概率触发吞噬逻辑
        if (target == null || Math.random() > 0.1 || !target.isAlive() || target.isInvulnerable()) return;

        // 计算朝向青蛙的向量
        Vec3 frogPos = frog.position();
        Vec3 targetPos = target.position();
        Vec3 direction = frogPos.subtract(targetPos).normalize();

        // 设置速度向量
        target.setDeltaMovement(direction.x * 0.5, direction.y * 0.5 + 0.2, direction.z * 0.5);

        // 如果目标是 LivingEntity 才能执行秒杀
        if (target instanceof LivingEntity livingTarget) {
            // 0.4秒后执行秒杀
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    livingTarget.setHealth(0);  // 目标死亡
                    frog.setHealth(frog.getMaxHealth());  // 将青蛙的生命值回满
                }
            }, 400); // 400毫秒延迟
        }
    }


    private static void handleGoatCharge(Goat goat) {
        // 获取山羊的朝向和速度
        Vec3 direction = goat.getLookAngle(); // 获取山羊的朝向
        Vec3 chargeVelocity = direction.scale(3.0);

        // 设置山羊的速度向前冲锋
        goat.setDeltaMovement(chargeVelocity);

        // 生成冲锋粒子效果
        if (!goat.level().isClientSide) {
            ServerLevel serverLevel = (ServerLevel) goat.level();
            // 在山羊位置生成粒子，沿冲锋方向扩散
            serverLevel.sendParticles(
                    ParticleTypes.CLOUD,
                    goat.getX(), goat.getY() + 1, goat.getZ(),
                    15,
                    direction.x * 0.5,
                    direction.y * 0.5,
                    direction.z * 0.5,
                    0.05
            );
        }

        // 定义冲锋的范围 (可以根据需求调整半径和长度)
        double range = 3.0;  // 冲锋的前方范围，单位是块
        double width = 2.0;  // 检测的宽度，即前方的宽度

        // 获取山羊的位置和朝向，确定冲锋区域
        Vec3 goatPosition = goat.position();
        Vec3 goatLookDirection = goat.getLookAngle();

        // 计算检测区域
        AABB area = new AABB(
                goatPosition.x() - width / 2, goatPosition.y() - width / 2, goatPosition.z() - width / 2,
                goatPosition.x() + width / 2, goatPosition.y() + width / 2, goatPosition.z() + width / 2
        ).move(goatLookDirection.scale(range)); // 通过朝向调整范围

        // 获取区域内的所有生物实体
        List<LivingEntity> entitiesInRange = goat.level().getEntitiesOfClass(LivingEntity.class, area);

        for (LivingEntity entity : entitiesInRange) {
            if (entity != goat) {  // 排除山羊自己
                goat.setLastHurtMob(entity);
                goat.doHurtTarget(entity); // 对目标实体造成伤害

                // 将目标实体击飞
                Vec3 knockbackDirection = entity.position().subtract(goat.position()).normalize(); // 计算击飞方向（从山羊指向目标）
                Vec3 knockbackVelocity = knockbackDirection.scale(1.5); // 施加击飞效果

                // 让目标生物按击飞方向和强度移动
                entity.setDeltaMovement(entity.getDeltaMovement().add(knockbackVelocity.x(), knockbackVelocity.y(), knockbackVelocity.z()));
            }
        }
    }

    private static void handleEnderMan(EnderMan enderman) {
        Level level = enderman.level();

        // 检查是否已经持有方块
        BlockState carriedBlock = enderman.getCarriedBlock();
        if (carriedBlock == null) {
            carriedBlock = Blocks.AIR.defaultBlockState();
        }

        // 仅当末影人持有有效方块时，才会执行扔方块的逻辑
        if (!carriedBlock.isAir()) {
            // 仅在服务端执行实体生成
            if (level instanceof ServerLevel serverLevel) {
                // 获取视线方向
                Vec3 look = enderman.getLookAngle();

                // 计算生成位置（基于末影人眼睛位置）
                Vec3 eyePos = enderman.getEyePosition();
                BlockPos spawnPos = BlockPos.containing(eyePos.x + look.x * 0.5, eyePos.y + look.y * 0.5, eyePos.z + look.z * 0.5);

                // 检查是否为TNT
                if (carriedBlock.is(Blocks.TNT)) {
                    // 创建点燃的TNT实体
                    PrimedTnt tnt = new PrimedTnt(
                            serverLevel,
                            spawnPos.getX() + 0.5,
                            spawnPos.getY() + 0.5,
                            spawnPos.getZ() + 0.5,
                            enderman // 设置引发爆炸的实体
                    );

                    // 设置基础参数
                    tnt.setFuse(80); // 设置80 ticks（4秒）引信
                    tnt.setDeltaMovement(
                            look.x * 0.8 + enderman.getDeltaMovement().x,
                            look.y * 0.8 + 0.2,
                            look.z * 0.8 + enderman.getDeltaMovement().z
                    );

                    // 添加实体到世界
                    serverLevel.addFreshEntity(tnt);

                    // 同步运动数据给客户端
                    serverLevel.getPlayers(player -> player instanceof ServerPlayer)
                            .forEach(player -> {
                                player.connection.send(
                                        new ClientboundSetEntityMotionPacket(tnt)
                                );
                            });
                } else {
                    // 原版下坠方块逻辑
                    FallingBlockEntity fallingBlock = FallingBlockEntity.fall(
                            serverLevel,
                            spawnPos,
                            carriedBlock
                    );

                    // 设置实体参数
                    fallingBlock.setDeltaMovement(
                            look.x * 0.8 + enderman.getDeltaMovement().x,
                            look.y * 0.8 + 0.2,
                            look.z * 0.8 + enderman.getDeltaMovement().z
                    );

                    // 获取方块硬度
                    float hardness = carriedBlock.getBlock().defaultDestroyTime();
                    // 初始化伤害值
                    int baseDamage;
                    int maxDamage;

                    // 根据硬度设置不同伤害策略
                    if (hardness < 0) {
                        baseDamage = 999;
                        maxDamage = 999;
                    } else {
                        baseDamage = (int) Math.max(1, hardness * 5);
                        maxDamage = (int) Math.max(1, hardness * 10);
                    }
                    fallingBlock.setHurtsEntities(baseDamage, maxDamage);
                    fallingBlock.time = 1;

                    // 添加实体到世界
                    serverLevel.addFreshEntity(fallingBlock);

                    // 同步运动数据给客户端
                    serverLevel.getPlayers(player -> player instanceof ServerPlayer)
                            .forEach(player -> {
                                player.connection.send(
                                        new ClientboundSetEntityMotionPacket(fallingBlock)
                                );
                            });
                }

                // 清空末影人手持方块
                enderman.setCarriedBlock(Blocks.AIR.defaultBlockState());
                enderman.swing(InteractionHand.MAIN_HAND);
            }
            return;
        }

        // 原方块拾取逻辑（保持不变）
        Vec3 start = enderman.getEyePosition();
        Vec3 look = enderman.getLookAngle();
        double maxDistance = 6.0;
        Vec3 end = start.add(look.x * maxDistance, look.y * maxDistance, look.z * maxDistance);

        ClipContext context = new ClipContext(
                start,
                end,
                ClipContext.Block.OUTLINE,
                ClipContext.Fluid.NONE,
                enderman
        );

        BlockHitResult hitResult = level.clip(context);

        if (hitResult.getType() == HitResult.Type.BLOCK) {
            BlockPos blockPos = hitResult.getBlockPos();
            BlockState blockState = level.getBlockState(blockPos);

            // 检查是否是末地传送门框架且没有眼睛
            if (blockState.is(Blocks.END_PORTAL_FRAME) && level instanceof ServerLevel serverLevel) {
                // 获取框架状态
                boolean hasEye = blockState.getValue(EndPortalFrameBlock.HAS_EYE);
                if (!hasEye) {
                    // 设置为有眼睛的状态
                    BlockState newState = blockState.setValue(EndPortalFrameBlock.HAS_EYE, true);
                    level.setBlock(blockPos, newState, Block.UPDATE_ALL);

                    // 2. 触发原版结构检测（关键代码）
                    serverLevel.getBlockState(blockPos).handleNeighborChanged(
                            serverLevel,
                            blockPos,
                            Blocks.END_PORTAL_FRAME,
                            blockPos.relative(blockState.getValue(EndPortalFrameBlock.FACING)),
                            false
                    );

                    // 播放放置声音和动画
                    level.playSound(null, blockPos, SoundEvents.END_PORTAL_FRAME_FILL, SoundSource.BLOCKS, 1.0F, 1.0F);
                    enderman.swing(InteractionHand.MAIN_HAND);
                }
                return;
            }

            enderman.setCarriedBlock(blockState);
            level.destroyBlock(blockPos, false, enderman);
        }
    }

    private static void handleEnderMite(Entity entity, Player player) {
        // 保存两者的位置和旋转角度
        double entityX = entity.getX();
        double entityY = entity.getY();
        double entityZ = entity.getZ();

        double playerX = player.getX();
        double playerY = player.getY();
        double playerZ = player.getZ();

        // 交换位置和角度
        entity.teleportTo(playerX, playerY, playerZ);
        player.teleportTo(entityX, entityY, entityZ);

        // 添加传送粒子效果
        if (entity.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(
                    ParticleTypes.PORTAL,
                    entityX, entityY + 0.5, entityZ,
                    20, 0.5, 0.5, 0.5, 0.1
            );
            serverLevel.sendParticles(
                    ParticleTypes.PORTAL,
                    playerX, playerY + 0.5, playerZ,
                    20, 0.5, 0.5, 0.5, 0.1
            );
        }
        player.playSound(SoundEvents.ENDERMAN_TELEPORT, 1.0F, 1.0F);
        entity.playSound(SoundEvents.ENDERMAN_TELEPORT, 1.0F, 1.0F);
    }

    private static void handleHorseRiding(LivingEntity entity, Entity target) {
        if (target instanceof LivingEntity livingTarget) {
            // 找到骑乘链的最顶端
            Entity topEntity = entity;
            while (!topEntity.getPassengers().isEmpty()) {
                topEntity = topEntity.getPassengers().getFirst();
            }

            // 如果目标生物不是当前乘客链的顶端
            if (topEntity != livingTarget) {
                livingTarget.startRiding(topEntity, true); // 骑乘
                entity.level().playSound(null, entity.getX(), entity.getY(), entity.getZ(), SoundEvents.HORSE_SADDLE, SoundSource.NEUTRAL, 1.0F, 1.0F);
            }
        }
    }

    private static class ControlledMobData {
        final Mob mob;
        final FishingHook hook;
        final Player player;
        final boolean originalAggressive;
        final int originalNoActionTime;
        ControlInput currentInput;

        ControlledMobData(Mob mob, FishingHook hook) {
            this.mob = mob;
            this.hook = hook;
            this.player = hook.getPlayerOwner();
            this.originalAggressive = mob.isAggressive();
            this.originalNoActionTime = mob.getNoActionTime();
            this.currentInput = new ControlInput(false, false, false, false, false, false, false);
        }

        boolean validate() {
            return mob != null && hook != null && player != null
                    && mob.isAlive() && hook.isAlive() && !hook.isRemoved()
                    && mob.distanceToSqr(hook) < 225
                    && mob.getPersistentData().getBoolean("HasookControlled");
        }
    }

    private record ControlInput(
            boolean forward,
            boolean backward,
            boolean left,
            boolean right,
            boolean jump,
            boolean sprinting,
            boolean sneaking
    ) {}

    @SubscribeEvent
    public static void onMovementInput(MovementInputUpdateEvent event) {
        Player player = event.getEntity();
        if (player.fishing instanceof FishingHook hook && hook.getHookedIn() != null) {
            ControlInputPacket packet = new ControlInputPacket(
                    event.getInput().up,
                    event.getInput().down,
                    event.getInput().left,
                    event.getInput().right,
                    event.getInput().jumping,
                    player.isSprinting(),
                    player.isCrouching()
            );
            PacketDistributor.sendToServer(packet);
        }
    }
}