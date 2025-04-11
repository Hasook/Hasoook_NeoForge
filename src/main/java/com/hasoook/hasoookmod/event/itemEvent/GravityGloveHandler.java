package com.hasoook.hasoookmod.event.itemEvent;

import com.hasoook.hasoookmod.HasoookMod;
import com.hasoook.hasoookmod.enchantment.ModEnchantmentHelper;
import com.hasoook.hasoookmod.item.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.network.protocol.game.ClientboundTeleportEntityPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.*;

@EventBusSubscriber(modid = HasoookMod.MOD_ID)
public class GravityGloveHandler {
    private static final WeakHashMap<Player, Entity> heldEntities = new WeakHashMap<>();
    private static final Map<Entity, ThrowData> thrownEntities = Collections.synchronizedMap(new WeakHashMap<>());

    private static class ThrowData {
        public final Player thrower;
        public final Vec3 initialVelocity;
        public int age;
        public final int powerLevel;  // 力量等级
        public final int punchLevel;  // 冲击等级
        public final int flameLevel;  // 火矢等级

        public ThrowData(Player thrower, Vec3 velocity, int power, int punch, int flame) {
            this.thrower = thrower;
            this.initialVelocity = velocity;
            this.age = 0;
            this.powerLevel = power;
            this.punchLevel = punch;
            this.flameLevel = flame;
        }
    }

    public static Entity getHeldEntity(Player player) {
        return heldEntities.get(player);
    }

    public static void startHolding(Player player, Entity entity) {
        heldEntities.put(player, entity);
        entity.setNoGravity(true);
    }

    public static void stopHolding(Player player) {
        Entity entity = heldEntities.remove(player);
        if (entity != null) {
            entity.setNoGravity(false);

            // 获取附魔等级
            ItemStack gloveStack = findGloveStack(player);
            int power = 0;
            int punch = 0;
            int flame = 0;
            if (gloveStack != null) {
                power = ModEnchantmentHelper.getEnchantmentLevel(Enchantments.POWER, gloveStack);
                punch = ModEnchantmentHelper.getEnchantmentLevel(Enchantments.PUNCH, gloveStack);
                flame = ModEnchantmentHelper.getEnchantmentLevel(Enchantments.FLAME, gloveStack);
            }

            Vec3 velocity = entity.getDeltaMovement();
            thrownEntities.put(entity, new ThrowData(player, velocity, power, punch, flame));

            if (flame > 0) {
                entity.setRemainingFireTicks(100); // 着火
            }

            if (entity instanceof LivingEntity living) {
                living.setJumping(false);
            }
        }
    }

