package com.hasoook.hasoookmod.block.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Unique;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public class HasoookLuckyBlock extends Block {
    public HasoookLuckyBlock(Properties p_49795_) {
        super(p_49795_);
    }

    @Unique
    List<EntityType<? extends Entity>> possibleEntities = Arrays.asList(
            EntityType.PUFFERFISH,
            EntityType.CREEPER,
            EntityType.ZOMBIE,
            EntityType.HORSE,
            EntityType.WOLF,
            EntityType.CAT,
            EntityType.IRON_GOLEM,
            EntityType.SNOW_GOLEM,
            EntityType.HUSK,
            EntityType.BLAZE
    );

    public void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean isMoving) {
        super.onRemove(state, world, pos, newState, isMoving);
        if (world.isClientSide) return; // 确保只有在服务器端打印
        Random random = new Random();
        EntityType<? extends Entity> randomEntityType = possibleEntities.get(random.nextInt(possibleEntities.size()));
            Mob entity = (Mob) randomEntityType.create(world);

        if (entity != null) {
            double scaleRan = random.nextInt(500) * 0.01;
            double movementSpeedRan = random.nextInt(10) * 0.1;
            double maxHealthRan = random.nextInt(100);
            entity.setPos(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
            Objects.requireNonNull(entity.getAttribute(Attributes.SCALE)).setBaseValue(scaleRan);
            Objects.requireNonNull(entity.getAttribute(Attributes.MOVEMENT_SPEED)).setBaseValue(movementSpeedRan);
            Objects.requireNonNull(entity.getAttribute(Attributes.MAX_HEALTH)).setBaseValue(maxHealthRan);
            world.addFreshEntity(entity);
        }
    }

}
