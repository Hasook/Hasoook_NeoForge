package com.hasoook.hasoookmod.mixin;

import com.hasoook.hasoookmod.event.player.ChatSuggestionsGui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.CommandSuggestions;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChatScreen.class)
public abstract class ChatScreenMixin extends Screen {

    @Shadow public EditBox input;
    @Shadow CommandSuggestions commandSuggestions;

    @Shadow private String initial;

    protected ChatScreenMixin(String pInitial) {
        super(Component.translatable("chat_screen.title"));
        this.initial = pInitial;
    }

    @Inject(method = "render", at = @At("TAIL"))
    public void renderSuggestions(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        ChatSuggestionsGui.render(guiGraphics, (ChatScreen) (Object) this);
    }

    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    private void handleCustomTabCompletion(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        // 只处理 Tab 键（258）
        if (keyCode != 258) return;

        String currentInput = this.input.getValue();
        int cursorPos = this.input.getCursorPosition();

        // 仅在存在@符号时启用自定义补全
        if (ChatSuggestionsGui.findLastAtSymbol(currentInput, cursorPos) != -1) {
            if (ChatSuggestionsGui.handleKeyPress(keyCode)) {
                cir.setReturnValue(true); // 拦截事件
            }
        }
    }

    @Override
    public void tick() {
        super.tick();
        ChatSuggestionsGui.updateSuggestions((ChatScreen) (Object) this, this.input.getValue());
    }
}