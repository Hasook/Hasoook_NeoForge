package com.hasoook.hasoookmod.entity.custom;

import com.google.common.annotations.VisibleForTesting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.fluids.FluidType;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class MeteoriteEntity extends LivingEntity {
    private static final EntityDataAccessor<Integer> ID_SIZE = SynchedEntityData.defineId(MeteoriteEntity.class, EntityDataSerializers.INT);
    private static final String NBT_SIZE = "MeteoriteSize";

    public MeteoriteEntity(EntityType<? extends LivingEntity> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        this.fixupDimensions();
        this.setSize(1);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag pCompound) {
        super.addAdditionalSaveData(pCompound);
        pCompound.putInt(NBT_SIZE, this.getSize());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag pCompound) {
        super.readAdditionalSaveData(pCompound);
        int size = pCompound.contains(NBT_SIZE) ? pCompound.getInt(NBT_SIZE) : 1;
        this.setSize(size);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.@NotNull Builder pBuilder) {
        super.defineSynchedData(pBuilder);
        pBuilder.define(ID_SIZE, 1);
    }

    @Override
    public @NotNull EntityDimensions getDefaultDimensions(@NotNull Pose pPose) {
        return super.getDefaultDimensions(pPose).scale((float)this.getSize());
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createLivingAttributes()
                .add(Attributes.MAX_HEALTH, 10d);
    }

    @VisibleForTesting
    public void setSize(int pSize) {
        int i = Mth.clamp(pSize, 1, 127);
        this.entityData.set(ID_SIZE, i);
        this.reapplyPosition();
        this.refreshDimensions();
    }

    public int getSize() {
        return this.entityData.get(ID_SIZE);
    }

    @Override
    public void tick() {
        Vec3 delta = this.getDeltaMovement();
        double speed = delta.length();

        spawnSmokeTrail(speed);

        if (!this.level().isClientSide && (this.horizontalCollision || this.verticalCollision)) {
            this.explode();
            this.discard(); // 销毁实体
        }

        super.tick();
    }

    private void explode() {
        final float BASE_POWER = 2.0f;  // 基础爆炸威力
        final float SIZE_MODIFIER = 3f; // 尺寸影响系数

        // 计算最终爆炸威力
        float explosionPower = BASE_POWER + (this.getSize() * SIZE_MODIFIER);

        if (!this.level().isClientSide) {
            // 创建球形爆炸
            createSphericalExplosion(explosionPower);
        }

        // 生成爆炸粒子
        generateExplosionEffects(explosionPower);

        // 生成持续烟雾
        generatePostExplosionSmoke(40);
    }

    // 自定义球形爆炸（核心改进）
    private void createSphericalExplosion(float power) {
        // 主爆炸参数增强（垂直方向加强）
        this.level().explode(
                this,
                this.getX(),
                this.getY() + this.getBbHeight()/2,
                this.getZ(),
                power * 1.2f,  // 增加20%威力
                true,
                Level.ExplosionInteraction.TNT
        );

        // 辅助爆炸点生成（增强深度）
        int verticalPoints = 3; // 垂直方向爆炸点数量
        for(int i = 0; i < verticalPoints; i++) {
            // 向下穿透的爆炸
            this.level().explode(
                    this,
                    this.getX(),
                    this.getY() - (i * 2), // 每层下降2格
                    this.getZ(),
                    power * 0.8f,
                    true,
                    Level.ExplosionInteraction.TNT
            );
        }

        // 球形辅助爆炸（改进随机分布）
        int auxCount = 6; // 增加辅助点数量
        for(int i = 0; i < auxCount; i++) {
            Vec3 offset = new Vec3(
                    (this.random.nextDouble() - 0.5) * power * 0.8, // X扩散加强
                    (this.random.nextDouble() - 0.5) * power * 1.2, // Y扩散重点加强
                    (this.random.nextDouble() - 0.5) * power * 0.8  // Z扩散加强
            );

            this.level().explode(
                    this,
                    this.getX() + offset.x,
                    this.getY() + this.getBbHeight()/2 + offset.y,
                    this.getZ() + offset.z,
                    power * 0.7f,
                    true,
                    Level.ExplosionInteraction.TNT
            );
        }
    }

    private void generateExplosionEffects(float power) {
        Level level = this.level();

        // 强化参数
        int particleCount = (int) (power * 100);
        double radius = power * 2.5;
        double verticalMultiplier = 0.7 + power * 0.3;

        // 冲击波形状生成器（球面分布）
        for (int i = 0; i < particleCount * 0.6; i++) {
            // 球坐标系转换
            double theta = level.random.nextDouble() * Math.PI;
            double phi = level.random.nextDouble() * Math.PI * 2;
            double r = radius * (0.7 + level.random.nextDouble() * 0.6);

            // 转换为笛卡尔坐标
            double dx = r * Math.sin(theta) * Math.cos(phi);
            double dy = r * Math.cos(theta);
            double dz = r * Math.sin(theta) * Math.sin(phi);

            level.addParticle(
                    ParticleTypes.FLAME, // 改用可移动的火焰粒子
                    true,
                    this.getX() + dx * 0.8,
                    this.getY() + this.getBbHeight()/2 + dy * verticalMultiplier,
                    this.getZ() + dz * 0.8,
                    dx * 0.15 * power, // 外向速度
                    dy * 0.2 * power,
                    dz * 0.15 * power
            );
        }

        // 动态核心闪光（旋转上升粒子）
        for (int i = 0; i < particleCount * 0.4; i++) {
            double angle = level.random.nextDouble() * Math.PI * 2;
            double speed = 0.3 + power * 0.2;

            // 螺旋上升路径
            level.addParticle(
                    ParticleTypes.FLASH, // 高亮闪光粒子
                    true,
                    this.getX() + Math.cos(angle) * power * 0.5,
                    this.getY() + this.getBbHeight()/2 + level.random.nextDouble() * power * 0.3,
                    this.getZ() + Math.sin(angle) * power * 0.5,
                    Math.cos(angle + Math.PI/2) * speed, // 切线速度
                    (0.4 + level.random.nextDouble() * 0.3) * power,
                    Math.sin(angle + Math.PI/2) * speed
            );
        }

        // 地面冲击环（极坐标分布）
        if (power > 1.5) {
            for (int i = 0; i < particleCount/3; i++) {
                double angle = level.random.nextDouble() * Math.PI * 2;
                double spread = radius * 0.9;

                level.addParticle(
                        ParticleTypes.EXPLOSION_EMITTER,
                        true,
                        this.getX() + Math.cos(angle) * spread,
                        this.getY() + 0.1, // 紧贴地面
                        this.getZ() + Math.sin(angle) * spread,
                        Math.cos(angle) * 0.4 * power, // 径向速度
                        0.15 * power, // 轻微上扬
                        Math.sin(angle) * 0.4 * power
                );
            }
        }

        // 三维火花溅射（立方体分布）
        for (int i = 0; i < particleCount * 2; i++) {
            double spread = radius * 1.2;
            level.addParticle(
                    ParticleTypes.LAVA,
                    this.getX() + (level.random.nextGaussian() * spread),
                    this.getY() + this.getBbHeight()/2 + (level.random.nextGaussian() * spread * 0.6),
                    this.getZ() + (level.random.nextGaussian() * spread),
                    (level.random.nextGaussian() * 0.25) * power,
                    (level.random.nextDouble() * 1.5) * power,
                    (level.random.nextGaussian() * 0.25) * power
            );
        }

        // 气浪效果（速度场模拟）
        for (int i = 0; i < particleCount/2; i++) {
            double distance = 1 + level.random.nextDouble() * radius;
            double angle = level.random.nextDouble() * Math.PI * 2;

            level.addParticle(
                    ParticleTypes.CLOUD, // 改用云粒子表现气浪
                    true,
                    this.getX() + Math.cos(angle) * distance,
                    this.getY() + this.getBbHeight()/2 + level.random.nextDouble() * 2,
                    this.getZ() + Math.sin(angle) * distance,
                    Math.cos(angle) * 0.7 * power,
                    (level.random.nextDouble() - 0.2) * 0.4 * power,
                    Math.sin(angle) * 0.7 * power
            );
        }
    }

    // 生成后续烟雾（持续效果）
    private void generatePostExplosionSmoke(int duration) {
        if (!(this.level() instanceof ServerLevel serverLevel)) return;

        // 创建烟雾生成任务
        new java.util.Timer().scheduleAtFixedRate(
                new java.util.TimerTask() {
                    int ticks = 0;

                    @Override
                    public void run() {
                        if (ticks++ >= duration) this.cancel();

                        serverLevel.sendParticles(
                                ParticleTypes.LARGE_SMOKE,
                                getX(),
                                getY() + getBbHeight()/2,
                                getZ(),
                                20,
                                3.0,
                                2.0,
                                3.0,
                                0.05
                        );

                        serverLevel.sendParticles(
                                ParticleTypes.CAMPFIRE_COSY_SMOKE,
                                getX(),
                                getY() + getBbHeight()/2,
                                getZ(),
                                50,
                                6.0,
                                6.0,
                                6.0,
                                0.05
                        );
                    }
                },
                0,  // 立即开始
                20
        );
    }

    private void spawnSmokeTrail(double currentSpeed) {
        Level level = this.level();

        RandomSource random = this.random;
        float size = this.getSize();

        // 动态调整粒子参数（基于速度和尺寸）
        int baseParticles = (int) (10 + (size * currentSpeed * 2)); // 速度越快粒子越多
        int maxParticles = Mth.clamp(baseParticles, 1, 100); // 控制性能上限
        float speedFactor = (float) Mth.clamp(currentSpeed * 0.5, 0.1, 1.5); // 速度影响粒子扩散

        Vec3 motion = this.getDeltaMovement(); // 获取陨石运动方向
        Vec3 reverseDir = motion.normalize().scale(-1); // 反方向作为基础喷射方向

        // 生成粒子
        for (int i = 0; i < maxParticles; i++) {
            // 圆锥形分布
            float coneAngle = 25 * (1 - speedFactor); // 速度越快喷射越集中
            Vec3 randomDir = new Vec3(
                    random.nextGaussian() * 0.3,
                    random.nextGaussian() * 0.2,
                    random.nextGaussian() * 0.3
            ).yRot((float) Math.toRadians(coneAngle * random.nextFloat()));

            Vec3 spawnPos = this.position()
                    .add(reverseDir.scale(0.5)) // 从尾部后方生成
                    .add(randomDir.scale(size * 0.6)); // 根据尺寸扩大生成范围

            // 基础烟雾粒子（80%概率）
            if (random.nextFloat() < 0.8) {
                level.addParticle(
                        ParticleTypes.CAMPFIRE_COSY_SMOKE,
                        spawnPos.x,
                        spawnPos.y + size * 0.2,
                        spawnPos.z,
                        // 粒子运动参数（加入随机性和空气阻力模拟）
                        (reverseDir.x + random.nextGaussian() * 0.08) * speedFactor,
                        (reverseDir.y * 0.3 + random.nextGaussian() * 0.05) * speedFactor,
                        (reverseDir.z + random.nextGaussian() * 0.08) * speedFactor
                );
                level.addParticle(
                        ParticleTypes.CAMPFIRE_COSY_SMOKE,
                        spawnPos.x,
                        spawnPos.y,
                        spawnPos.z,
                        reverseDir.x + random.nextGaussian() * 0.01,
                        0.05,
                        reverseDir.z + random.nextGaussian() * 0.01
                );
            }

            // 岩浆粒子（15%概率，速度越快越多）
            if (random.nextFloat() < 0.15 * speedFactor) {
                level.addParticle(
                        ParticleTypes.LAVA,
                        spawnPos.x,
                        spawnPos.y,
                        spawnPos.z,
                        (reverseDir.x * 0.5 + random.nextGaussian() * 0.1) * speedFactor,
                        (reverseDir.y * 0.2 + random.nextGaussian() * 0.1) * speedFactor,
                        (reverseDir.z * 0.5 + random.nextGaussian() * 0.1) * speedFactor
                );
            }

            // 大型烟雾（每5个粒子生成一个）
            if (i % 5 == 0) {
                level.addParticle(
                        ParticleTypes.LARGE_SMOKE,
                        true, // 强制渲染
                        spawnPos.x,
                        spawnPos.y + size * 0.1,
                        spawnPos.z,
                        (reverseDir.x * 0.2 + random.nextGaussian() * 0.05) * speedFactor,
                        (reverseDir.y * 0.1 + random.nextGaussian() * 0.02) * speedFactor,
                        (reverseDir.z * 0.2 + random.nextGaussian() * 0.05) * speedFactor
                );
            }
        }

        float smokeRingChance = 0.25f; // 每帧生成烟圈的概率
        float ringRadiusBase = size * 0.8f; // 基础半径
        int ringParticles = 15 + (int)(size * 5); // 烟圈粒子数量
        if (random.nextFloat() < smokeRingChance) {
            generateSmokeRing(level, currentSpeed, ringRadiusBase, ringParticles);
        }
    }

    private void generateSmokeRing(Level level, double speed, float baseRadius, int particles) {
        Vec3 motionDir = this.getDeltaMovement().normalize();

        float dynamicRadius = baseRadius * (0.8f + this.random.nextFloat() * 0.4f); // 动态变化的半径
        float verticalOffset = 0.3f * (this.random.nextFloat() - 0.5f); // 垂直位置波动

        for (int i = 0; i < particles; i++) {
            // 环形分布计算
            float angle = (float) (Math.PI * 2 * i / particles);
            Vec3 ringOffset = new Vec3(
                    Math.cos(angle) * dynamicRadius,
                    verticalOffset,
                    Math.sin(angle) * dynamicRadius
            );

            // 应用方向对齐
            Vec3 finalOffset = ringOffset.xRot((float) Math.toRadians(-30)) // 30度仰角
                    .yRot(this.getYRot() * Mth.DEG_TO_RAD);

            Vec3 spawnPos = this.position()
                    .add(motionDir.scale(-0.5)) // 在尾部后方生成
                    .add(finalOffset);

            // 烟圈粒子参数
            float spread = 0.15f + this.random.nextFloat() * 0.1f;
            float speedFactor = (float) (0.3f + speed * 0.2f);

            level.addParticle(
                    ParticleTypes.CLOUD, // 使用白色云粒子
                    true,
                    spawnPos.x,
                    spawnPos.y + 0.2,
                    spawnPos.z,
                    // 运动参数（向外扩散+随机扰动）
                    (finalOffset.normalize().x + random.nextGaussian() * 0.08) * speedFactor,
                    (0.1 + random.nextGaussian() * 0.05) * speedFactor * 0.5f,
                    (finalOffset.normalize().z + random.nextGaussian() * 0.08) * speedFactor
            );

            // 添加次级烟圈（更小更密集）
            if (i % 3 == 0) {
                Vec3 innerPos = spawnPos.add(
                        random.nextGaussian() * 0.1,
                        random.nextGaussian() * 0.05,
                        random.nextGaussian() * 0.1
                );
                level.addParticle(
                        ParticleTypes.CLOUD,
                        true,
                        innerPos.x,
                        innerPos.y,
                        innerPos.z,
                        motionDir.x * -0.2f,
                        motionDir.y * -0.1f,
                        motionDir.z * -0.2f
                );
            }
        }
    }

    @Override
    public void refreshDimensions() {
        // 强制更新碰撞箱
        this.setBoundingBox(this.makeBoundingBox());
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> pKey) {
        if (ID_SIZE.equals(pKey)) {
            this.refreshDimensions();
            this.setYRot(this.yHeadRot);
            this.yBodyRot = this.yHeadRot;
        }
        super.onSyncedDataUpdated(pKey);
    }

    @Override
    public Iterable<ItemStack> getArmorSlots() {
        return Collections.emptyList();
    }

    @Override
    public ItemStack getItemBySlot(EquipmentSlot pSlot) {
        return ItemStack.EMPTY;
    }

    @Override
    public void setItemSlot(EquipmentSlot pSlot, ItemStack pStack) {

    }

    @Override
    public HumanoidArm getMainArm() {
        return HumanoidArm.RIGHT;
    }

    @Override
    public boolean hurt(DamageSource pSource, float pAmount) {
        String source = pSource.getMsgId(); // 获取伤害类型
        if (source.equals("genericKill")) {
            this.discard();
        }
        return false;
    }

    @Override
    public boolean isCustomNameVisible() {
        return false;
    }

    @Override
    public boolean isAffectedByFluids() {
        return false; // 禁止流体影响实体运动
    }

    @Override
    public boolean isPushable() {
        return false; // 不可被推挤
    }

    @Override
    public boolean isPushedByFluid(@NotNull FluidType type) {
        return false; // 不可被流体推动
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double pDistance) {
        return true; // 总是渲染
    }

    @Override
    public boolean canRide(@NotNull Entity entity) {
        return false;// 禁止骑乘
    }
}
