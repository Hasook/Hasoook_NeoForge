package com.hasoook.hasoookmod.entityEnchantment;

import com.hasoook.hasoookmod.HasoookMod;
import com.hasoook.hasoookmod.item.ModItems;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

import java.util.Objects;
import java.util.Set;

@EventBusSubscriber(modid = HasoookMod.MOD_ID)
public class EntityEnchantmentInteract {
    @SubscribeEvent
    public static void onPlayerInteractEntity(PlayerInteractEvent.EntityInteract event) {
        if (!event.getLevel().isClientSide) {
            ItemStack itemStack = event.getEntity().getMainHandItem();

            if (itemStack.getItem() == ModItems.ENCHANTMENT_BRUSH.get()) {
                Player player = event.getEntity();
                Entity target = event.getTarget();

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

                if (target instanceof LivingEntity livingEntity) {
                    int powerLevel = EntityEnchantmentHelper.getEnchantmentLevel(target,"minecraft:power");
                    int unbreakingLevel = EntityEnchantmentHelper.getEnchantmentLevel(target,"minecraft:unbreaking");

                    // 获取实体的属性实例
                    AttributeInstance scaleAttr = Objects.requireNonNull(livingEntity.getAttribute(Attributes.SCALE));
                    AttributeInstance attackDamageAttr = Objects.requireNonNull(livingEntity.getAttribute(Attributes.ATTACK_DAMAGE));
                    AttributeInstance maxHealthAttr = Objects.requireNonNull(livingEntity.getAttribute(Attributes.MAX_HEALTH));

                    // 处理力量相关的属性，ee_power中的ee的意思为EntityEnchant，为方便区分
                    scaleAttr.removeModifier(ResourceLocation.withDefaultNamespace("ee_power"));
                    attackDamageAttr.removeModifier(ResourceLocation.withDefaultNamespace("ee_power2"));
                    if (powerLevel > 0) {
                        scaleAttr.addPermanentModifier(new AttributeModifier(ResourceLocation.withDefaultNamespace("ee_power"), powerLevel * 0.05, AttributeModifier.Operation.ADD_VALUE));
                        attackDamageAttr.addPermanentModifier(new AttributeModifier(ResourceLocation.withDefaultNamespace("ee_power2"), powerLevel, AttributeModifier.Operation.ADD_VALUE));
                    }

                    // 处理耐久相关的属性
                    maxHealthAttr.removeModifier(ResourceLocation.withDefaultNamespace("ee_unbreaking"));
                    if (unbreakingLevel > 0) {
                        maxHealthAttr.addPermanentModifier(new AttributeModifier(ResourceLocation.withDefaultNamespace("ee_unbreaking"), unbreakingLevel * livingEntity.getMaxHealth() * 0.2, AttributeModifier.Operation.ADD_VALUE));
                        livingEntity.setHealth(livingEntity.getHealth() + unbreakingLevel * livingEntity.getMaxHealth() * 0.2F);
                    }

                }

                if (event.getLevel() instanceof ServerLevel serverlevel) {
                    if (!itemEnchantments.isEmpty()) {
                        serverlevel.sendParticles(ParticleTypes.WITCH, target.getX(), target.getY() + target.getBbHeight() / 2, target.getZ(), 5, target.getBbWidth() / 2.2, target.getBbHeight() / 2.5, target.getBbWidth() / 2.2, 0.0);
                        serverlevel.sendParticles(ParticleTypes.EFFECT, target.getX(), target.getY() + target.getBbHeight() / 2, target.getZ(), 2, target.getBbWidth() / 3, target.getBbHeight() / 3, target.getBbWidth() / 3, 0.0);
                    } else {
                        serverlevel.sendParticles(ParticleTypes.DUST_PLUME, target.getX(), target.getY() + target.getBbHeight() / 2.5, target.getZ(), 5, target.getBbWidth() / 3, target.getBbHeight() / 3, target.getBbWidth() / 3, 0.0);
                        serverlevel.sendParticles(ParticleTypes.SMOKE, target.getX(), target.getY() + target.getBbHeight() / 2.5, target.getZ(), 3, target.getBbWidth() / 3, target.getBbHeight() / 3, target.getBbWidth() / 3, 0.0);
                    }
                }

                player.swing(InteractionHand.MAIN_HAND,true); // 摇动玩家的主手
                event.setCanceled(true); // 取消交互事件
            }
        }

    }
}
