package com.hasoook.hasoookmod.mixin;

import com.hasoook.hasoookmod.enchantment.ModEnchantmentHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.MagmaCube;
import net.minecraft.world.entity.monster.Shulker;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Mixin(FishingHook.class)
public abstract class FishingHookMixin extends Projectile {
    @Shadow @Final private RandomSource syncronizedRandom;
    @Shadow @Nullable public abstract Player getPlayerOwner();
    @Shadow protected abstract boolean shouldStopFishing(Player pPlayer);
    @Shadow private int life;
    @Shadow @Nullable private Entity hookedIn;
    @Shadow protected abstract void checkCollision();
    @Shadow protected abstract void setHookedEntity(@org.jetbrains.annotations.Nullable Entity pHookedEntity);
    @Shadow private int nibble;
    @Shadow private boolean openWater;
    @Shadow private int outOfWaterTime;
    @Shadow private int timeUntilHooked;
    @Shadow protected abstract boolean calculateOpenWater(BlockPos pPos);
    @Shadow private boolean biting;
    @Shadow protected abstract void catchingFish(BlockPos pPos);
    @Shadow private FishingHook.FishHookState currentState = FishingHook.FishHookState.FLYING;
    @Shadow @Final private static EntityDataAccessor<Integer> DATA_HOOKED_ENTITY;
    @Shadow @Final private static EntityDataAccessor<Boolean> DATA_BITING;
    @Shadow private int timeUntilLured;
    @Shadow private float fishAngle;
    @Shadow @Final private int lureSpeed;
    @Shadow protected abstract void pullEntity(Entity pEntity);
    @Shadow @Final private int luck;

    protected FishingHookMixin(EntityType<? extends Projectile> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder pBuilder) {
        pBuilder.define(DATA_HOOKED_ENTITY, 0);
        pBuilder.define(DATA_BITING, false);
    }

