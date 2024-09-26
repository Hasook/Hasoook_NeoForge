package com.hasoook.hasoookmod.item.custom;

import com.hasoook.hasoookmod.entity.custom.PokerProjectileEntity;
import com.hasoook.hasoookmod.item.ModItems;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;

import java.util.Random;

public class Poker extends Item {
    public Poker(Properties pProperties) {
        super(pProperties.stacksTo(1).durability(54));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pHand) {
        ItemStack itemstack = pPlayer.getItemInHand(pHand);
        pLevel.playSound(null, pPlayer.getX(), pPlayer.getY(), pPlayer.getZ(), SoundEvents.SNOWBALL_THROW, SoundSource.NEUTRAL, 0.5F, 0.4F / (pLevel.getRandom().nextFloat() * 0.4F + 0.8F));

        if (!pLevel.isClientSide) {
            int deck1Value = itemstack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getInt("Deck1");
            if (deck1Value < 1) {
                initializeDeck(itemstack, pPlayer); // 初始化牌堆
                pPlayer.displayClientMessage(Component.nullToEmpty("[ 洗牌 ]"), true);
                pPlayer.getCooldowns().addCooldown(this, 10);
            } else {
                int amount = 1; // 即将消耗的耐久值
                if (areAllSuitsEqual(itemstack)) { // 检查是否所有花色相同
                    throwPokerCard(pLevel, pPlayer, deck1Value, itemstack, 4,0, true, false); // 发射扑克牌
                    initializeDeck(itemstack, pPlayer);
                    amount = 4;
                } else if (areAllSuitsDifferent(itemstack)) {
                    // 发射四张不同花色的扑克牌
                    for (int i = 1; i <= 4; i++) {
                        ItemStack differentPokerCard = getPokerCard(i);
                        PokerProjectileEntity differentPokerProjectile = new PokerProjectileEntity(pLevel, pPlayer, 4, 0, false, true);
                        differentPokerProjectile.setItem(differentPokerCard);
                        differentPokerProjectile.shootFromRotation(pPlayer, pPlayer.getXRot(), pPlayer.getYRot(), 0.0F, 1.8F, 2.0F + i);
                        pLevel.addFreshEntity(differentPokerProjectile);
                    }
                    initializeDeck(itemstack, pPlayer);
                    amount = 4;
                } else {
                    throwPokerCard(pLevel, pPlayer, deck1Value, itemstack, 1, 0, false, false); // 发射扑克牌
                    updateDeckValues(itemstack); // 更新牌堆
                }

                // 更新耐久
                EquipmentSlot equipmentslot = itemstack.equals(pPlayer.getItemBySlot(EquipmentSlot.OFFHAND))
                        ? EquipmentSlot.OFFHAND
                        : EquipmentSlot.MAINHAND;
                itemstack.hurtAndBreak(amount, pPlayer, equipmentslot);
                pPlayer.getCooldowns().addCooldown(this, 5);
            }
        }
        pPlayer.awardStat(Stats.ITEM_USED.get(this));
        return InteractionResultHolder.sidedSuccess(itemstack, pLevel.isClientSide());
    }

    public void inventoryTick(ItemStack pStack, Level pLevel, Entity pEntity, int pSlotId, boolean pIsSelected) {
        Player player = (Player) pEntity;
        if (player.getCooldowns().getCooldownPercent(pStack.getItem(),0) == 0 && pIsSelected) {
            // 获取当前的牌组值
            int deck1Value = pStack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getInt("Deck1");
            int deck2Value = pStack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getInt("Deck2");
            int deck3Value = pStack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getInt("Deck3");
            int deck4Value = pStack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getInt("Deck4");

            // 构建消息
            String message = "[" + getCardName(deck1Value) + " §f, " +
                    getCardName(deck2Value) + " §f, " +
                    getCardName(deck3Value) + " §f, " +
                    getCardName(deck4Value) + "§f]";

            // 发送消息给玩家
            player.displayClientMessage(Component.nullToEmpty(message), true);
        }
    }

