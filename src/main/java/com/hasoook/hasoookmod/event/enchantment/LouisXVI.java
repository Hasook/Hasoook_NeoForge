package com.hasoook.hasoookmod.event.enchantment;

import com.hasoook.hasoookmod.Config;
import com.hasoook.hasoookmod.HasoookMod;
import com.hasoook.hasoookmod.enchantment.ModEnchantmentHelper;
import com.hasoook.hasoookmod.enchantment.ModEnchantments;
import com.hasoook.hasoookmod.net.LouisXVIS2CPacket;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec2;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingEquipmentChangeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.*;

@EventBusSubscriber(modid = HasoookMod.MOD_ID)
public class LouisXVI {
    private static final Random RANDOM = new Random();
    private static final Map<String, String> ENTITY_HEAD_ID = new HashMap<>();

    static {
        // 生物ID映射头颅ID
        ENTITY_HEAD_ID.put("minecraft:snow_golem", "chops16");
        ENTITY_HEAD_ID.put("minecraft:iron_golem", "MHF_Golem");
        ENTITY_HEAD_ID.put("minecraft:cave_spider", "MHF_WSkeleton");
        ENTITY_HEAD_ID.put("minecraft:wandering_trader", "Wandering_Trader");
        ENTITY_HEAD_ID.put("minecraft:skeleton_horse", "MHF_horse");
        ENTITY_HEAD_ID.put("minecraft:llama", "kamallamakk");
        ENTITY_HEAD_ID.put("minecraft:frog", "Cosmic_void1");
        ENTITY_HEAD_ID.put("minecraft:phantom", "RAZUMIK");
        ENTITY_HEAD_ID.put("minecraft:zombie_villager", "Golem2323");
        ENTITY_HEAD_ID.put("minecraft:zombified_piglin", "ZombiePiglin");
        ENTITY_HEAD_ID.put("minecraft:armadillo", "MHF_Present1");
        ENTITY_HEAD_ID.put("minecraft:wither_skeleton", "MHF_WSkeleton");
        ENTITY_HEAD_ID.put("minecraft:magma_cube", "MHF_LavaSlime");
        ENTITY_HEAD_ID.put("minecraft:piglin_brute", "MHF_Piglin");
    }

    /**
     * 互动实体剪头
     */
    @SubscribeEvent
    public static void onPlayerInteractEntity(PlayerInteractEvent.EntityInteract event) {
        ItemStack itemStack = event.getItemStack();
        Player player = event.getEntity();
        Entity entity = event.getTarget();
        CompoundTag persistentData = entity.getPersistentData();
        boolean louisXvi = persistentData.getBoolean("louis_xvi");
        int enchantmentLevel = ModEnchantmentHelper.getEnchantmentLevel(ModEnchantments.LOUIS_XVI, itemStack);

        if (entity instanceof LivingEntity livingEntity) {
            if (!louisXvi && enchantmentLevel > 0) {
                handleLouisXVIInteraction(event.getHand(), player, livingEntity, persistentData, itemStack);
            } else if (louisXvi) {
                headReversion(event.getHand(), player, livingEntity, persistentData, itemStack);
            }
        }
    }

    /**
     * 玩家右键使用物品的逻辑（自己剪头、自己装头、限制使用头盔）
     */
    @SubscribeEvent
    public static void onPlayerRightClickAir(PlayerInteractEvent.RightClickItem event) {
        Player player = event.getEntity();
        ItemStack itemStack = event.getItemStack();
        CompoundTag persistentData = player.getPersistentData();
        boolean louisXvi = persistentData.getBoolean("louis_xvi");

        // 检查玩家是否潜行且手持附魔物品，而且不是剪头状态，则剪头，
        if (player.isCrouching() && ModEnchantmentHelper.getEnchantmentLevel(ModEnchantments.LOUIS_XVI, itemStack) > 0 && !louisXvi) {
            handleLouisXVIInteraction(event.getHand(), player, player, persistentData, itemStack);
        } else if (louisXvi && player.isCrouching()) {
            // 如果物品是玩家自己的头，则装头
            headReversion(event.getHand(), player, player, persistentData, itemStack);
        }

        // 限制使用头部物品槽
        if (Config.louisXviHead && itemStack.getItem() instanceof ArmorItem armorItem && armorItem.getEquipmentSlot() == EquipmentSlot.HEAD) {
            if (player.getPersistentData().getBoolean("louis_xvi")) {
                event.setCanceled(true); // 取消事件
                player.displayClientMessage(Component.literal("§c当前状态无法使用头盔槽！"), true);
            }
        }
    }