    @Inject(at = @At("HEAD"), method = "tick", cancellable = true)
    public void tick(CallbackInfo ci) {
        this.syncronizedRandom.setSeed(this.getUUID().getLeastSignificantBits() ^ this.level().getGameTime());
        super.tick();

        Player player = this.getPlayerOwner();
        if (player == null) {
            this.discard();
        } else if (this.level().isClientSide || !this.shouldStopFishing(player)) {
            // 如果在地面上，增加生命周期计数
            if (this.onGround()) {
                this.life++;
                if (this.life >= 1200) {
                    this.discard();
                    return;
                }
            } else {
                this.life = 0; // 如果不在地面上，重置生命周期
            }

            float f = 0.0F;
            BlockPos blockpos = this.blockPosition();
            FluidState fluidstate = this.level().getFluidState(blockpos);
            // 检查当前位置是否有水
            if (fluidstate.is(FluidTags.WATER) || fluidstate.is(FluidTags.LAVA)) {
                f = fluidstate.getHeight(this.level(), blockpos); // 获取水的高度
            }

            boolean flag = f > 0.0F; // 判断是否在水面上
            if (this.currentState == FishingHook.FishHookState.FLYING) {
                // 如果钩子挂在了鱼上
                if (this.hookedIn != null) {
                    this.setDeltaMovement(Vec3.ZERO);
                    this.currentState = FishingHook.FishHookState.HOOKED_IN_ENTITY;
                    return;
                }

                // 如果在水面上，设置钩子的运动状态
                if (flag) {
                    this.setDeltaMovement(this.getDeltaMovement().multiply(0.3, 0.2, 0.3));
                    this.currentState = FishingHook.FishHookState.BOBBING;
                    return;
                }

                this.checkCollision(); // 检查碰撞
            } else {
                // 如果钩子挂在实体上
                if (this.currentState == FishingHook.FishHookState.HOOKED_IN_ENTITY) {
                    if (this.hookedIn != null) {
                        // 检查挂钩的实体是否有效
                        if (!this.hookedIn.isRemoved() && this.hookedIn.level().dimension() == this.level().dimension()) {
                            this.setPos(this.hookedIn.getX(), this.hookedIn.getY(0.8), this.hookedIn.getZ());
                        } else {
                            this.setHookedEntity(null);
                            this.currentState = FishingHook.FishHookState.FLYING; // 恢复为飞行状态
                        }
                    }
                    return;
                }

                // 处理钩子的漂浮状态
                if (this.currentState == FishingHook.FishHookState.BOBBING) {
                    Vec3 vec3 = this.getDeltaMovement();
                    double d0 = this.getY() + vec3.y - (double)blockpos.getY() - (double)f;
                    // 调整钩子位置
                    if (Math.abs(d0) < 0.01) {
                        d0 += Math.signum(d0) * 0.1;
                    }

                    this.setDeltaMovement(vec3.x * 0.9, vec3.y - d0 * (double)this.random.nextFloat() * 0.2, vec3.z * 0.9);
                    if (this.nibble <= 0 && this.timeUntilHooked <= 0) {
                        this.openWater = true;
                    } else {
                        this.openWater = this.openWater && this.outOfWaterTime < 10 && this.calculateOpenWater(blockpos);
                    }

                    if (flag) {
                        this.outOfWaterTime = Math.max(0, this.outOfWaterTime - 1); // 减少离水时间
                        if (this.biting) {
                            // 调整钩子的下降速度
                            this.setDeltaMovement(
                                    this.getDeltaMovement()
                                            .add(0.0, -0.1 * (double)this.syncronizedRandom.nextFloat() * (double)this.syncronizedRandom.nextFloat(), 0.0)
                            );
                        }

                        if (!this.level().isClientSide) {
                            this.catchingFish(blockpos);
                        }
                    } else {
                        this.outOfWaterTime = Math.min(10, this.outOfWaterTime + 1);
                    }
                }
            }

            // 如果不在水中，增加重力
            if (!fluidstate.is(FluidTags.WATER)) {
                this.setDeltaMovement(this.getDeltaMovement().add(0.0, -0.03, 0.0));
            }

            this.move(MoverType.SELF, this.getDeltaMovement());
            this.updateRotation();

            // 检查钩子状态
            if (this.currentState == FishingHook.FishHookState.FLYING && (this.onGround() || this.horizontalCollision)) {
                this.setDeltaMovement(Vec3.ZERO);
            }

            double d1 = 0.92;
            this.setDeltaMovement(this.getDeltaMovement().scale(0.92)); // 应用摩擦力
            this.reapplyPosition(); // 重新应用位置
        }
        ci.cancel();
    }

