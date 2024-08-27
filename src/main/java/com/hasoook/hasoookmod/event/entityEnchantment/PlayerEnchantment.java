package com.hasoook.hasoookmod.event.entityEnchantment;

import com.hasoook.hasoookmod.HasoookMod;
import com.hasoook.hasoookmod.item.ModItems;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

import java.util.Objects;
import java.util.Set;

@EventBusSubscriber(modid = HasoookMod.MODID)
public class PlayerEnchantment {
    @SubscribeEvent
    public static void onPlayerInteractEntity(PlayerInteractEvent.RightClickItem event) {
        if (!event.getLevel().isClientSide()) {
            Player player = event.getEntity();
            ItemStack itemStack = event.getItemStack();

            if (itemStack.getItem() == ModItems.ENCHANTMENT_BRUSH.get() && !event.getLevel().isClientSide()) {
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

                CompoundTag playerNbt = player.getPersistentData();
                playerNbt.put("enchantments", enchantmentNbtList);
                player.swing(InteractionHand.MAIN_HAND,true); // 摇动玩家的主手
            }
        }
    }
}
