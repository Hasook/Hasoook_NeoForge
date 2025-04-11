package com.hasoook.hasoookmod.event.player;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.*;
import java.util.stream.Collectors;

public class ChatSuggestionsGui {
    private static List<String> suggestions = new ArrayList<>();
    private static int selectedIndex = -1;
    private static int startX, startY;

    public static void updateSuggestions(ChatScreen chatScreen, String input) {
        Minecraft mc = Minecraft.getInstance();
        suggestions.clear();
        selectedIndex = -1;

        if (input == null || chatScreen.input == null) return;

        int cursorPos = chatScreen.input.getCursorPosition();
        int atIndex = findLastAtSymbol(input, cursorPos);
        if (atIndex == -1) return;

        String partialName;
        if (cursorPos > atIndex + 1) {
            partialName = input.substring(atIndex + 1, Math.min(cursorPos, input.length()));
        } else {
            partialName = "";
        }

        // 空玩家列表检查
        if (mc.getConnection() == null || mc.getConnection().getOnlinePlayers() == null) return;

        suggestions = mc.getConnection().getOnlinePlayers().stream()
                .filter(Objects::nonNull)
                .map(p -> p.getProfile().getName())
                .filter(name -> !name.isEmpty() && name.toLowerCase().startsWith(partialName.toLowerCase()))
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .collect(Collectors.toList());

        // 添加字体空检查
        Font font = mc.font;
        if (font == null) return;

        // 计算位置时考虑屏幕边界
        startX = chatScreen.input.getX() + font.width(input.substring(0, atIndex + 1));
        startY = chatScreen.input.getY() - (suggestions.size() * 10) - 4;

        // 确保提示框不会超出屏幕底部
        if (startY < 10) {
            startY = chatScreen.input.getY() + 12;
        }
    }

    public static void render(GuiGraphics guiGraphics, ChatScreen chatScreen) {
        if (suggestions.isEmpty()) return;

        Font font = Minecraft.getInstance().font;
        int y = startY;

        for (int i = 0; i < suggestions.size(); i++) {
            String name = suggestions.get(i);
            // 确保 name 不为空
            if (name == null || name.isEmpty()) continue;

            int finalI = i;
            MutableComponent text = Component.literal(name)
                    .withStyle(style -> style.withColor(finalI == selectedIndex ? 0x00FFFF : 0xFFFFFF));

            // 位置边界检查
            int textWidth = font.width(text);
            if (startX + textWidth > chatScreen.width) {
                startX = chatScreen.width - textWidth - 4;
            }

            // 绘制背景
            guiGraphics.fill(
                    startX - 2, y - 1,
                    startX + textWidth + 2, y + 9,
                    0x80000000
            );

            guiGraphics.drawString(
                    font,
                    text,
                    startX, y,
                    0xFFFFFF,
                    false // 此处关闭阴影渲染
            );
            y += 10;
        }
    }

    public static boolean handleKeyPress(int keyCode) {
        if (suggestions.isEmpty()) return false;

        if (keyCode == 264) { // 下箭头
            selectedIndex = (selectedIndex + 1) % suggestions.size();
            return true;
        } else if (keyCode == 265) { // 上箭头
            selectedIndex = (selectedIndex - 1 + suggestions.size()) % suggestions.size();
            return true;
        } else if (keyCode == 258) { // Tab 键
            applySelectedSuggestion();
            return true;
        }
        return false;
    }

    private static void applySelectedSuggestion() {
        if (selectedIndex == -1) return;

        Minecraft mc = Minecraft.getInstance();
        if (!(mc.screen instanceof ChatScreen)) return;

        ChatScreen chat = (ChatScreen) mc.screen;
        String input = chat.input.getValue();
        int cursorPos = chat.input.getCursorPosition();

        int atIndex = findLastAtSymbol(input, cursorPos);
        if (atIndex == -1) return;

        String newText = input.substring(0, atIndex + 1)
                + suggestions.get(selectedIndex)
                + input.substring(cursorPos);
        chat.input.setValue(newText);
        chat.input.setCursorPosition(atIndex + 1 + suggestions.get(selectedIndex).length());
        suggestions.clear();
    }

    public static int findLastAtSymbol(String text, int cursorPos) {
        for (int i = cursorPos - 1; i >= 0; i--) {
            if (text.charAt(i) == '@') {
                return i;
            }
        }
        return -1;
    }
}