    @Inject(at = @At("HEAD"), method = "catchingFish")
    private void catchingFish(BlockPos pPos, CallbackInfo ci) {
        ServerLevel serverlevel = (ServerLevel)this.level(); // 获取当前服务器的世界
        int i = 1; // 初始化鱼的捕获状态
        BlockPos blockpos = pPos.above(); // 获取钓鱼位置上方的方块位置

        // 检查当前是否在下雨，增加捕获几率
        if (this.random.nextFloat() < 0.25F && this.level().isRainingAt(blockpos)) {
            i++;
        }

        // 检查是否能看到天空，减少捕获几率
        if (this.random.nextFloat() < 0.5F && !this.level().canSeeSky(blockpos)) {
            i--;
        }

        // 如果正在 nibble 状态（鱼咬钩）
        if (this.nibble > 0) {
            this.nibble--; // 减少 nibble 值
            if (this.nibble <= 0) { // 如果 nibble 值归零
                this.timeUntilLured = 0; // 设置引诱时间为0
                this.timeUntilHooked = 0; // 设置上钩时间为0
                this.getEntityData().set(DATA_BITING, false); // 更新咬钩状态
            }
        } else if (this.timeUntilHooked > 0) { // 如果还有上钩时间
            this.timeUntilHooked -= i; // 根据捕获几率减少上钩时间
            // 计算鱼的角度并发送水花粒子效果
            this.fishAngle = this.fishAngle + (float)this.random.triangle(0.0, 9.188);
            float f = this.fishAngle * (float) (Math.PI / 180.0);
            float f1 = Mth.sin(f);
            float f2 = Mth.cos(f);
            float f3 = f1 * 0.04F;
            float f4 = f2 * 0.04F;
            double d0 = this.getX() + (double)(f1 * (float)this.timeUntilHooked * 0.1F);
            double d1 = (double)((float)Mth.floor(this.getY()) + 1.0F);
            double d2 = this.getZ() + (double)(f2 * (float)this.timeUntilHooked * 0.1F);
            BlockState blockstate = serverlevel.getBlockState(BlockPos.containing(d0, d1 - 1.0, d2));
            if (this.timeUntilHooked > 0) {
                // 检查水并生成粒子效果
                if (blockstate.is(Blocks.WATER)) {
                    if (this.random.nextFloat() < 0.15F) {
                        serverlevel.sendParticles(ParticleTypes.BUBBLE, d0, d1 - 0.1F, d2, 1, f1, 0.1, (double)f2, 0.0);
                    }
                    serverlevel.sendParticles(ParticleTypes.FISHING, d0, d1, d2, 0, f4, 0.01, (-f3), 1.0);
                    serverlevel.sendParticles(ParticleTypes.FISHING, d0, d1, d2, 0, -f4, 0.01, f3, 1.0);
                }
                // 检查熔岩并生成粒子效果
                if (blockstate.is(Blocks.LAVA)) {
                    if (this.random.nextFloat() < 0.15F) {
                        serverlevel.sendParticles(ParticleTypes.LAVA, d0, d1 - 0.1F, d2, 1, (double)f1, 0.1, (double)f2, 0.0);
                    }
                    serverlevel.sendParticles(ParticleTypes.LAVA, d0, d1, d2, 0, (double)f4, 0.01, (double)(-f3), 1.0);
                    serverlevel.sendParticles(ParticleTypes.LAVA, d0, d1, d2, 0, (double)(-f4), 0.01, (double)f3, 1.0);
                }
            } else { // 如果上钩时间归零
                this.playSound(SoundEvents.FISHING_BOBBER_SPLASH, 0.25F, 1.0F + (this.random.nextFloat() - this.random.nextFloat()) * 0.4F);
                double d3 = this.getY() + 0.5;
                if (blockstate.is(Blocks.LAVA)) {
                    serverlevel.sendParticles(
                            ParticleTypes.LAVA,
                            this.getX(),
                            d3,
                            this.getZ(),
                            (int)(2.0F + this.getBbWidth() * 20.0F),
                            (double)this.getBbWidth(),
                            0.0,
                            (double)this.getBbWidth(),
                            0.2F
                    );
                } else {
                    serverlevel.sendParticles(
                            ParticleTypes.BUBBLE,
                            this.getX(),
                            d3,
                            this.getZ(),
                            (int)(1.0F + this.getBbWidth() * 20.0F),
                            (double)this.getBbWidth(),
                            0.0,
                            (double)this.getBbWidth(),
                            0.2F
                    );
                    serverlevel.sendParticles(
                            ParticleTypes.FISHING,
                            this.getX(),
                            d3,
                            this.getZ(),
                            (int)(1.0F + this.getBbWidth() * 20.0F),
                            (double)this.getBbWidth(),
                            0.0,
                            (double)this.getBbWidth(),
                            0.2F
                    );
                }
                this.nibble = Mth.nextInt(this.random, 20, 40); // 随机设置下一次 nibble 的时间
                this.getEntityData().set(DATA_BITING, true); // 更新咬钩状态
            }
        } else if (this.timeUntilLured > 0) { // 如果还有引诱时间
            this.timeUntilLured -= i; // 根据捕获几率减少引诱时间
            float f5 = 0.15F; // 基础引诱概率
            // 根据引诱时间调整引诱概率
            if (this.timeUntilLured < 20) {
                f5 += (float)(20 - this.timeUntilLured) * 0.05F;
            } else if (this.timeUntilLured < 40) {
                f5 += (float)(40 - this.timeUntilLured) * 0.02F;
            } else if (this.timeUntilLured < 60) {
                f5 += (float)(60 - this.timeUntilLured) * 0.01F;
            }

            // 随机生成水花粒子效果
            if (this.random.nextFloat() < f5) {
                float f6 = Mth.nextFloat(this.random, 0.0F, 360.0F) * (float) (Math.PI / 180.0);
                float f7 = Mth.nextFloat(this.random, 25.0F, 60.0F);
                double d4 = this.getX() + (double)(Mth.sin(f6) * f7) * 0.1;
                double d5 = (double)((float)Mth.floor(this.getY()) + 1.0F);
                double d6 = this.getZ() + (double)(Mth.cos(f6) * f7) * 0.1;
                BlockState blockstate1 = serverlevel.getBlockState(BlockPos.containing(d4, d5 - 1.0, d6));
                if (blockstate1.is(Blocks.WATER)) {
                    serverlevel.sendParticles(ParticleTypes.SPLASH, d4, d5, d6, 2 + this.random.nextInt(2), 0.1F, 0.0, 0.1F, 0.0);
                }
            }

            // 如果引诱时间归零，设置鱼的角度和上钩时间
            if (this.timeUntilLured <= 0) {
                this.fishAngle = Mth.nextFloat(this.random, 0.0F, 360.0F);
                this.timeUntilHooked = Mth.nextInt(this.random, 20, 80);
            }
        } else { // 如果没有引诱时间
            this.timeUntilLured = Mth.nextInt(this.random, 100, 600); // 随机设置引诱时间
            this.timeUntilLured = this.timeUntilLured - this.lureSpeed; // 减去钓饵速度
        }
    }