    /**
     * 处理剪头交互逻辑
     */
    private static void handleLouisXVIInteraction(InteractionHand hand, Player player, LivingEntity entity, CompoundTag persistentData, ItemStack itemStack) {
        persistentData.putBoolean("louis_xvi", true);

        // 确保只在服务器端发送数据包
        if (!entity.level().isClientSide()) {
            PacketDistributor.sendToAllPlayers(new LouisXVIS2CPacket(entity.getId(), true));
        }

        if (entity.level() instanceof ServerLevel serverLevel) {
            String profileId = getProfileId(entity);
            double motionX = (RANDOM.nextDouble() - 0.5) * 0.2;
            double motionY = 0.2;
            double motionZ = (RANDOM.nextDouble() - 0.5) * 0.2;

            String command = String.format(
                    "summon item ~ ~ ~ {Item:{id:\"minecraft:player_head\",Count:1,components:{\"minecraft:profile\":{name:\"%s\"}}},Motion:[%.2f,%.2f,%.2f],PickupDelay:20}",
                    profileId, motionX, motionY, motionZ
            );

            serverLevel.getServer().getCommands().performPrefixedCommand(
                    new CommandSourceStack(CommandSource.NULL, entity.position().add(0, entity.getEyeHeight(), 0),
                            Vec2.ZERO, serverLevel, 4, "", Component.literal(""), serverLevel.getServer(), null
                    ).withSuppressedOutput(),
                    command
            );
        }

        // 生成头盔槽物品的掉落物
        ItemStack headItem = entity.getItemBySlot(EquipmentSlot.HEAD);
        if (!headItem.isEmpty() && Config.louisXviHead) {
            // 检查玩家背包是否有空间
            if (player.getInventory().getFreeSlot() == -1) {
                player.drop(headItem, false); // 如果背包已满，掉落物品
            } else {
                player.getInventory().add(headItem); // 如果背包有空间，将物品放入背包
            }
            entity.setItemSlot(EquipmentSlot.HEAD, ItemStack.EMPTY);
        }

        int efficiencyLevel = ModEnchantmentHelper.getEnchantmentLevel(Enchantments.EFFICIENCY, itemStack);
        itemStack.hurtAndBreak(16, player, hand == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND);
        player.swing(hand); // 挥动手臂
        player.getCooldowns().addCooldown(itemStack.getItem(), 320 / (1 + efficiencyLevel)); // 冷却时间
        entity.gameEvent(GameEvent.SHEAR, player);
        entity.playSound(SoundEvents.SHEEP_SHEAR, 1.0f, 1.0f);
    }

    /**
     * 获取生物对应的 MHF 头颅 ID
     */
    private static String getProfileId(Entity entity) {
        if (entity instanceof Player) {
            // 如果是玩家，直接返回玩家的名字
            return entity.getName().getString();
        } else {
            // 如果是其他生物，从映射表中获取头颅ID
            String entityId = Objects.requireNonNull(entity.getEncodeId());
            String mhfHeadId = ENTITY_HEAD_ID.get(entityId);

            // 如果映射表中没有对应的ID，则按照MHF格式生成一个
            if (mhfHeadId == null) {
                String[] idParts = entityId.split(":");
                if (idParts.length > 1) {
                    mhfHeadId = "MHF_" + idParts[1].substring(0, 1).toUpperCase() + idParts[1].substring(1);
                }
            }
            return mhfHeadId;
        }
    }

