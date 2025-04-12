package com.hasoook.hasoookmod.event.player;

import com.hasoook.hasoookmod.HasoookMod;
import com.hasoook.hasoookmod.gamerule.ModGameRules;
import com.hasoook.hasoookmod.net.SyncGameRulePacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid = HasoookMod.MOD_ID)
public class PlayerLoginHandler {
    // 处理玩家登录事件
    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        syncGameRuleToPlayer(event.getEntity());
    }

    // 处理玩家切换维度事件
    @SubscribeEvent
    public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        syncGameRuleToPlayer(event.getEntity());
    }

    // 发送网络包
    private static void syncGameRuleToPlayer(Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            ServerLevel currentLevel = (ServerLevel) serverPlayer.level();
            boolean ruleState = currentLevel.getGameRules().getRule(ModGameRules.RANDOM_BLOCK_DROPS).get();
            PacketDistributor.sendToPlayer(serverPlayer, new SyncGameRulePacket(ruleState));
        }
    }
}