    @Inject(at = @At("HEAD"), method = "retrieve")
    public void retrieve(ItemStack pStack, CallbackInfoReturnable<Integer> cir) {
        Player player = this.getPlayerOwner();
        int efficiencyLevel = ModEnchantmentHelper.getEnchantmentLevel(Enchantments.EFFICIENCY, pStack);
        int fpLevel = ModEnchantmentHelper.getEnchantmentLevel(Enchantments.FIRE_PROTECTION, pStack);

        if (!this.level().isClientSide && fpLevel > 0) {
            ServerLevel serverlevel = (ServerLevel) this.level();
            float f = this.fishAngle * (float) (Math.PI / 180.0);
            float f1 = Mth.sin(f);
            float f2 = Mth.cos(f);
            double b0 = this.getX() + (double) (f1 * (float) this.timeUntilHooked * 0.1F);
            double b1 = ((float) Mth.floor(this.getY()) + 1.0F);
            double b2 = this.getZ() + (double) (f2 * (float) this.timeUntilHooked * 0.1F);
            BlockState blockstate = serverlevel.getBlockState(BlockPos.containing(b0, b1 - 1.0, b2));

            if (blockstate.is(Blocks.LAVA)) {
                // 每个事件的概率
                float blockChance = 0.3f; // 方块实体
                float entityChance = 0.2f; // 实体
                float itemChance = 0.5f; // 物品

                // 生成一个0到1之间的随机数
                float randomValue = this.random.nextFloat();
                float cumulativeChance = 0.0f;

                cumulativeChance += blockChance;
                if (randomValue < cumulativeChance) {
                    // 钓到随机方块实体
                    BlockState fallingBlock = createRandomFallingBlock(level().registryAccess());

                    // 设置为不含水
                    if (fallingBlock.getBlock().defaultBlockState().hasProperty(BlockStateProperties.WATERLOGGED)) {
                        fallingBlock = fallingBlock.setValue(BlockStateProperties.WATERLOGGED, false);
                    }
                    // 设置为可以召唤监守者
                    if (fallingBlock.getBlock().defaultBlockState().hasProperty(BlockStateProperties.CAN_SUMMON) && this.random.nextFloat() >= 0.5) {
                        fallingBlock = fallingBlock.setValue(BlockStateProperties.CAN_SUMMON, true);
                        fallingBlock = fallingBlock.setValue(BlockStateProperties.SHRIEKING, true);
                    }

                    // 根据数量调整生成位置和调整伤害和运动
                    for (int i = 0; i <= efficiencyLevel; i++) {
                        FallingBlockEntity fallingBlockEntity = FallingBlockEntity.fall(serverlevel, BlockPos.containing(b0, b1 + 0.4 + i, b2), fallingBlock);
                        double c0 = player.getX() - this.getX();
                        double c1 = player.getY() - this.getY();
                        double c2 = player.getZ() - this.getZ();
                        fallingBlockEntity.setHurtsEntities(1,40); // 坠落伤害
                        fallingBlockEntity.setDeltaMovement(c0 * 0.1, c1 * 0.1 + Math.sqrt(Math.sqrt(c0 * c0 + c1 * c1 + c2 * c2)) * 0.08, c2 * 0.1);
                        serverlevel.addFreshEntity(fallingBlockEntity);
                        System.out.println(fallingBlockEntity.getBlockState());

                        if (this.level() instanceof ServerLevel serverLevel) {
                            serverLevel.getPlayers(player2 -> player instanceof ServerPlayer).forEach(player2 -> {
                                Packet<?> packet = new ClientboundSetEntityMotionPacket(fallingBlockEntity);
                                (player2).connection.send(packet);
                            });
                        }
                    }
                } else {
                    // 钓到随机实体
                    cumulativeChance += entityChance;
                    if (randomValue < cumulativeChance) {
                        Entity randomEntity = createRandomEntity();
                        System.out.println(randomEntity);

                        for (int i = 0; i <= efficiencyLevel; i++) {
                            Entity entity = randomEntity.getType().create(this.level());

                            double offsetX = (this.random.nextDouble() - 0.5) * 0.2;
                            double offsetZ = (this.random.nextDouble() - 0.5) * 0.2;
                            entity.moveTo(this.getX() + offsetX, this.getY() + 0.4, this.getZ() + offsetZ, this.getYRot(), this.getXRot());

                            double d0 = player.getX() - this.getX();
                            double d1 = player.getY() - this.getY();
                            double d2 = player.getZ() - this.getZ();
                            entity.setDeltaMovement(d0 * 0.2, d1 * 0.2 + Math.sqrt(Math.sqrt(d0 * d0 + d1 * d1 + d2 * d2)) * 0.08, d2 * 0.2);

                            this.level().addFreshEntity(entity);

                            if (entity instanceof LivingEntity livingEntity && this.random.nextFloat() >= 0.2f) {
                                // 设置随机主手物品
                                ItemStack randomWeapon = getRandomWeapon();
                                livingEntity.setItemInHand(InteractionHand.MAIN_HAND, randomWeapon);
                            }
                            if (entity instanceof Shulker shulker) {
                                // 设置随机颜色
                                DyeColor randomColor = DyeColor.byId((this.random.nextInt(16) + 1));
                                shulker.setVariant(Optional.of(randomColor));
                            }
                            if (entity instanceof MagmaCube magmaCube) {
                                // 设置随机大小
                                magmaCube.setSize(this.random.nextInt(5),true);
                            }
                        }
                    } else {
                        // 钓到随机物品
                        ItemStack randomItemStack = createRandomItem();
                        if (!randomItemStack.isEmpty()) {
                            System.out.println(randomItemStack);

                            // 获取冶炼后的物品，如果有的话
                            ItemStack smeltedItem = getSmeltedItem(randomItemStack);

                            // 使用冶炼后的物品或原物品
                            ItemStack itemStack = smeltedItem.isEmpty() ? randomItemStack.copy() : smeltedItem;
                            itemStack.setCount(1 + efficiencyLevel); // 设置数量

                            // 创建物品实体并投掷
                            ItemEntity itementity = new ItemEntity(this.level(), this.getX(), this.getY() + 0.4, this.getZ(), itemStack);
                            double d0 = player.getX() - this.getX();
                            double d1 = player.getY() - this.getY();
                            double d2 = player.getZ() - this.getZ();
                            itementity.setDeltaMovement(d0 * 0.1, d1 * 0.1 + Math.sqrt(Math.sqrt(d0 * d0 + d1 * d1 + d2 * d2)) * 0.08, d2 * 0.1);
                            this.level().addFreshEntity(itementity);
                        }
                    }
                }
                player.level().addFreshEntity(new ExperienceOrb(player.level(), player.getX(), player.getY() + 0.5, player.getZ() + 0.5, this.random.nextInt(6) + 1));
                this.discard();
            }
        }
    }

