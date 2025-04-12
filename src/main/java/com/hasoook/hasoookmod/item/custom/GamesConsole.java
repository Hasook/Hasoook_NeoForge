package com.hasoook.hasoookmod.item.custom;

import com.hasoook.hasoookmod.Config;
import com.hasoook.hasoookmod.gamerule.ModGameRules;
import com.hasoook.hasoookmod.net.SyncGameRulePacket;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

public class GamesConsole extends Item {
    public GamesConsole(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level pLevel, @NotNull Player pPlayer, @NotNull InteractionHand pUsedHand) {
        if (!pLevel.isClientSide) {
            ServerLevel serverLevel = (ServerLevel) pLevel;
            GameRules.BooleanValue rule = serverLevel.getGameRules().getRule(ModGameRules.RANDOM_BLOCK_DROPS);

            boolean newValue = !rule.get();
            rule.set(newValue, serverLevel.getServer());

            PacketDistributor.sendToAllPlayers(new SyncGameRulePacket(newValue));

            // 文字消息提示
            Component message = Component.translatable("hasoook.message.random_block_drops")
                    .append(Component.translatable(newValue ? "hasoook.message.options.on" : "hasoook.message.options.off")
                            .withStyle(newValue ? ChatFormatting.GREEN : ChatFormatting.RED));

            pPlayer.sendSystemMessage(message);

            // 播放音效
            pLevel.playSound(null,
                    pPlayer.getX(),
                    pPlayer.getY(),
                    pPlayer.getZ(),
                    SoundEvents.NOTE_BLOCK_BIT,
                    SoundSource.PLAYERS,
                    1.5F,
                    newValue ? 1.5F : 0.5F);

            pPlayer.swing(pUsedHand, true);
        }
        return super.use(pLevel, pPlayer, pUsedHand);
    }

    @Override
    public boolean isFoil(@NotNull ItemStack stack) {
        if (Minecraft.getInstance().level != null) {
            // 现在使用同步后的客户端规则状态
            return Minecraft.getInstance().level.getGameRules()
                    .getRule(ModGameRules.RANDOM_BLOCK_DROPS)
                    .get();
        }
        return false;
    }
}
