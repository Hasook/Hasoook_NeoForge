package com.hasoook.hasoookmod.event.entityEnchantment;

import com.hasoook.hasoookmod.HasoookMod;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

import java.util.Objects;
import java.util.Set;

@EventBusSubscriber(modid = HasoookMod.MODID)
public class EntityEnchantmentInteract {
    @SubscribeEvent
    public static void onPlayerInteractEntity(PlayerInteractEvent.EntityInteract event) {
        if (!event.getLevel().isClientSide()) {
            Player player = event.getEntity();
            Entity target = event.getTarget();
            ItemStack itemStack = player.getMainHandItem();

            if (itemStack.getItem() == Items.BRUSH) {
                ItemEnchantments itemEnchantments = itemStack.getTagEnchantments();

                ListTag enchantmentNbtList = new ListTag();
                Set<Object2IntMap.Entry<Holder<Enchantment>>> entries = itemEnchantments.entrySet();

                for (Object2IntMap.Entry<Holder<Enchantment>> entry : entries) {
                    Holder<Enchantment> key = entry.getKey();  // 获取附魔
                    int intValue = entry.getIntValue();  // 获取附魔等级

                    CompoundTag enchantmentNbt = new CompoundTag();
                    enchantmentNbt.putString("id", Objects.requireNonNull(key.getKey()).location().toString());
                    enchantmentNbt.putInt("lvl", intValue);
                    enchantmentNbtList.add(enchantmentNbt);
                }

                CompoundTag playerNbt = target.getPersistentData();
                playerNbt.put("enchantments", enchantmentNbtList);
            }

        }

    }
}