    private BlockState createRandomFallingBlock(RegistryAccess registryAccess) {
        // 随机从已注册的方块里获取
        List<Block> fallingBlocks = registryAccess.registryOrThrow(Registries.BLOCK).stream()
                .filter(block -> block != Blocks.WATER) // 水
                .filter(block -> block != Blocks.KELP) // 海带
                .filter(block -> block != Blocks.SEAGRASS) // 海草
                .filter(block -> block != Blocks.TALL_SEAGRASS) // 高海草
                .filter(block -> block.defaultBlockState().getDestroySpeed(null, BlockPos.ZERO) >= 0) // 硬度大于等于0
                .toList();
        return fallingBlocks.get(this.random.nextInt(fallingBlocks.size())).defaultBlockState();
    }

    private Entity createRandomEntity() {
        // 随机从已注册的实体里获取
        List<EntityType<?>> otherEntities = level().registryAccess().registryOrThrow(Registries.ENTITY_TYPE).stream()
                .filter(entityType -> entityType != EntityType.PLAYER) // 玩家
                .filter(entityType -> entityType != EntityType.ENDER_DRAGON) // 末影龙
                .filter(entityType -> entityType != EntityType.WITHER) // 凋灵
                .filter(entityType -> entityType != EntityType.FISHING_BOBBER) // 鱼漂
                .filter(entityType -> entityType != EntityType.INTERACTION) // 交互实体
                .filter(entityType -> entityType != EntityType.BLOCK_DISPLAY) // 展示实体
                .filter(entityType -> entityType != EntityType.ITEM_DISPLAY) // 展示实体
                .filter(entityType -> entityType != EntityType.TEXT_DISPLAY) // 展示实体
                .filter(entityType -> entityType != EntityType.WARDEN) // 监守者
                .toList();

        EntityType<?> entityType = otherEntities.get(this.random.nextInt(otherEntities.size()));
        return entityType.create(this.level());
    }

