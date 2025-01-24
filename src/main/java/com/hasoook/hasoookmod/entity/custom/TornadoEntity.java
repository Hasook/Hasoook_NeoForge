package com.hasoook.hasoookmod.entity.custom;

import com.hasoook.hasoookmod.entity.ModEntities;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

public class TornadoEntity extends Animal {
    public final AnimationState idleAnimationState = new AnimationState();
    private int idleAnimationTimeout = 0;
    private int age = 0;

    public TornadoEntity(EntityType<? extends Animal> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.1));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createLivingAttributes()
                .add(Attributes.MAX_HEALTH, 10d)
                .add(Attributes.MOVEMENT_SPEED, 0.25D)
                .add(Attributes.FOLLOW_RANGE, 24D);
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(@NotNull ServerLevel level, @NotNull AgeableMob otherParent) {
        return ModEntities.TORNADO.get().create(level);
    }

    private void setupAnimationStates() {
        if(this.idleAnimationTimeout <= 0) {
            this.idleAnimationTimeout = 80;
            this.idleAnimationState.start(this.tickCount);
        } else {
            --this.idleAnimationTimeout;
        }
    }

    @Override
    public boolean isFood(@NotNull ItemStack pStack) {
        return false;
    }

    @Override
    public void tick() {
        this.age++;
        this.noPhysics = true;
        this.setNoGravity(true);
        this.setInvulnerable(true);
        super.tick();
        if(this.level().isClientSide()) {
            this.setupAnimationStates();
        }
        this.emitCloudParticles();
        this.emitGroundParticles(1 + this.getRandom().nextInt(1)); // 粒子效果
        this.attractEntities();
        this.tornadoFit();

        if (this.age > 100 && !this.level().isClientSide) {
            this.discard();
        }
    }

    public void emitGroundParticles(int pCount) {
        if (!this.isPassenger()) {
            Vec3 vec3 = this.getBoundingBox().getCenter();
            Vec3 vec31 = new Vec3(vec3.x, this.position().y, vec3.z);
            BlockState blockstate = !this.getInBlockState().isAir() ? this.getInBlockState() : this.getBlockStateOn();
            if (blockstate.getRenderShape() != RenderShape.INVISIBLE) {
                for (int i = 0; i < pCount; i++) {
                    this.level().addParticle(new BlockParticleOption(ParticleTypes.BLOCK, blockstate), vec31.x, vec31.y, vec31.z, 0.0, 0.0, 0.0);
                }
            }
        }
    }

    public void emitCloudParticles() {
        float scale = this.getScale();
        int bound = Math.max(1, (int) (16 - scale));
        int particleCount = Math.max(1, (int) (scale * 0.5));  // 根据尺寸计算粒子数量

        for (int i = 0; i < particleCount; i++) {
            if (this.random.nextInt(bound) == 0) {
                // 随机位置生成，根据尺寸计算
                Vec3 randomOffset = new Vec3(
                        (this.random.nextFloat() - 0.5) * scale * 2.5,
                        this.random.nextFloat() * 2.5 * scale,
                        (this.random.nextFloat() - 0.5) * scale * 2.5
                );

                // 粒子初始位置为实体位置 + 随机偏移
                Vec3 particlePosition = this.position().add(randomOffset);

                // 计算粒子与实体之间的距离
                double distance = this.position().distanceTo(particlePosition);

                // 计算粒子需要向哪个方向移动，以便向实体位置移动
                Vec3 moveDirection = this.position().subtract(particlePosition).normalize().add(0,0.5,0);  // 计算指向实体位置的方向

                // 根据距离调整移动速度，距离越远，移动越快
                double speedFactor = distance * 0.05;  // 距离越远，speedFactor 越大，速度越快

                // 生成粒子并向实体位置移动，移动速度与距离成正比
                this.level().addParticle(
                        ParticleTypes.CLOUD,
                        particlePosition.x,  // 粒子的 X 坐标
                        particlePosition.y,  // 粒子的 Y 坐标
                        particlePosition.z,  // 粒子的 Z 坐标
                        moveDirection.x * speedFactor,  // X 方向的偏移，粒子向实体方向移动
                        moveDirection.y * speedFactor,  // Y 方向的偏移，粒子向实体方向移动
                        moveDirection.z * speedFactor   // Z 方向的偏移，粒子向实体方向移动
                );
            }
        }
    }

    private void attractEntities() {
        Vec3 tornadoPosition = this.position().add(0, this.getBbHeight() * 2, 0); // 获取龙卷风当前位置
        float scale = this.getScale(); // 获取龙卷风尺寸

        // 查找范围内的实体
        double radius = 4.0 * scale;  // 设置吸引范围
        List<Entity> entities = this.level().getEntities(this, new AABB(tornadoPosition.subtract(radius, radius, radius), tornadoPosition.add(radius, radius, radius)));

        for (Entity entity : entities) {
            // 不吸引创造模式的玩家
            if (entity instanceof Player && ((Player) entity).isCreative() || entity instanceof TornadoEntity) {
                continue;
            }

            // 计算吸引力：计算当前位置与目标生物的向量差
            Vec3 direction = tornadoPosition.subtract(entity.position()).normalize();
            double distance = tornadoPosition.distanceTo(entity.position());

            // 吸引力度：随着距离增加逐渐减小
            double attractionStrength = Math.min(1.0, 10.0 * scale / (distance * distance));  // 根据距离调整吸引力度

            // 向目标生物施加力
            entity.setDeltaMovement(entity.getDeltaMovement().add(direction.scale(attractionStrength * 0.1)));  // 调整吸引力的强度
        }
    }

    private void tornadoFit() {
        Vec3 tornadoPosition = this.position(); // 获取龙卷风当前位置
        float scale = this.getScale(); // 获取龙卷风尺寸

        // 查找范围内的实体
        double radius = 0.5 + scale * 0.5;  // 设置吸引范围
        List<Entity> entities = this.level().getEntities(this, new AABB(tornadoPosition.subtract(radius, radius, radius), tornadoPosition.add(radius, radius, radius)));

        for (Entity entity : entities) {
            int fissionLvl = entity.getPersistentData().getInt("fission");
            if (entity instanceof TornadoEntity || fissionLvl > 0) {
                entity.discard();

                AttributeInstance scaleAttr = Objects.requireNonNull(this.getAttribute(Attributes.SCALE));
                scaleAttr.removeModifiers();
                scaleAttr.setBaseValue(scale + 1);

                this.level().addParticle(
                        ParticleTypes.GUST,
                        this.getX(),
                        this.getY(),
                        this.getZ(),
                        0,
                        0,
                        0
                );
                this.age = 0; // 重置时间
            }
        }
    }

    @Override
    public boolean hurt(DamageSource pSource, float pAmount) {
        String source = pSource.getMsgId(); // 获取伤害类型
        if (source.equals("genericKill")) {
            this.discard();
        }
        return false;
    }
}