    // 从数值获取符号
    private String getCardName(int deckValue) {
        return switch (deckValue) {
            case 1 -> "§4♥";
            case 2 -> "§4♦";
            case 3 -> "§0♠";
            case 4 -> "§0♣";
            default -> "§k♥"; // 默认值
        };
    }

    // 检查四张牌是否为同一花色
    private boolean areAllSuitsEqual(ItemStack itemStack) {
        int deck1Value = itemStack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getInt("Deck1");
        int deck2Value = itemStack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getInt("Deck2");
        int deck3Value = itemStack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getInt("Deck3");
        int deck4Value = itemStack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getInt("Deck4");

        return deck1Value == deck2Value && deck2Value == deck3Value && deck3Value == deck4Value;
    }

    // 检查四张牌是否都不同
    private boolean areAllSuitsDifferent(ItemStack itemStack) {
        int deck1Value = itemStack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getInt("Deck1");
        int deck2Value = itemStack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getInt("Deck2");
        int deck3Value = itemStack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getInt("Deck3");
        int deck4Value = itemStack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getInt("Deck4");

        return deck1Value != deck2Value && deck1Value != deck3Value && deck1Value != deck4Value &&
                deck2Value != deck3Value && deck2Value != deck4Value &&
                deck3Value != deck4Value;
    }

    // 初始化牌组
    private void initializeDeck(ItemStack itemStack, Player player) {
        Random random = new Random();
        String[] decks = {"Deck1", "Deck2", "Deck3", "Deck4"};

        CustomData.update(DataComponents.CUSTOM_DATA, itemStack, tag -> {
            for (String deck : decks) {
                int randomValue = random.nextInt(4) + 1; // 随机花色
                tag.putDouble(deck, randomValue);
            }
        });
    }

    // 发射扑克牌
    private void throwPokerCard(Level level, Player player, int deck1Value, ItemStack itemStack, int hurt, int firetick, boolean isExplosive, boolean isPiercing) {
        ItemStack pokerCard = getPokerCard(deck1Value);

        // 创建并发射扑克实体
        PokerProjectileEntity pokerProjectile = new PokerProjectileEntity(level, player, hurt, firetick, isExplosive, isPiercing);
        pokerProjectile.setItem(pokerCard);
        pokerProjectile.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 1.8F, 1.0F);
        level.addFreshEntity(pokerProjectile);
    }

    // 根据 Deck1 的值获取对应的扑克
    private ItemStack getPokerCard(int deck1Value) {
        return switch (deck1Value) {
            case 1 -> ModItems.POKER_HEART.get().getDefaultInstance();
            case 2 -> ModItems.POKER_DIAMOND.get().getDefaultInstance();
            case 3 -> ModItems.POKER_SPADE.get().getDefaultInstance();
            case 4 -> ModItems.POKER_CLUB.get().getDefaultInstance();
            default -> ItemStack.EMPTY; // 默认值
        };
    }

    // 更新牌组的值
    private void updateDeckValues(ItemStack itemStack) {
        Random random = new Random();

        // 读取当前的牌组值
        int deck1Value = itemStack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY)
                .copyTag()
                .getInt("Deck1");
        int deck2Value = itemStack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY)
                .copyTag()
                .getInt("Deck2");
        int deck3Value = itemStack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY)
                .copyTag()
                .getInt("Deck3");
        int deck4Value = itemStack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY)
                .copyTag()
                .getInt("Deck4");

        // 随机生成新值用于 Deck4
        int newDeck4Value = random.nextInt(4) + 1;

        // 更新牌组，Deck1 和 Deck2 前移，Deck3 接收 Deck4 的值
        CustomData.update(DataComponents.CUSTOM_DATA, itemStack, tag -> {
            tag.putDouble("Deck1", deck2Value);
            tag.putDouble("Deck2", deck3Value);
            tag.putDouble("Deck3", deck4Value);
            tag.putDouble("Deck4", newDeck4Value); // Deck4 更新为新随机值
        });
    }
}