    private ItemStack getRandomWeapon() {
        // 随机从已注册的工具和武器里获取
        List<Item> items = new ArrayList<>(level().registryAccess().registryOrThrow(Registries.ITEM).stream()
                .filter(item -> item instanceof TieredItem || item instanceof SwordItem)
                .toList());
        items.add(Items.MACE);
        items.add(Items.TOTEM_OF_UNDYING);
        items.add(Items.SHIELD);
        items.add(Items.BOW);
        items.add(Items.CROSSBOW);
        items.add(Items.TRIDENT);
        items.add(Items.FISHING_ROD);
        items.add(Items.SPYGLASS);
        items.add(Items.BRUSH);
        items.add(Items.LAVA_BUCKET);
        items.add(Items.FLINT_AND_STEEL);
        items.add(Items.WARPED_FUNGUS_ON_A_STICK);

        Item randomItem = items.get(this.random.nextInt(items.size()));
        return new ItemStack(randomItem);
    }

    private ItemStack createRandomItem() {
        // 随机从已注册的物品里获取
        boolean excludeBlockItems = this.random.nextInt(10) >= 2; // 80%概率排除方块物品
        List<Item> registeredItems = level().registryAccess().registryOrThrow(Registries.ITEM).stream()
                .filter(item -> !excludeBlockItems || !(item instanceof BlockItem))
                .toList();

        if (registeredItems.isEmpty()) {
            return ItemStack.EMPTY; // 如果没有物品，返回空的 ItemStack
        }

        Item randomItem = registeredItems.get(this.random.nextInt(registeredItems.size()));
        return new ItemStack(randomItem);
    }

    private ItemStack getSmeltedItem(ItemStack itemstack) {
        if (itemstack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        Level level = this.level();
        if (level instanceof Level) {
            // 查询冶炼配方
            return level.getRecipeManager().getRecipeFor(RecipeType.SMELTING, new SingleRecipeInput(itemstack), level)
                    .map(recipe -> recipe.value().getResultItem(level.registryAccess()).copy()) // 获取冶炼结果
                    .orElse(ItemStack.EMPTY); // 如果没有冶炼配方，返回空物品
        }

        return ItemStack.EMPTY;
    }
}