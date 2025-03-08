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

import java.util.function.Consumer;
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
                final HolderGetter<Enchantment> enchantments = pParameters.holders().lookupOrThrow(Registries.ENCHANTMENT);
                Consumer<ResourceKey<Enchantment>> addEnchantedBook = enchantKey -> {
                    Holder.Reference<Enchantment> holder = enchantments.getOrThrow(enchantKey);
                    Enchantment enchantment = holder.value();
                    pOutput.accept(EnchantedBookItem.createForEnchantment(
                            new EnchantmentInstance(holder, enchantment.getMaxLevel())
                    ));
                };

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
                pOutput.accept(ModItems.INSIGHT_MAGNIFIER.get());

                // 添加自定义的方块
                pOutput.accept(ModBlock.CONFUSION_FLOWER.get());
                pOutput.accept(ModBlock.HASOOOK_LUCKY_BLOCK.get());
                pOutput.accept(ModBlock.GREEN_SCREEN_BLOCK.get());

                // 添加自定义的附魔书
                addEnchantedBook.accept(ModEnchantments.RANDOM_BULLETS);
                addEnchantedBook.accept(ModEnchantments.SEPARATION_EXPLOSION);
                addEnchantedBook.accept(ModEnchantments.DISDAIN);
                addEnchantedBook.accept(ModEnchantments.SWAP);
                addEnchantedBook.accept(ModEnchantments.CHAIN_DAMAGE);
                addEnchantedBook.accept(ModEnchantments.GIVE);
                addEnchantedBook.accept(ModEnchantments.UNYIELDING);
                addEnchantedBook.accept(ModEnchantments.SEVEN_STEP_SNAKE_VENOM);
                addEnchantedBook.accept(ModEnchantments.KILL_A_MAN_EVERY_TEN_PACES);
                addEnchantedBook.accept(ModEnchantments.BACKSTAB);
                addEnchantedBook.accept(ModEnchantments.BETRAY);
                addEnchantedBook.accept(ModEnchantments.RACIAL_DISCRIMINATION);
                addEnchantedBook.accept(ModEnchantments.MIDDLE_EAST_BEST_PILOT);
                addEnchantedBook.accept(ModEnchantments.DE_URBANIZATION);
                addEnchantedBook.accept(ModEnchantments.SPOTLIGHT);
                addEnchantedBook.accept(ModEnchantments.ZERO_COST_PURCHASE);
                addEnchantedBook.accept(ModEnchantments.POLITICAL_CORRECTNESS);
                addEnchantedBook.accept(ModEnchantments.FISSION);
                addEnchantedBook.accept(ModEnchantments.TORNADO);
                addEnchantedBook.accept(ModEnchantments.LOUIS_XVI);
            })
            .build());
    public static void register(IEventBus eventBus){
        CREATIVE_MODE_TABS.register(eventBus);
    }
}