    // 获取玩家当前持有的手套
    private static ItemStack findGloveStack(Player player) {
        ItemStack mainHand = player.getMainHandItem();
        if (mainHand.getItem() == ModItems.GRAVITY_GLOVE.get()) {
            return mainHand;
        }
        ItemStack offHand = player.getOffhandItem();
        if (offHand.getItem() == ModItems.GRAVITY_GLOVE.get()) {
            return offHand;
        }
        return null;
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        Entity heldEntity = heldEntities.get(player);

        if (heldEntity != null) {
            // 检查玩家是否拿着手套而且实体还存在
            if (!isHoldingGlove(player) || !heldEntity.isAlive()) {
                stopHolding(player);
                return;
            }

            Vec3 entityCenter = heldEntity.getBoundingBox().getCenter();
            Vec3 look = player.getLookAngle();
            Vec3 targetCenter = player.getEyePosition().add(look.x * 4, look.y * 4, look.z * 4);
            Vec3 toTarget = targetCenter.subtract(entityCenter);

            // 基础运动参数
            double sizeFactor = heldEntity.getBbWidth() * heldEntity.getBbHeight();
            double baseSpeed = 0.4 + sizeFactor * 0.1;
            double maxSpeed = 2.5;
            double damping = 0.6;

            Vec3 motion = toTarget.normalize().scale(Math.min(toTarget.length() * baseSpeed, maxSpeed));
            double distance = toTarget.length();

            if (distance < 1.0) {
                motion = motion.scale(damping * (0.3 + distance * 0.7));
            }

            // 持续晃动系统
            if (heldEntity.isAlive()) {
                // 使用实体自身的时间基准
                double time = heldEntity.tickCount * 0.15;

                // 双轴相位偏移波形
                double horizontalShake = Math.sin(time * 4.2) * 0.04;
                double verticalShake = Math.cos(time * 3.8 + 1.5) * 0.02; // 相位偏移1.5弧度

                // 动态方向计算（基于当前视角右方）
                float yawRad = (float) Math.toRadians(player.getYRot() + 90);
                Vec3 rightDir = new Vec3(-Math.sin(yawRad), 0, Math.cos(yawRad));

                // 合成晃动向量
                Vec3 shakeVec = rightDir.scale(horizontalShake)
                        .add(0, verticalShake, 0);

                // 强制最低运动量
                if (motion.length() < 0.01) {
                    motion = shakeVec.scale(0.3); // 保留30%基础晃动
                } else {
                    motion = motion.add(shakeVec);
                }
            }

            heldEntity.setDeltaMovement(motion);
            spawnMovingParticles(player, heldEntity);

            if (player.level() instanceof ServerLevel serverLevel) {
                // 同时发送位置和速度数据包
                serverLevel.getPlayers(player1 -> true).forEach(serverPlayer -> {
                    serverPlayer.connection.send(new ClientboundSetEntityMotionPacket(heldEntity));
                    serverPlayer.connection.send(new ClientboundTeleportEntityPacket(heldEntity));
                });
            }
        }
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        synchronized (thrownEntities) {
            Iterator<Map.Entry<Entity, ThrowData>> it = thrownEntities.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<Entity, ThrowData> entry = it.next();
                Entity entity = entry.getKey();
                ThrowData data = entry.getValue();

                if (entity == null || !entity.isAlive() || data.age++ > 200) {
                    it.remove();
                    continue;
                }

                if (data.age > 1) {
                    Vec3 motion = entity.getDeltaMovement();
                    entity.setDeltaMovement(motion.scale(0.99));
                }

                // 粒子效果
                if (entity.level() instanceof ServerLevel serverLevel) {
                    spawnTrailParticles(serverLevel, entity, data);
                }

                checkCollisions(entity, data);

                // 如果速度过小就移除属性
                if (entity.getDeltaMovement().length() < 0.2) {
                    it.remove();
                }
            }
        }
    }

    private static void checkCollisions(Entity projectile, ThrowData data) {
        // 扩大碰撞检测范围
        List<Entity> entities = projectile.level().getEntities(null, projectile.getBoundingBox().inflate(0.5));

        Set<Entity> attackedEntities = new HashSet<>();

        for (Entity target : entities) {
            if (target == data.thrower || target == projectile || !target.isAlive() || attackedEntities.contains(target)) {
                continue;
            }

            BlockState blockState = null;
            float hardness = 0;
            if (projectile instanceof FallingBlockEntity fallingBlock) {
                blockState = fallingBlock.getBlockState();
                hardness = blockState.getDestroySpeed(projectile.level(), BlockPos.containing(projectile.position()));
            }

            // 使用初始速度 + 当前速度的综合速度计算伤害
            Vec3 effectiveVelocity = projectile.getDeltaMovement().add(data.initialVelocity.scale(0.2));
            double speed = effectiveVelocity.length();

            // 伤害计算公式：基础伤害 + 速度（受力量影响） + 方块硬度
            float damage = (float) (5 + (speed * (2.0 + data.powerLevel * 0.5) + Math.max(hardness, 0) * 0.5f));

            if (data.flameLevel > 0) {
                target.setRemainingFireTicks(40); // 着火
            }

            if (target.hurt(projectile.damageSources().indirectMagic(projectile, data.thrower), damage)) {
                // 击退效果
                double punchMultiplier = (speed + data.punchLevel) * 0.5;
                Vec3 knockback = effectiveVelocity.normalize().scale(punchMultiplier).add(0, 0.1 * punchMultiplier, 0);
                target.push(knockback.x, knockback.y, knockback.z);
                attackedEntities.add(target);

                // 命中后速度衰减
                Vec3 newMotion = projectile.getDeltaMovement().scale(0.8);
                projectile.setDeltaMovement(newMotion);
                projectile.hurt(projectile.damageSources().indirectMagic(projectile, data.thrower), damage);

                Random random = new Random();
                // 破裂概率计算公式
                float baseChance = 0.2f;                  // 基础20%概率
                float speedFactor = (float) (speed * 0.1f);  // 速度每增加1单位+20%
                float hardnessFactor = hardness * 0.05f;   // 每点硬度减少的概率

                // 最终概率 = (基础概率 + 速度加成) * (1 - 硬度衰减)
                float breakChance = (baseChance + speedFactor) * (1 - Math.min(hardnessFactor, 0.6f));
                breakChance = Math.max(breakChance, 0); // 确保不为负数

                if (blockState != null && random.nextFloat() <= breakChance) {
                    spawnBreakEffects(projectile.level(), projectile.position(), blockState);

                    // 移除方块实体
                    projectile.discard();

                    break;
                }
            }
        }
    }

    private static boolean isHoldingGlove(Player player) {
        ItemStack mainHand = player.getMainHandItem();
        ItemStack offHand = player.getOffhandItem();
        return mainHand.getItem() == ModItems.GRAVITY_GLOVE.get() ||
                offHand.getItem() == ModItems.GRAVITY_GLOVE.get();
    }

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        stopHolding(event.getEntity());
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        stopHolding(event.getEntity());
    }

    private static void spawnMovingParticles(Player player, Entity entity) {
        Level level = player.level();
        if (level.isClientSide()) {
            // 获取实体包围盒
            AABB bb = entity.getBoundingBox();

            // 粒子参数配置
            int particlesPerTick = (int) Math.max(1, entity.getBbHeight());
            double spreadStrength = 0.25; // 扩散强度
            double verticalSpread = 0.3; // 垂直扩散比例

            for(int i = 0; i < particlesPerTick; i++) {
                // 在实体范围内随机生成位置
                double x = bb.minX + (bb.maxX - bb.minX) * level.random.nextDouble();
                double y = bb.minY + (bb.maxY - bb.minY) * level.random.nextDouble();
                double z = bb.minZ + (bb.maxZ - bb.minZ) * level.random.nextDouble();

                // 三维球面扩散算法
                double theta = level.random.nextDouble() * 2 * Math.PI;
                double phi = level.random.nextDouble() * Math.PI;
                double r = spreadStrength * level.random.nextDouble();

                double motionX = r * Math.sin(phi) * Math.cos(theta);
                double motionY = r * Math.cos(phi) * verticalSpread;
                double motionZ = r * Math.sin(phi) * Math.sin(theta);

                level.addParticle(
                        ParticleTypes.REVERSE_PORTAL,
                        true, // 长距离可见
                        x, y, z,
                        motionX,
                        motionY,
                        motionZ
                );
            }
        }
    }

    private static void spawnTrailParticles(ServerLevel level, Entity entity, ThrowData data) {
        Vec3 pos = entity.position();
        level.sendParticles(
                ParticleTypes.REVERSE_PORTAL,
                pos.x,
                pos.y + entity.getBbHeight() / 2,
                pos.z,
                2, // 数量
                0.2, 0.2, 0.2, // 随机偏移
                0.02 // 速度
        );
        if (data.flameLevel > 0) {
            level.sendParticles(
                    ParticleTypes.FLAME,
                    pos.x,
                    pos.y + entity.getBbHeight() / 2,
                    pos.z,
                    1, // 数量
                    0.5, 0.5, 0.5, // 随机偏移
                    0.02 // 速度
            );
        }
    }

    // 方块破碎效果
    private static void spawnBreakEffects(Level level, Vec3 pos, BlockState state) {
        if (state.isAir()) return;

        // 播放方块破碎音效
        level.playSound(null,
                pos.x, pos.y, pos.z,
                state.getSoundType().getBreakSound(),
                SoundSource.BLOCKS,
                0.7F,
                0.9F + level.random.nextFloat() * 0.2F
        );

        // 生成破碎粒子
        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(
                    new BlockParticleOption(ParticleTypes.BLOCK, state),
                    pos.x, pos.y, pos.z,
                    25,
                    0.3, 0.3, 0.3,
                    0.1
            );
        }
    }
}
