package com.hasoook.hasoookmod.recipes;

import com.hasoook.hasoookmod.HasoookMod;
import com.hasoook.hasoookmod.block.ModBlock;
import com.hasoook.hasoookmod.effect.ModPotions;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.item.alchemy.Potions;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.brewing.RegisterBrewingRecipesEvent;// 使用某种方法监听事件

@EventBusSubscriber(modid = HasoookMod.MOD_ID)
public class registerPotionsBrewingEvent {
    @SubscribeEvent
    public static void onRegisterPotionsBrewing(RegisterBrewingRecipesEvent event) {
        PotionBrewing.Builder builder = event.getBuilder();

        builder.addMix(Potions.AWKWARD, ModBlock.CONFUSION_FLOWER.asItem(), ModPotions.CONFUSION);
        // 混乱药水
    }
}
