package com.hasoook.hasoookmod.event.player;

import com.hasoook.hasoookmod.HasoookMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ChatScreen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import org.lwjgl.glfw.GLFW;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@EventBusSubscriber(modid = HasoookMod.MOD_ID, value = Dist.CLIENT)
public class ClientChatHandler {
    private static boolean tabPressed = false;

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        // 检测 Tab 键释放，避免重复触发
        if (tabPressed && !isKeyPressed()) {
            tabPressed = false;
        }
    }

    @SubscribeEvent
    public static void onKeyPressed(ScreenEvent.KeyPressed.Pre event) {
        if (!(event.getScreen() instanceof ChatScreen chatScreen)) return;

        String message = chatScreen.input.getValue();
        int cursorPos = chatScreen.input.getCursorPosition();

        boolean hasAtSymbol = findLastAtSymbol(message, cursorPos) != -1;

        if (event.getKeyCode() == GLFW.GLFW_KEY_TAB && !tabPressed) {
            tabPressed = true;

            if (hasAtSymbol) {
                boolean didComplete = handleTabCompletion(chatScreen);
                if (didComplete) {
                    event.setCanceled(true);
                }
            }
        }
    }

    private static boolean handleTabCompletion(ChatScreen chatScreen) {
        String message = chatScreen.input.getValue();
        int cursorPos = chatScreen.input.getCursorPosition();

        int atIndex = findLastAtSymbol(message, cursorPos);
        if (atIndex == -1) return false;

        String partialName = message.substring(atIndex + 1, cursorPos);
        List<String> suggestions = getOnlinePlayerNames(partialName);

        if (!suggestions.isEmpty()) {
            String completion = suggestions.getFirst();
            String newMessage = message.substring(0, atIndex + 1) + completion + message.substring(cursorPos);
            chatScreen.input.setValue(newMessage);
            chatScreen.input.setCursorPosition(atIndex + 1 + completion.length());
            return true; // 表示成功补全
        }
        return false;
    }

    private static int findLastAtSymbol(String text, int cursorPos) {
        for (int i = cursorPos - 1; i >= 0; i--) {
            if (text.charAt(i) == '@') {
                return i;
            }
        }
        return -1;
    }

    private static List<String> getOnlinePlayerNames(String partial) {
        Minecraft mc = Minecraft.getInstance();
        return Objects.requireNonNull(mc.getConnection()).getOnlinePlayers().stream()
                .map(player -> player.getProfile().getName())
                .filter(name -> name.toLowerCase().startsWith(partial.toLowerCase()))
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .collect(Collectors.toList());
    }

    private static boolean isKeyPressed() {
        return GLFW.glfwGetKey(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_KEY_TAB) == GLFW.GLFW_PRESS;
    }
}