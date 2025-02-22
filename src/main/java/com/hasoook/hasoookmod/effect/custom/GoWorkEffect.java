package com.hasoook.hasoookmod.effect.custom;

import com.hasoook.hasoookmod.effect.ModEffects;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class GoWorkEffect extends MobEffect {
    // 定义可识别的农作物列表
    private static final List<Block> CROPS = List.of(
            Blocks.WHEAT,
            Blocks.CARROTS,
            Blocks.POTATOES,
            Blocks.BEETROOTS,
            Blocks.MELON, // 西瓜
            Blocks.PUMPKIN // 南瓜
    );

    private static final Map<LivingEntity, Long> lastHarvestTime = new WeakHashMap<>();

    public GoWorkEffect(MobEffectCategory category, int color) {
        super(category, color);
    }

    @Override
    public void onEffectAdded(@NotNull LivingEntity pLivingEntity, int pAmplifier) {
        if (pLivingEntity instanceof Mob mob && !pLivingEntity.level().isClientSide) {
            mob.targetSelector.setControlFlag(Goal.Flag.TARGET, false); // 禁用目标选择器的目标更新
            mob.setTarget(null); // 清除当前攻击目标
        }
        super.onEffectAdded(pLivingEntity, pAmplifier);
    }

    @Override
    public boolean applyEffectTick(@NotNull LivingEntity entity, int amplifier) {
        if (entity instanceof Mob mob && !entity.level().isClientSide) {
            if (mob.getTarget() != null) {
                onEffectAdded(entity, amplifier);
            }

            long lastTime = lastHarvestTime.getOrDefault(entity, 0L);
            if (entity.level().getGameTime() - lastTime < 20 - amplifier * 2L) { // 1秒冷却
                return true;
            }
            lastHarvestTime.put(entity, entity.level().getGameTime());

            BlockPos entityPos = mob.blockPosition();
            int range = 16; // 搜索范围
            // 查找周围成熟的农作物
            List<BlockPos> matureCrops = findMatureCrops(mob.level(), entityPos, range);

            if (!matureCrops.isEmpty()) {
                // 找到最近的农作物
                BlockPos target = findNearest(entityPos, matureCrops);

                // 移动到目标位置
                if (mob.getNavigation().moveTo(
                        target.getX() + 0.5,
                        target.getY(),
                        target.getZ() + 0.5,
                         Math.min(1.2 + amplifier * 0.05, 2))) {

                    // 检查距离
                    if (mob.distanceToSqr(target.getX(), target.getY(), target.getZ()) < 5) {
                        harvestCrop(mob.level(), target); // 收获作物
                        mob.swing(InteractionHand.MAIN_HAND);
                    }
                }
            }
        }
        return super.applyEffectTick(entity, amplifier);
    }

    private List<BlockPos> findMatureCrops(Level level, BlockPos center, int range) {
        List<BlockPos> results = new ArrayList<>();
        BlockPos.betweenClosedStream(center.offset(-range, -2, -range),
                        center.offset(range, 2, range))
                .forEach(pos -> {
                    BlockState state = level.getBlockState(pos);
                    if (CROPS.contains(state.getBlock())) {
                        if (state.getBlock() instanceof CropBlock crop) {
                            // 检测成熟状态
                            if (crop.isMaxAge(state)) {
                                results.add(pos.immutable());
                            }
                        } else if (state.is(Blocks.MELON) || state.is(Blocks.PUMPKIN)) {
                            // 西瓜和南瓜方块
                            results.add(pos.immutable());
                        }
                    }
                });
        return results;
    }

    private BlockPos findNearest(BlockPos entityPos, List<BlockPos> positions) {
        return positions.stream()
                .min(Comparator.comparingDouble(entityPos::distSqr))
                .orElseThrow();
    }

    private void harvestCrop(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        // 破坏作物并掉落物品
        if (state.getBlock() instanceof CropBlock) {
            level.destroyBlock(pos, true);
            level.setBlock(pos, state.getBlock().defaultBlockState(), 3);
        } else {
            level.destroyBlock(pos, true);
        }
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return true;
    }
}
