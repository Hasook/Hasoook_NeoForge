package com.hasoook.hasoookmod.event.item;

import com.hasoook.hasoookmod.Config;
import com.hasoook.hasoookmod.HasoookMod;
import com.hasoook.hasoookmod.item.custom.HugeDiamondPickaxe;
import com.hasoook.hasoookmod.sound.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.level.BlockEvent;

import java.util.HashSet;
import java.util.Set;

@EventBusSubscriber(modid = HasoookMod.MODID)
public class HugeDiamondPickaxeEvent {
    private static final Set<BlockPos> HARVESTED_BLOCKS = new HashSet<>();

    // Done with the help of https://github.com/CoFH/CoFHCore/blob/1.19.x/src/main/java/cofh/core/event/AreaEffectEvents.java
    // Don't be a jerk License
    @SubscribeEvent
    public static void onHammerUsage(BlockEvent.BreakEvent event) {
        Player player = event.getPlayer();
        ItemStack mainHandItem = player.getMainHandItem();

        if(mainHandItem.getItem() instanceof HugeDiamondPickaxe && player instanceof ServerPlayer serverPlayer) {
            BlockPos initialBlockPos = event.getPos();
            BlockState initialBlockState = event.getLevel().getBlockState(initialBlockPos);
            float initialHardness = initialBlockState.getDestroySpeed(event.getLevel(), initialBlockPos);
            if(HARVESTED_BLOCKS.contains(initialBlockPos) || initialHardness <= 0) {
                return;
            }

            for(BlockPos pos : HugeDiamondPickaxe.getBlocksToBeDestroyed(Config.hugeDiamondPickMiningRange, initialBlockPos, serverPlayer)) {
                float destroy = event.getLevel().getBlockState(pos).getDestroySpeed(event.getLevel(), pos);
                if(pos == initialBlockPos || destroy < 0) {
                    continue;
                }

                HARVESTED_BLOCKS.add(pos);
                serverPlayer.gameMode.destroyBlock(pos);
                HARVESTED_BLOCKS.remove(pos);
            }
        }
    }
}
