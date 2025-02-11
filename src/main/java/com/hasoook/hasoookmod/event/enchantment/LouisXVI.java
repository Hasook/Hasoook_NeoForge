package com.hasoook.hasoookmod.event.enchantment;

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
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec2;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingEquipmentChangeEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

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

        if (!louisXvi && enchantmentLevel > 0 && entity instanceof LivingEntity) {
            handleLouisXVIInteraction(event.getHand(), player, entity, persistentData, itemStack);
        } else if (louisXvi) {
            headReversion(event.getHand(), player, entity, persistentData, itemStack);
        }
    }

    /**
     * 玩家自己剪头
     */
    @SubscribeEvent
    public static void onPlayerRightClickAir(PlayerInteractEvent.RightClickItem event) {
        Player player = event.getEntity();
        ItemStack itemStack = event.getItemStack();
        CompoundTag persistentData = player.getPersistentData();
        boolean louisXvi = persistentData.getBoolean("louis_xvi");

        // 检查玩家是否潜行且手持附魔物品
        if (player.isCrouching() && ModEnchantmentHelper.getEnchantmentLevel(ModEnchantments.LOUIS_XVI, itemStack) > 0 && !louisXvi) {
            handleLouisXVIInteraction(event.getHand(), player, player, persistentData, itemStack);
        } else if (louisXvi && player.isCrouching()) {
            headReversion(event.getHand(), player, player, persistentData, itemStack);
        }
    }

    /**
     * 处理造成伤害时剪头的方法
     */
    /*@SubscribeEvent
    public static void onEntityAttack(LivingIncomingDamageEvent event) {
        Entity source = event.getSource().getEntity();
        Entity entity = event.getEntity();

        if (source instanceof LivingEntity sourceEntity) {
            ItemStack itemStack = sourceEntity.getItemInHand(InteractionHand.MAIN_HAND);
            int lvl = ModEnchantmentHelper.getEnchantmentLevel(ModEnchantments.LOUIS_XVI, itemStack);
            CompoundTag persistentData = entity.getPersistentData();
            boolean louisXvi = persistentData.getBoolean("louis_xvi");
            if (lvl > 0 && !louisXvi) {
                handleLouisXVIInteraction(null, null, entity, persistentData, itemStack);
            }
        }
    }*/

    /**
     * 处理剪头交互逻辑
     */
    private static void handleLouisXVIInteraction(InteractionHand hand, Player player, Entity entity, CompoundTag persistentData, ItemStack itemStack) {
        persistentData.putBoolean("louis_xvi", true);

        // 向客户端发包
        if (!entity.level().isClientSide) {
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

        itemStack.hurtAndBreak(1, player, hand == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND);
        player.swing(hand);
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
        if (profile != null) {
            profile.name().ifPresent(name -> {
                if (isNameMatch(entity, name)) {
                    persistentData.putBoolean("louis_xvi", false);
                    PacketDistributor.sendToAllPlayers(new LouisXVIS2CPacket(entity.getId(), false));
                    player.swing(hand);
                    if (!player.isCreative()) {
                        itemStack.shrink(1);
                    }
                }
            });
        }
    }

    /**
     * 处理头部物品槽的逻辑
     */
    @SubscribeEvent
    private static void LivingEquipmentChange(LivingEquipmentChangeEvent event) {
        LivingEntity entity = event.getEntity();
        ItemStack toItem = event.getTo(); // 现在的装备
        EquipmentSlot slot = event.getSlot(); // 获取槽位
        CompoundTag persistentData = entity.getPersistentData();
        boolean louisXvi = persistentData.getBoolean("louis_xvi");

        if (louisXvi && slot.getSerializedName().equalsIgnoreCase("HEAD")) {
            ResolvableProfile profile = toItem.get(DataComponents.PROFILE);
            if (profile != null) {
                profile.name().ifPresent(name -> {
                    if (isNameMatch(entity, name)) {
                        persistentData.putBoolean("louis_xvi", false);
                        PacketDistributor.sendToAllPlayers(new LouisXVIS2CPacket(entity.getId(), false));
                        toItem.shrink(1);
                    }
                });
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
     * 网络发包
     */
    @SubscribeEvent
    public static void playerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (!player.level().isClientSide && player.level() instanceof ServerLevel serverLevel) {
            double range = 16.0;
            AABB area = player.getBoundingBox().inflate(range);
            serverLevel.getEntitiesOfClass(LivingEntity.class, area, entity -> entity.getPersistentData().getBoolean("louis_xvi") && entity.isAlive())
                    .stream()
                    .filter(player::hasLineOfSight)
                    .forEach(mob -> PacketDistributor.sendToPlayer((ServerPlayer) player, new LouisXVIS2CPacket(mob.getId(), true)));
        }
    }
}
