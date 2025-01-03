package com.hasoook.hasoookmod.item.custom;

import com.hasoook.hasoookmod.Config;
import com.hasoook.hasoookmod.sound.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

import java.util.ArrayList;
import java.util.List;

public class HugeDiamondPickaxe extends PickaxeItem {
    private static final String MODIFIER_ID = "huge_diamond_pickaxe";

    public HugeDiamondPickaxe(Tier p_42961_, Properties p_42964_) {
        super(p_42961_, p_42964_);
    }

    @Override
    public void inventoryTick(ItemStack pStack, Level pLevel, Entity pEntity, int pSlotId, boolean pIsSelected) {
        super.inventoryTick(pStack, pLevel, pEntity, pSlotId, pIsSelected);
        if (pEntity instanceof Player player) {
            AttributeInstance attribute = player.getAttribute(Attributes.BLOCK_INTERACTION_RANGE);
            AttributeInstance attribute2 = player.getAttribute(Attributes.ENTITY_INTERACTION_RANGE);
            if (attribute != null && attribute2 != null) {
                // 动态创建修饰符
                AttributeModifier modifier = new AttributeModifier(
                        ResourceLocation.withDefaultNamespace(MODIFIER_ID),
                        Config.hugeDiamondPickInteractionRange,  // 使用当前的配置值
                        AttributeModifier.Operation.ADD_VALUE
                );

                if (player.getMainHandItem().is(pStack.getItem())) {
                    // 添加修饰符
                    attribute.removeModifier(ResourceLocation.parse(MODIFIER_ID));
                    attribute.addPermanentModifier(modifier);

                    attribute2.removeModifier(ResourceLocation.parse(MODIFIER_ID));
                    attribute2.addPermanentModifier(modifier);
                } else {
                    // 移除修饰符
                    attribute.removeModifier(ResourceLocation.parse(MODIFIER_ID));
                    attribute2.removeModifier(ResourceLocation.parse(MODIFIER_ID));
                }
            }
        }
    }

    public static List<BlockPos> getBlocksToBeDestroyed(int range, BlockPos initialBlockPos, ServerPlayer player) {
        List<BlockPos> positions = new ArrayList<>();
        BlockHitResult traceResult = player.level().clip(new ClipContext(player.getEyePosition(1f),
                (player.getEyePosition(1f).add(player.getViewVector(1f).scale(6f + Config.hugeDiamondPickInteractionRange))),
                ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
        if(traceResult.getType() == HitResult.Type.MISS) {
            return positions;
        }

        for(int x = -range; x <= range; x++) {
            for(int y = -range; y <= range; y++) {
                for(int z = -range; z <= range; z++) {
                    positions.add(new BlockPos(initialBlockPos.getX() + x, initialBlockPos.getY() + y, initialBlockPos.getZ() + z));
                }
            }
        }

        return positions;
    }

    @Override
    public boolean hurtEnemy(ItemStack pStack, LivingEntity pTarget, LivingEntity pAttacker) {
        if (pTarget.getHealth() <= 0) {
            pTarget.playSound(ModSounds.DONG.get(), 1.0f, 1.0f);
            return true;
        }
        return false;
    }
}