    /**
     * 头颅还原
     */
    private static void headReversion(InteractionHand hand, Player player, Entity entity, CompoundTag persistentData, ItemStack itemStack) {
        ResolvableProfile profile = itemStack.get(DataComponents.PROFILE);
        if (profile != null && !itemStack.isEmpty()) {
            profile.name().ifPresent(name -> {
                if (isNameMatch(entity, name)) {
                    persistentData.putBoolean("louis_xvi", false);

                    // 确保只在服务器端发送数据包
                    if (!entity.level().isClientSide()) {
                        PacketDistributor.sendToAllPlayers(new LouisXVIS2CPacket(entity.getId(), false));
                    }

                    player.swing(hand);
                    if (!player.isCreative()) {
                        itemStack.shrink(1);
                    }
                }
            });
        }
    }

    /**
     * 装备更换时的逻辑（从装备栏装头、限制头部物品槽）
     */
    @SubscribeEvent
    public static void onLivingEquipmentChange(LivingEquipmentChangeEvent event) {
        LivingEntity entity = event.getEntity();
        CompoundTag persistentData = entity.getPersistentData();
        EquipmentSlot slot = event.getSlot();

        if (slot == EquipmentSlot.HEAD && persistentData.getBoolean("louis_xvi")) {
            ItemStack toItem = event.getTo();
            ResolvableProfile profile = toItem.get(DataComponents.PROFILE);

            // 处理还原逻辑（当装备了匹配的头颅时）
            if (!toItem.isEmpty() && profile != null) {
                Optional<String> nameOpt = profile.name();
                if (nameOpt.isPresent() && isNameMatch(entity, nameOpt.get())) {
                    toItem.setCount(0);
                    persistentData.putBoolean("louis_xvi", false);
                    if (!entity.level().isClientSide()) {
                        PacketDistributor.sendToAllPlayers(new LouisXVIS2CPacket(entity.getId(), false));
                    }
                }
            }

            // 阻止更换头部装备
            if (persistentData.getBoolean("louis_xvi") && entity instanceof Player player && Config.louisXviHead && !toItem.isEmpty()) {
                ItemStack fromItem = event.getFrom();
                ItemStack toItemStack = event.getTo();

                // 撤销更换
                player.setItemSlot(EquipmentSlot.HEAD, fromItem);

                // 退回新物品到背包或掉落
                if (!toItemStack.isEmpty()) {
                    if (!player.getInventory().add(toItemStack)) {
                        player.drop(toItemStack, false);
                    }
                }

                player.displayClientMessage(Component.literal("§c当前状态无法使用头盔槽！"), true);
            }
        }
    }

    /**
     * 比较名称是否匹配
     */
    private static boolean isNameMatch(Entity entity, String name) {
        String profileId = getProfileId(entity);
        return name.equalsIgnoreCase(profileId);
    }

    /**
     * 网络发包，向客户端发送生物的剪头状态
     */
    @SubscribeEvent
    public static void playerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (!player.level().isClientSide() && player.level() instanceof ServerLevel serverLevel) {
            double range = 16.0;
            AABB area = player.getBoundingBox().inflate(range);
            serverLevel.getEntitiesOfClass(LivingEntity.class, area, entity -> entity.getPersistentData().getBoolean("louis_xvi") && entity.isAlive())
                    .stream()
                    .filter(player::hasLineOfSight)
                    .forEach(mob -> PacketDistributor.sendToPlayer((ServerPlayer) player, new LouisXVIS2CPacket(mob.getId(), true)));
        }
        if (player.getPersistentData().getBoolean("louis_xvi") && Config.louisXviBlindness) {
            player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 32, 15));
        }
    }
}