package com.hasoook.hasoookmod.event.player;

import com.hasoook.hasoookmod.HasoookMod;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.ServerChatEvent;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@EventBusSubscriber(modid = HasoookMod.MOD_ID)
public class ChatHandler {
    private static final Pattern MENTION_PATTERN = Pattern.compile("@(\\b\\w{3,16}\\b|所有人)");
    private static final TextColor MENTION_COLOR = TextColor.fromRgb(0x00FFFF); // 青色

    @SubscribeEvent
    public static void onServerChat(ServerChatEvent event) {
        String message = event.getRawText();
        ServerPlayer sender = event.getPlayer();
        Set<ServerPlayer> mentionedPlayers = new HashSet<>();
        boolean isAtAll = false;

        Matcher matcher = MENTION_PATTERN.matcher(message);
        while (matcher.find()) {
            String target = matcher.group(1);
            if (target.equalsIgnoreCase("所有人")) {
                if (canAtAll(sender)) {
                    isAtAll = true;
                } else {
                    sender.sendSystemMessage(Component.literal("你没有权限@所有人！").withStyle(ChatFormatting.RED));
                }
            } else {
                findPlayerByName(target, sender.server).ifPresent(mentionedPlayers::add);
            }
        }

        event.setMessage(processMentions(message, mentionedPlayers, isAtAll, sender));
        sendNotifications(mentionedPlayers, isAtAll, sender.server);
    }

    private static boolean canAtAll(ServerPlayer player) {
        // 4级权限对应管理员
        return player.hasPermissions(4) ||
                player.getServer().getPlayerList().isOp(player.getGameProfile());
    }

    // 精确查找玩家（不区分大小写）
    private static Optional<ServerPlayer> findPlayerByName(String name, MinecraftServer server) {
        return server.getPlayerList().getPlayers().stream()
                .filter(p -> p.getGameProfile().getName().equalsIgnoreCase(name))
                .findFirst();
    }

    // 高亮逻辑优化
    private static Component processMentions(String message, Set<ServerPlayer> mentionedPlayers,
                                             boolean isAtAll, ServerPlayer sender) {
        MutableComponent result = Component.literal("");
        Matcher matcher = MENTION_PATTERN.matcher(message);
        int lastEnd = 0;

        while (matcher.find()) {
            result.append(message.substring(lastEnd, matcher.start()));

            String target = matcher.group(1);
            boolean isValidMention = false;

            if (target.equalsIgnoreCase("所有人")) {
                isValidMention = isAtAll;
            } else {
                isValidMention = mentionedPlayers.stream()
                        .anyMatch(p -> p.getGameProfile().getName().equalsIgnoreCase(target));
            }

            Style style = isValidMention
                    ? Style.EMPTY.withColor(MENTION_COLOR)
                    : Style.EMPTY;

            result.append(Component.literal("@" + target).setStyle(style));
            lastEnd = matcher.end();
        }

        if (lastEnd < message.length()) {
            result.append(message.substring(lastEnd));
        }

        return result;
    }

    // 统一通知方式
    private static void sendNotifications(Set<ServerPlayer> mentionedPlayers,
                                          boolean isAtAll, MinecraftServer server) {
        Collection<ServerPlayer> targets = isAtAll
                ? server.getPlayerList().getPlayers()
                : mentionedPlayers;

        targets.forEach(player -> {
            // 音效
            player.playNotifySound(SoundEvents.EXPERIENCE_ORB_PICKUP,
                    SoundSource.PLAYERS, 1.0f, 1.0f);

            // ActionBar小字提示
            player.connection.send(new ClientboundSetActionBarTextPacket(
                    Component.literal("你被提及了！")
                            .withStyle(Style.EMPTY.withColor(0x00FFFF))
            ));
        });
    }
}
