package com.hasoook.hasoookmod.enchantment;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.HashMap;
import java.util.Map;

public class EnchantmentValueRegistry {
    private static final Map<Item, Integer> ENCHANTABILITY_MAP = new HashMap<>();

    // 为原版物品修改附魔能力
    static {
        register(Items.SHEARS, 1);  // 剪刀
        register(Items.ELYTRA, 5);
        register(Items.STONE, 5);
    }

    public static void register(Item item, int enchantmentValue) {
        ENCHANTABILITY_MAP.put(item, enchantmentValue);
    }

    public static int getEnchantmentValue(Item item) {
        return ENCHANTABILITY_MAP.getOrDefault(item, 0);
    }

    public static boolean isEnchantable(ItemStack stack) {
        return ENCHANTABILITY_MAP.containsKey(stack.getItem());
    }
}
