package com.hasoook.hasoookmod.entityEnchantment;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.Entity;

public class EntityEnchantmentHelper {

    // 获取指定的附魔的等级
    public static int getEnchantmentLevel(Entity entity, String enchantmentId) {
        CompoundTag playerNbt = entity.getPersistentData();

        if (playerNbt.contains("enchantments", Tag.TAG_LIST)) {
            ListTag enchantmentNbtList = playerNbt.getList("enchantments", Tag.TAG_COMPOUND);

            for (int i = 0; i < enchantmentNbtList.size(); i++) {
                CompoundTag enchantmentNbt = enchantmentNbtList.getCompound(i);
                String id = enchantmentNbt.getString("id");
                int level = enchantmentNbt.getInt("lvl");

                if (id.equals(enchantmentId)) {
                    return level;
                }
            }
        }

        return 0;
    }

    // 获取已拥有的附魔词条数
    public static int getEnchantmentSize(Entity entity) {
        CompoundTag playerNbt = entity.getPersistentData();

        if (playerNbt.contains("enchantments", Tag.TAG_LIST)) {
            ListTag enchantmentNbtList = playerNbt.getList("enchantments", Tag.TAG_COMPOUND);
            return enchantmentNbtList.size();
        }

        return 0;
    }

}
