package com.hasoook.hasoookmod.item.custom;

import net.minecraft.core.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.LodestoneTracker;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.Optional;

public class PetCompassItem extends Item {
    public PetCompassItem(Item.Properties properties) {
        super(properties);
    }

    @Nullable
    public static GlobalPos getSpawnPosition(Level pLevel) {
        return pLevel.dimensionType().natural() ? GlobalPos.of(pLevel.dimension(), pLevel.getSharedSpawnPos()) : null;
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return stack.has(DataComponents.LODESTONE_TRACKER) || super.isFoil(stack);
    }

    @Override
    public void inventoryTick(@NotNull ItemStack stack, @NotNull Level level, @NotNull Entity entity, int slot, boolean selected) {
        if (level instanceof ServerLevel serverLevel) {
            LodestoneTracker tracker = stack.get(DataComponents.LODESTONE_TRACKER);
            if (tracker != null) {
                LodestoneTracker updatedTracker = tracker.tick(serverLevel);
                if (updatedTracker != tracker) {
                    stack.set(DataComponents.LODESTONE_TRACKER, updatedTracker);
                }
            }
        }
    }

    @Override
    public @NotNull InteractionResult useOn(UseOnContext context) {
        BlockPos pos = context.getClickedPos();
        Level level = context.getLevel();
        if (!level.getBlockState(pos).is(Blocks.LODESTONE)) {
            return super.useOn(context);
        }

        level.playSound(null, pos, SoundEvents.LODESTONE_COMPASS_LOCK, SoundSource.PLAYERS, 1.0F, 1.0F);
        Player player = context.getPlayer();
        ItemStack stack = context.getItemInHand();
        boolean singleItem = false;
        if (player != null) {
            singleItem = !player.hasInfiniteMaterials() && stack.getCount() == 1;
        }

        LodestoneTracker tracker = new LodestoneTracker(
                Optional.of(GlobalPos.of(level.dimension(), pos)),
                true
        );

        if (singleItem) {
            stack.set(DataComponents.LODESTONE_TRACKER, tracker);
        } else {
            ItemStack newStack = stack.transmuteCopy(Items.COMPASS, 1);
            stack.consume(1, player);
            newStack.set(DataComponents.LODESTONE_TRACKER, tracker);
            if (player != null && !player.getInventory().add(newStack)) {
                player.drop(newStack, false);
            }
        }

        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public @NotNull String getDescriptionId(ItemStack stack) {
        return stack.has(DataComponents.LODESTONE_TRACKER)
                ? "item.minecraft.lodestone_compass"
                : super.getDescriptionId(stack);
    }
}
