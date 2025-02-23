package com.hasoook.hasoookmod.effect.custom;

import com.hasoook.hasoookmod.entity.ModEntityHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class GoWorkEffect extends MobEffect {
    // 使用 WeakHashMap 防止内存泄漏，记录实体最后一次收获的时间戳
    private static final Map<LivingEntity, Long> lastHarvestTime = new WeakHashMap<>();

    public GoWorkEffect(MobEffectCategory category, int color) {
        super(category, color);
    }

    @Override
    public void onEffectAdded(@NotNull LivingEntity pLivingEntity, int pAmplifier) {
        // 药水效果开始时，禁用生物的攻击目标选择逻辑
        if (pLivingEntity instanceof Mob mob && !pLivingEntity.level().isClientSide) {
            mob.targetSelector.setControlFlag(Goal.Flag.TARGET, false);
            mob.setTarget(null); // 立即清除当前目标
        }
        super.onEffectAdded(pLivingEntity, pAmplifier);
    }

    @Override
    public boolean applyEffectTick(@NotNull LivingEntity entity, int amplifier) {
        if (ModEntityHelper.isBlackMob(entity) && entity instanceof Mob mob && !entity.level().isClientSide) {
            // 如果生物有攻击目标，重新触发效果，清除攻击目标
            if (mob.getTarget() != null) onEffectAdded(entity, amplifier);

            // 收获间隔（受药水等级影响）
            long lastTime = lastHarvestTime.getOrDefault(entity, 0L);
            if (entity.level().getGameTime() - lastTime < 20 - amplifier * 2L) {
                return true; // 未达到操作间隔
            }
            lastHarvestTime.put(entity, entity.level().getGameTime());

            // 搜索并处理成熟作物
            BlockPos entityPos = mob.blockPosition();
            List<BlockPos> matureCrops = findMatureCrops(mob.level(), entityPos, 16, (int) entity.getBbHeight());
            if (!matureCrops.isEmpty()) {
                BlockPos target = findNearest(entityPos, matureCrops);
                // 移动到目标位置（等级越高速度越快）
                if (mob.getNavigation().moveTo(
                        target.getX() + 0.5,
                        target.getY(),
                        target.getZ() + 0.5,
                        Math.min(1.2 + amplifier * 0.05, 2))) { // 限制最大速度
                    // 距离小于6时执行收获
                    if (mob.distanceToSqr(target.getX(), target.getY(), target.getZ()) < 6) {
                        harvestCrop(mob.level(), target);
                        mob.swing(InteractionHand.MAIN_HAND); // 播放挥手动画
                    }
                }
            }
        }
        return super.applyEffectTick(entity, amplifier);
    }

    /**
     * 在指定范围内寻找成熟作物
     *
     * @param range    搜索半径（以方块为单位）
     * @param bbHeight 实体高度
     * @return 包含所有成熟作物坐标的列表
     */
    private List<BlockPos> findMatureCrops(Level level, BlockPos center, int range, int bbHeight) {
        List<BlockPos> results = new ArrayList<>();
        // 在XZ平面扩展范围，Y轴±2格范围内搜索
        BlockPos.betweenClosedStream(center.offset(-range, -2, -range), center.offset(range, 1 + bbHeight / 2, range))
                .forEach(pos -> {
                    BlockState state = level.getBlockState(pos);
                    Block block = state.getBlock();

                    // 直接匹配西瓜/南瓜
                    if (block == Blocks.MELON || block == Blocks.PUMPKIN) {
                        results.add(pos.immutable());
                    }
                    // 检查其他作物成熟状态，排除茎类
                    else if (!(block instanceof StemBlock) && isCropMatureByProperty(state)) {
                        results.add(pos.immutable());
                    }
                });
        return results;
    }

    /**
     * 通过方块属性判断是否成熟
     * @return 如果检测到 age/growth/stage 属性且达到最大值返回 true
     */
    private boolean isCropMatureByProperty(BlockState state) {
        for (Property<?> prop : state.getProperties()) {
            String propName = prop.getName().toLowerCase(Locale.ROOT);
            // 匹配常见成熟度属性
            if ((propName.contains("age") || propName.contains("growth") || propName.contains("stage"))
                    && prop instanceof IntegerProperty intProp) {
                int current = state.getValue(intProp);
                Optional<Integer> max = intProp.getPossibleValues().stream().max(Integer::compare);
                return max.isPresent() && current >= max.get(); // 达到最大阶段（成熟）
            }
        }
        return false;
    }

    /**
     * 找到离实体最近的坐标
     * @param entityPos 实体所在位置
     * @param positions 候选坐标列表
     * @return 最近的方块坐标
     */
    private BlockPos findNearest(BlockPos entityPos, List<BlockPos> positions) {
        return positions.stream()
                .min(Comparator.comparingDouble(entityPos::distSqr)) // 使用平方距离比较（性能优化）
                .orElseThrow(); // 列表非空时安全调用
    }

    /**
     * 执行收获操作
     * @param level 当前世界
     * @param pos 目标位置
     */
    private void harvestCrop(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        Block block = state.getBlock();

        if (block instanceof CropBlock crop) {
            // 处理需要重新种植的农作物
            level.destroyBlock(pos, true);
            level.setBlock(pos, crop.getStateForAge(0), 3);  // 立即重新种植
        } else if (block == Blocks.MELON || block == Blocks.PUMPKIN) {
            // 处理不需要补种的瓜类
            level.destroyBlock(pos, true);
        } else if (block instanceof CocoaBlock) {
            // 处理可可豆的特殊补种逻辑
            Direction facing = state.getValue(CocoaBlock.FACING);
            level.destroyBlock(pos, true);
            // 重新种植可可豆并保留原方向
            level.setBlock(pos, block.defaultBlockState()
                    .setValue(CocoaBlock.AGE, 0)
                    .setValue(CocoaBlock.FACING, facing), 3);
        } else {
            // 其他作物的通用处理
            if (isCropMatureByProperty(state)) {
                level.destroyBlock(pos, true);
            }
        }
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return true; // 每tick都触发 applyEffectTick
    }
}
