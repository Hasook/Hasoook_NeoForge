package com.hasoook.hasoookmod.mixin.item;

import com.hasoook.hasoookmod.enchantment.EnchantmentValueRegistry;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Item.class)
public abstract class ItemEnchantableMixin {

    @Inject(method = "isEnchantable", at = @At("HEAD"), cancellable = true)
    private void onIsEnchantable(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (EnchantmentValueRegistry.isEnchantable(stack)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "getEnchantmentValue", at = @At("HEAD"), cancellable = true)
    private void onGetEnchantmentValue(CallbackInfoReturnable<Integer> cir) {
        Item item = (Item) (Object) this;
        int value = EnchantmentValueRegistry.getEnchantmentValue(item);
        if (value > 0) {
            cir.setReturnValue(value);
        }
    }
}
