package com.hasoook.hasoookmod.item;

import com.hasoook.hasoookmod.HasoookMod;
import com.hasoook.hasoookmod.block.ModBlock;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModCreativeTab {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, HasoookMod.MODID);
    // 这个string是鼠标移动到tab上的显示的内容。
    public static final String HASOOOK_MOD_TAB_STRING = "creativetab.hasoook_tab";
    // 添加一个tab，title标题，icon显示图标，displayItem是指tab中添加的内容，这里传入一个lammabd表达式，通过poutput添加物品
    public static final Supplier<CreativeModeTab> EXAMPLE_TAB  = CREATIVE_MODE_TABS.register("example_tab",() -> CreativeModeTab.builder()
            .withTabsBefore(CreativeModeTabs.COMBAT)
            .title(Component.translatable(HASOOOK_MOD_TAB_STRING))
            .icon(ModItems.HUGE_DIAMOND_PICKAXE.get()::getDefaultInstance)
            .displayItems((pParameters, pOutput) -> {
                pOutput.accept(ModItems.TOTEM_OF_SURRENDER.get());
                pOutput.accept(ModItems.ENCHANTMENT_BRUSH.get());
                pOutput.accept(ModItems.WATER_BUCKET.get());
                pOutput.accept(ModItems.POKER.get());
                pOutput.accept(ModItems.HUGE_DIAMOND_PICKAXE.get());
                pOutput.accept(ModItems.WATER_BOOTS.get());
                pOutput.accept(ModItems.SPIT.get());

                pOutput.accept(ModBlock.CONFUSION_FLOWER.get());
                pOutput.accept(ModBlock.HASOOOK_LUCKY_BLOCK.get());
                pOutput.accept(ModBlock.GREEN_SCREEN_BLOCK.get());
            })
            .build());

    //记得在总线上注册
    public static void register(IEventBus eventBus){
        CREATIVE_MODE_TABS.register(eventBus);
    }
}

