package com.hasoook.hasoookmod.entityEnchantment;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.Entity;

import java.util.Random;

public class EntityEnchantmentHelper {

    /**
     * 获取实体上的附魔数量。
     *
     * @param entity 实体
     * @return 附魔数量
     */
    public static int getEnchantmentSize(Entity entity) {
        CompoundTag playerNbt = entity.getPersistentData();

        if (playerNbt.contains("enchantments", Tag.TAG_LIST)) {
            ListTag enchantmentNbtList = playerNbt.getList("enchantments", Tag.TAG_COMPOUND);
            return enchantmentNbtList.size();
        }

        return 0;
    }

    /**
     * 获取指定的附魔的等级。
     *
     * @param entity 实体
     * @param enchantmentId 附魔ID
     * @return 附魔等级
     */
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

    /**
     * 向实体添加一条新附魔。如果有相同附魔则更新。
     *
     * @param entity 实体
     * @param enchantmentId 附魔ID
     * @param level 附魔等级
     */
    public static void addEnchantment(Entity entity, String enchantmentId, int level) {
        CompoundTag playerNbt = entity.getPersistentData();

        ListTag enchantmentNbtList;
        if (playerNbt.contains("enchantments", Tag.TAG_LIST)) {
            enchantmentNbtList = playerNbt.getList("enchantments", Tag.TAG_COMPOUND);
        } else {
            enchantmentNbtList = new ListTag();
        }

        // 检查是否存在相同附魔
        for (int i = 0; i < enchantmentNbtList.size(); i++) {
            CompoundTag enchantmentNbt = enchantmentNbtList.getCompound(i);
            String id = enchantmentNbt.getString("id");

            if (id.equals(enchantmentId)) {
                // 如果存在，更新附魔等级
                enchantmentNbt.putInt("lvl", level);
                playerNbt.put("enchantments", enchantmentNbtList);
                return;
            }
        }

        // 如果不存在，创建新的附魔并添加到列表中
        CompoundTag newEnchantmentNbt = new CompoundTag();
        newEnchantmentNbt.putString("id", enchantmentId);
        newEnchantmentNbt.putInt("lvl", level);
        enchantmentNbtList.add(newEnchantmentNbt);
        playerNbt.put("enchantments", enchantmentNbtList);
    }

    /**
     * 删除一条指定的附魔。
     *
     * @param entity 实体
     * @param enchantmentId 附魔ID
     */
    public static void removeEnchantment(Entity entity, String enchantmentId) {
        CompoundTag playerNbt = entity.getPersistentData();

        if (playerNbt.contains("enchantments", Tag.TAG_LIST)) {
            ListTag enchantmentNbtList = playerNbt.getList("enchantments", Tag.TAG_COMPOUND);

            for (int i = 0; i < enchantmentNbtList.size(); i++) {
                CompoundTag enchantmentNbt = enchantmentNbtList.getCompound(i);
                String id = enchantmentNbt.getString("id");

                if (id.equals(enchantmentId)) {
                    enchantmentNbtList.remove(i);
                    playerNbt.put("enchantments", enchantmentNbtList);
                    break; // 只删除第一个匹配的附魔
                }
            }
        }
    }

    /**
     * 从已拥有的附魔里随机删除一条。
     */
    public static void removeRandomEnchantment(Entity entity) {
        CompoundTag playerNbt = entity.getPersistentData();

        if (playerNbt.contains("enchantments", Tag.TAG_LIST)) {
            ListTag enchantmentNbtList = playerNbt.getList("enchantments", Tag.TAG_COMPOUND);
            int size = enchantmentNbtList.size();

            if (size > 0) {
                Random random = new Random();
                int index = random.nextInt(size); // 选择一个随机的索引
                enchantmentNbtList.remove(index);
                playerNbt.put("enchantments", enchantmentNbtList);
            }
        }
    }

    /**
     * 调整指定的附魔的等级。如果调整后等级为0，则删除该附魔。
     *
     * @param entity 实体
     * @param enchantmentId 附魔ID
     * @param adjustment 调整量（正数表示增加，负数表示减少）
     */
    public static void adjustEnchantmentLevel(Entity entity, String enchantmentId, int adjustment) {
        CompoundTag playerNbt = entity.getPersistentData();

        if (playerNbt.contains("enchantments", Tag.TAG_LIST)) {
            ListTag enchantmentNbtList = playerNbt.getList("enchantments", Tag.TAG_COMPOUND);

            for (int i = 0; i < enchantmentNbtList.size(); i++) {
                CompoundTag enchantmentNbt = enchantmentNbtList.getCompound(i);
                String id = enchantmentNbt.getString("id");

                if (id.equals(enchantmentId)) {
                    int level = enchantmentNbt.getInt("lvl");
                    int newLevel = level + adjustment;

                    if (newLevel > 0) {
                        enchantmentNbt.putInt("lvl", newLevel);
                        playerNbt.put("enchantments", enchantmentNbtList);
                    } else {
                        // 调整后的等级为0或负数时，删除该附魔
                        enchantmentNbtList.remove(i);
                        playerNbt.put("enchantments", enchantmentNbtList);
                    }
                    break;
                }
            }
        }
    }

}
