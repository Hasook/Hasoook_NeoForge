package com.hasoook.hasoookmod.item.custom;

import com.hasoook.hasoookmod.item.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.LodestoneTracker;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.UUID;

public class PetCompass extends Item {
    public PetCompass(Properties pProperties) {
        super(pProperties);
    }

    @Nullable
    public static GlobalPos getSpawnPosition(Level pLevel) {
        return pLevel.dimensionType().natural() ? GlobalPos.of(pLevel.dimension(), pLevel.getSharedSpawnPos()) : null;
    }

    @Override
    public boolean isFoil(ItemStack pStack) {
        return pStack.has(DataComponents.LODESTONE_TRACKER) || super.isFoil(pStack);
    }

    @Override
    public void inventoryTick(ItemStack pStack, Level pLevel, Entity pEntity, int pItemSlot, boolean pIsSelected) {
        if (pLevel instanceof ServerLevel serverlevel) {
            LodestoneTracker lodestonetracker = pStack.get(DataComponents.LODESTONE_TRACKER);
            if (lodestonetracker != null) {
                LodestoneTracker updatedTracker = lodestonetracker.tick(serverlevel);
                if (updatedTracker != lodestonetracker) {
                    pStack.set(DataComponents.LODESTONE_TRACKER, updatedTracker);
                }
            }
        }
    }

    @Override
    public InteractionResult useOn(UseOnContext pContext) {
        BlockPos blockpos = pContext.getClickedPos();
        Level level = pContext.getLevel();
        if (!level.getBlockState(blockpos).is(Blocks.LODESTONE)) {
            return super.useOn(pContext);
        } else {
            level.playSound(null, blockpos, SoundEvents.LODESTONE_COMPASS_LOCK, SoundSource.PLAYERS, 1.0F, 1.0F);
            Player player = pContext.getPlayer();
            ItemStack stack = pContext.getItemInHand();
            boolean singleItem = !player.hasInfiniteMaterials() && stack.getCount() == 1;

            LodestoneTracker tracker = new LodestoneTracker(Optional.of(GlobalPos.of(level.dimension(), blockpos)), true);

            if (singleItem) {
                stack.set(DataComponents.LODESTONE_TRACKER, tracker);
            } else {
                ItemStack newStack = stack.transmuteCopy(ModItems.PET_COMPASS.get(), 1); // 确保注册了PET_COMPASS物品
                stack.consume(1, player);
                newStack.set(DataComponents.LODESTONE_TRACKER, tracker);
                if (!player.getInventory().add(newStack)) {
                    player.drop(newStack, false);
                }
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
    }

    @Override
    public String getDescriptionId(ItemStack pStack) {
        return pStack.has(DataComponents.LODESTONE_TRACKER)
                ? "item.yourmod.pet_compass_locked"
                : super.getDescriptionId(pStack);
    }
}
