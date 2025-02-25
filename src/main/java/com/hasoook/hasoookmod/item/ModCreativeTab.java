package com.hasoook.hasoookmod.item;

import com.hasoook.hasoookmod.HasoookMod;
import com.hasoook.hasoookmod.block.ModBlock;
import com.hasoook.hasoookmod.enchantment.ModEnchantments;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModCreativeTab {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, HasoookMod.MOD_ID);
    // 这个string是鼠标移动到tab上的显示的内容。
    public static final String HASOOOK_MOD_TAB_STRING = "creativetab.hasoook_tab";
    // 添加一个tab，title标题，icon显示图标，displayItem是指tab中添加的内容，这里传入一个lammabd表达式，通过poutput添加物品
    public static final Supplier<CreativeModeTab> EXAMPLE_TAB  = CREATIVE_MODE_TABS.register("example_tab",() -> CreativeModeTab.builder()
            .withTabsBefore(CreativeModeTabs.COMBAT)
            .title(Component.translatable(HASOOOK_MOD_TAB_STRING))
            .icon(Items.ENCHANTED_BOOK::getDefaultInstance)
            .displayItems((pParameters, pOutput) -> {
                // 添加自定义的物品
                pOutput.accept(ModItems.TOTEM_OF_SURRENDER.get());
                pOutput.accept(ModItems.ENCHANTMENT_BRUSH.get());
                pOutput.accept(ModItems.WATER_BUCKET.get());
                pOutput.accept(ModItems.HUGE_DIAMOND_PICKAXE.get());
                pOutput.accept(ModItems.WATER_BOOTS.get());
                pOutput.accept(ModItems.SPIT.get());
                pOutput.accept(ModItems.RIPEN_FLINT_AND_STEEL.get());
                pOutput.accept(ModItems.PET_COMPASS.get());
                pOutput.accept(ModItems.DISK_CRICKET_MOTION_BADGE.get());

                // 添加自定义的方块
                pOutput.accept(ModBlock.CONFUSION_FLOWER.get());
                pOutput.accept(ModBlock.HASOOOK_LUCKY_BLOCK.get());
                pOutput.accept(ModBlock.GREEN_SCREEN_BLOCK.get());

                // 获取附魔注册表查询接口
                HolderGetter<Enchantment> enchantments = pParameters.holders().lookupOrThrow(Registries.ENCHANTMENT);

                // 添加自定义附魔的附魔书
                addMaxLevelEnchantedBook(pOutput, enchantments, ModEnchantments.RANDOM_BULLETS);
                addMaxLevelEnchantedBook(pOutput, enchantments, ModEnchantments.SEPARATION_EXPLOSION);
                addMaxLevelEnchantedBook(pOutput, enchantments, ModEnchantments.DISDAIN);
                addMaxLevelEnchantedBook(pOutput, enchantments, ModEnchantments.SWAP);
                addMaxLevelEnchantedBook(pOutput, enchantments, ModEnchantments.CHAIN_DAMAGE);
                addMaxLevelEnchantedBook(pOutput, enchantments, ModEnchantments.GIVE);
                addMaxLevelEnchantedBook(pOutput, enchantments, ModEnchantments.UNYIELDING);
                addMaxLevelEnchantedBook(pOutput, enchantments, ModEnchantments.SEVEN_STEP_SNAKE_VENOM);
                addMaxLevelEnchantedBook(pOutput, enchantments, ModEnchantments.KILL_A_MAN_EVERY_TEN_PACES);
                addMaxLevelEnchantedBook(pOutput, enchantments, ModEnchantments.BACKSTAB);
                addMaxLevelEnchantedBook(pOutput, enchantments, ModEnchantments.BETRAY);
                addMaxLevelEnchantedBook(pOutput, enchantments, ModEnchantments.RACIAL_DISCRIMINATION);
                addMaxLevelEnchantedBook(pOutput, enchantments, ModEnchantments.MIDDLE_EAST_BEST_PILOT);
                addMaxLevelEnchantedBook(pOutput, enchantments, ModEnchantments.DE_URBANIZATION);
                addMaxLevelEnchantedBook(pOutput, enchantments, ModEnchantments.SPOTLIGHT);
                addMaxLevelEnchantedBook(pOutput, enchantments, ModEnchantments.ZERO_COST_PURCHASE);
                addMaxLevelEnchantedBook(pOutput, enchantments, ModEnchantments.POLITICAL_CORRECTNESS);
                addMaxLevelEnchantedBook(pOutput, enchantments, ModEnchantments.FISSION);
                addMaxLevelEnchantedBook(pOutput, enchantments, ModEnchantments.TORNADO);
                addMaxLevelEnchantedBook(pOutput, enchantments, ModEnchantments.LOUIS_XVI);

            })
            .build());

    // 辅助方法：生成级附魔书
    private static void addMaxLevelEnchantedBook(CreativeModeTab.Output output, HolderGetter<Enchantment> enchantments, ResourceKey<Enchantment> enchantKey) {
        Holder.Reference<Enchantment> holder = enchantments.getOrThrow(enchantKey); // 获取附魔的 Holder
        Enchantment enchantment = holder.value(); // 从 Holder 中提取附魔实例
        int maxLevel = enchantment.getMaxLevel(); // 获取最大等级

        // 生成附魔书
        output.accept(EnchantedBookItem.createForEnchantment(
                new EnchantmentInstance(holder, maxLevel)
        ));
    }

    //记得在总线上注册
    public static void register(IEventBus eventBus){
        CREATIVE_MODE_TABS.register(eventBus);
    }
}

