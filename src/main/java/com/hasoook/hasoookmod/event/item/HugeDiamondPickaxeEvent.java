package com.hasoook.hasoookmod.event.item;

import com.hasoook.hasoookmod.HasoookMod;
import com.hasoook.hasoookmod.item.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.BlockEvent;

@EventBusSubscriber(modid = HasoookMod.MODID)
public class HugeDiamondPickaxeEvent {
    @SubscribeEvent
    public static void BreakEvent(BlockEvent.BreakEvent event) {
        Player player = event.getPlayer();
        if (player.getMainHandItem().is(ModItems.HUGE_DIAMOND_PICKAXE)) {
            Level level = (Level) event.getLevel();
            BlockPos pos = event.getPos();
            if (event.getState().getDestroySpeed(level, pos) > 0) {
                for (int x = -2; x <= 2; x++) {
                    for (int y = -2; y <= 2; y++) {
                        for (int z = -2; z <= 2; z++) {
                            BlockPos targetPos = pos.offset(x, y, z);
                            float destroySpeed = level.getBlockState(targetPos).getDestroySpeed(level, pos);
                            if (destroySpeed >= 0 && destroySpeed <= 60) {
                                level.destroyBlock(targetPos, !player.isCreative());
                            }
                        }
                    }
                }
            }
        }
    }
}
