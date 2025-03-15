package com.hasoook.hasoookmod.event.enchantment;

import com.hasoook.hasoookmod.HasoookMod;
import com.hasoook.hasoookmod.entity.ModEntityHelper;
import com.hasoook.hasoookmod.net.ControlInputPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.animal.frog.Frog;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.animal.horse.Donkey;
import net.minecraft.world.entity.animal.horse.Llama;
import net.minecraft.world.entity.animal.horse.Mule;
import net.minecraft.world.entity.monster.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.entity.projectile.LlamaSpit;
import net.minecraft.world.entity.projectile.SmallFireball;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.MovementInputUpdateEvent;
import net.neoforged.neoforge.event.entity.ProjectileImpactEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.HashMap;
import java.util.Map;

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
        if (!(event.getRayTraceResult() instanceof EntityHitResult entityHit)) return;
        if (!(entityHit.getEntity() instanceof Mob mob)) return;
        if (!(hook.getOwner() instanceof Player player)) return;
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
        handleLeftClick(event.getEntity(), event);
    }

    @SubscribeEvent
    public static void onLeftClickEmpty(PlayerInteractEvent.LeftClickEmpty event) {
        handleLeftClick(event.getEntity(), event);
    }

    private static void handleLeftClick(Player player, PlayerInteractEvent event) {
        ControlledMobData data = CONTROLLED_MOBS.get(player);
        if (data == null || !data.validate()) return;

        Entity target = ModEntityHelper.getFirstInSight(data.mob, 5.0);

        // 特殊处理苦力怕
        if (data.mob instanceof Creeper creeper) {
            // 点燃苦力怕启动自爆
            creeper.ignite();
            creeper.setSwellDir(1);
            return;
        }

        // 特殊处理羊驼
        if (data.mob instanceof Llama llama) {
            LlamaSpit spit = new LlamaSpit(llama.level(), llama);

            float yawRad = llama.getYRot() * ((float) Math.PI / 180F);
            float pitchRad = llama.getXRot() * ((float) Math.PI / 180F);

            double x = -Math.sin(yawRad) * Math.cos(pitchRad);
            double y = -Math.sin(pitchRad);
            double z = Math.cos(yawRad) * Math.cos(pitchRad);

            Vec3 mouthPos = llama.getEyePosition().add(llama.getLookAngle().scale(0.5)).add(0, -0.2, 0);
            spit.setPos(mouthPos.x, mouthPos.y, mouthPos.z);

            spit.shoot(x, y, z, 1.5F, 10.0F);
            llama.level().addFreshEntity(spit);
            return;
        }

        // 特殊处理烈焰人（新增部分）
        if (data.mob instanceof Blaze blaze) {
            Vec3 lookAngle = blaze.getLookAngle();

            // 火球生成位置
            Vec3 fireballPos = blaze.getEyePosition().add(blaze.getLookAngle().scale(0.5)).add(0, -0.2, 0);

            // 创建小火球实体
            SmallFireball fireball = new SmallFireball(
                    blaze.level(),
                    fireballPos.x,
                    fireballPos.y,
                    fireballPos.z,
                    lookAngle
            );

            fireball.setPos(fireballPos.x, fireballPos.y, fireballPos.z);

            // 设置火球速度与随机偏移
            float speed = 1.5F;   // 基础速度
            float inaccuracy = 1.0F; // 精准度（值越小越准）
            fireball.shoot(lookAngle.x, lookAngle.y, lookAngle.z, speed, inaccuracy);

            // 将火球加入世界
            blaze.level().addFreshEntity(fireball);
            return;
        }

        // 青蛙吞噬逻辑
        if (data.mob instanceof Frog frog) {
            frog.setPose(Pose.STANDING);
            frog.setPose(Pose.USING_TONGUE);
        }

        if (data.mob instanceof EnderMan enderman) {
            if (enderman.getCarriedBlock() != null) {
                BlockHitResult placeHit = enderman.level().clip(new ClipContext(
                        enderman.getEyePosition(1.0F),
                        enderman.getEyePosition(1.0F).add(enderman.getViewVector(1.0F).scale(8)),
                        ClipContext.Block.OUTLINE,
                        ClipContext.Fluid.NONE,
                        enderman
                ));

                if (placeHit.getType() == HitResult.Type.BLOCK) {
                    BlockPos placePos = placeHit.getBlockPos().relative(placeHit.getDirection());
                    BlockState carried = enderman.getCarriedBlock();

                    // 严格检查放置条件：目标位置为空且末影人可以放置
                    if (enderman.level().getBlockState(placePos).isAir()) {
                        // 放置方块并清空手持
                        enderman.level().setBlock(placePos, carried, 3);
                        enderman.setCarriedBlock(null);
                        return;
                    }
                }
            } else {
                BlockHitResult hit = enderman.level().clip(new ClipContext(
                        enderman.getEyePosition(1.0F),
                        enderman.getEyePosition(1.0F).add(enderman.getViewVector(1.0F).scale(8)),
                        ClipContext.Block.OUTLINE,
                        ClipContext.Fluid.NONE,
                        enderman
                ));

                if (hit.getType() == HitResult.Type.BLOCK) {
                    BlockPos pos = hit.getBlockPos();
                    BlockState state = enderman.level().getBlockState(pos);

                    // 强制手动移除原方块（确保原版机制失效时也能工作）
                    if (!enderman.level().isClientSide) {
                        enderman.setCarriedBlock(state);
                        enderman.level().removeBlock(pos, false); // 手动移除
                        return;
                    }
                }
            }
        }

        // 新增：处理马的骑乘逻辑
        if ((data.mob instanceof AbstractHorse || data.mob instanceof Donkey || data.mob instanceof Mule || data.mob instanceof Strider) && target != null) {
            if (target instanceof LivingEntity livingTarget) {
                // 找到骑乘链的最顶端
                Entity topEntity = data.mob;
                while (!topEntity.getPassengers().isEmpty()) {
                    topEntity = topEntity.getPassengers().getFirst();
                }

                // 如果目标生物不是当前乘客链的顶端
                if (topEntity != livingTarget) {
                    livingTarget.startRiding(topEntity, true); // 骑乘
                    data.mob.level().playSound(null, data.mob.getX(), data.mob.getY(), data.mob.getZ(), SoundEvents.HORSE_SADDLE, SoundSource.NEUTRAL, 1.0F, 1.0F);
                }
                return;
            }
        }

        if (target instanceof LivingEntity livingTarget) {
            // 新增属性检查
            if (data.mob.getAttribute(Attributes.ATTACK_DAMAGE) == null) {
                return; // 该生物没有攻击能力
            }

            data.mob.setLastHurtMob(livingTarget);
            data.mob.doHurtTarget(livingTarget);
            data.mob.attackStrengthTicker = 0;
            data.mob.swing(InteractionHand.MAIN_HAND);
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