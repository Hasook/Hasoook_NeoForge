package com.hasoook.hasoookmod.client.render;

import com.hasoook.hasoookmod.HasoookMod;
import com.hasoook.hasoookmod.item.custom.InsightMagnifier;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

import java.util.*;

@EventBusSubscriber(modid = HasoookMod.MOD_ID, value = Dist.CLIENT)
public class InsightMagnifierHandler {
    private static final Map<EntityType<?>, Double> HEALTH_CACHE = new HashMap<>();

    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;

        // 检查是否手持放大镜
        boolean isHoldingMagnifier = player.getMainHandItem().getItem() instanceof InsightMagnifier || player.getOffhandItem().getItem() instanceof InsightMagnifier;

        if (isHoldingMagnifier) {
            ItemStack stack = event.getItemStack();

            // 处理普通物品的提示
            handleItemLore(event, stack);

            // 处理附魔书
            if (stack.getItem() instanceof EnchantedBookItem) {
                handleEnchantments(event, stack);
            }

            handleFoodInfo(event, stack);

            handleSpawnEggInfo(event, stack);
        }
    }

    private static void handleItemLore(ItemTooltipEvent event, ItemStack stack) {
        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
        String loreKey = "item." + itemId.getNamespace() + "." + itemId.getPath() + ".insight";
        addTranslatedLines(event, loreKey);
    }

    private static void handleEnchantments(ItemTooltipEvent event, ItemStack stack) {
        ItemEnchantments enchantments = stack.get(DataComponents.ENCHANTMENTS);
        if (stack.getItem() instanceof EnchantedBookItem) {
            enchantments = stack.get(DataComponents.STORED_ENCHANTMENTS);
        }
        if (enchantments == null || enchantments.size() != 1) return;

        for (var entry : enchantments.entrySet()) {
            Holder<Enchantment> holder = entry.getKey();
            Enchantment enchantment = holder.value();

            // 原有附魔描述处理
            ResourceLocation enchantId = Objects.requireNonNull(holder.getKey()).location();
            String enchantLoreKey = "enchantment." + enchantId.getNamespace() + "." + enchantId.getPath() + ".desc";
            addTranslatedLines(event, enchantLoreKey);

            HolderSet<Item> supportedItems = enchantment.definition().supportedItems();
            Set<String> displayNames = new LinkedHashSet<>();

            // 收集适用物品显示名称
            for (Holder<Item> itemHolder : supportedItems) {
                Set<String> translatedTags = new LinkedHashSet<>();

                // 优先使用标签翻译
                itemHolder.tags().forEach(tagKey -> {
                    String translationKey = String.format("hasoook.tooltip.tag.%s.%s",
                            tagKey.location().getNamespace(),
                            tagKey.location().getPath().replace('/', '.'));
                    String localized = Component.translatable(translationKey).getString();
                    if (!localized.equals(translationKey)) {
                        translatedTags.add(localized);
                    }
                });

                if (!translatedTags.isEmpty()) {
                    displayNames.addAll(translatedTags);
                } else {
                    displayNames.add(
                            Component.translatable(itemHolder.value().getDescriptionId()).getString()
                    );
                }
            }

            if (!displayNames.isEmpty()) {
                boolean isShiftDown = Screen.hasShiftDown();
                int maxDisplay = isShiftDown ? Integer.MAX_VALUE : 6; // 显示上限
                boolean hasMore = displayNames.size() > maxDisplay;

                List<String> displayList = new ArrayList<>(displayNames);
                List<String> toShow = displayList.subList(0, Math.min(maxDisplay, displayList.size()));

                // 构建显示文本
                MutableComponent itemsText = Component.literal("");
                for (int i = 0; i < toShow.size(); i++) {
                    itemsText.append(toShow.get(i));
                    if (i < toShow.size() - 1) {
                        itemsText.append(", ");
                    }
                }

                // 添加省略提示
                if (!isShiftDown && hasMore) {
                    itemsText.append(
                            Component.translatable("hasoook.tooltip.etc")
                                    .withStyle(ChatFormatting.DARK_GRAY)
                    );
                }

                // 添加到工具提示
                event.getToolTip().add(
                        Component.translatable("hasoook.tooltip.applicable_items")
                                .withStyle(ChatFormatting.DARK_GRAY)
                );
                event.getToolTip().add(itemsText.withStyle(ChatFormatting.DARK_GRAY));
            }
        }
    }

    private static void handleFoodInfo(ItemTooltipEvent event, ItemStack stack) {
        FoodProperties food = stack.get(DataComponents.FOOD);
        if (food == null) return;

        List<Component> tooltip = event.getToolTip();

        // 原有营养和饱和度显示
        tooltip.add(Component.translatable("hasoook.tooltip.nutrition", food.nutrition())
                .withStyle(ChatFormatting.DARK_GRAY));
        tooltip.add(Component.translatable("hasoook.tooltip.saturation", String.format("%.1f", food.saturation()))
                .withStyle(ChatFormatting.DARK_GRAY));

        // 新增效果显示（优化概率显示）
        List<FoodProperties.PossibleEffect> effects = food.effects();
        if (!effects.isEmpty()) {
            for (FoodProperties.PossibleEffect entry : effects) {
                MobEffectInstance effect = entry.effect();
                float probability = entry.probability(); // 获取概率

                // 获取效果名称
                String translationKey = "effect." + effect.getEffect().getRegisteredName().replace(':', '.');
                MutableComponent effectName = Component.translatable(translationKey);
                // 获取效果信息
                String amplifier = getRomanNumber(effect.getAmplifier() + 1);
                String duration = getFormattedDuration(effect.getDuration());

                // 构建显示文本
                MutableComponent line;
                if (probability == 1.0F) { // 精确判断100%概率
                    line = Component.translatable("hasoook.tooltip.effect_entry.100",
                            effectName,
                            amplifier,
                            duration);
                } else {
                    int percent = Math.round(probability * 100);
                    line = Component.translatable("hasoook.tooltip.effect_entry",
                            percent,
                            effectName,
                            amplifier,
                            duration);
                }
                tooltip.add(line.withStyle(ChatFormatting.DARK_GRAY));
            }
        }
    }

    // 将数字转换为罗马数字
    private static String getRomanNumber(int number) {
        return switch (number) {
            case 1 -> "I";
            case 2 -> "II";
            case 3 -> "III";
            case 4 -> "IV";
            case 5 -> "V";
            default -> Integer.toString(number);
        };
    }

    // 辅助方法：格式化持续时间
    private static String getFormattedDuration(int ticks) {
        int seconds = ticks / 20;
        int minutes = seconds / 60;
        seconds %= 60;

        return String.format("%02d:%02d", minutes, seconds);
    }

    private static void handleSpawnEggInfo(ItemTooltipEvent event, ItemStack stack) {
        if (!(stack.getItem() instanceof SpawnEggItem)) return;

        // 获取实体类型
        EntityType<?> entityType = ((SpawnEggItem) stack.getItem()).getType(stack);

        // 从缓存中获取血量
        Double baseHealth = HEALTH_CACHE.get(entityType);

        // 如果缓存中没有，则计算并缓存
        if (baseHealth == null) {
            if (HEALTH_CACHE.size() > 100) {
                HEALTH_CACHE.clear(); // 如果大于100则清理缓存
            }

            Level level = Minecraft.getInstance().level;
            if (level == null) return;

            Entity entity = entityType.create(level);
            if (entity instanceof LivingEntity livingEntity) {
                AttributeInstance healthAttribute = livingEntity.getAttribute(Attributes.MAX_HEALTH);
                if (healthAttribute != null) {
                    baseHealth = healthAttribute.getBaseValue();
                    HEALTH_CACHE.put(entityType, baseHealth);
                }
            }
        }

        if (baseHealth != null && baseHealth > 0) {
            event.getToolTip().add(
                    Component.translatable("hasoook.tooltip.spawn_egg_health",
                                    String.format("%.1f", baseHealth))
                            .withStyle(ChatFormatting.DARK_GRAY)
            );
        }
    }

    private static void addTranslatedLines(ItemTooltipEvent event, String loreKey) {
        MutableComponent loreText = Component.translatable(loreKey);
        String translatedText = loreText.getString();

        if (!translatedText.equals(loreKey)) {
            String[] lines = translatedText.split("\n");
            for (String line : lines) {
                event.getToolTip().add(
                        Component.literal(line).withStyle(ChatFormatting.GRAY)
                );
            }
        }
    }
}
