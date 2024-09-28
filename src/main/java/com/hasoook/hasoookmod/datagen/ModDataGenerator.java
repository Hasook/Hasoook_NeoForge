package com.hasoook.hasoookmod.datagen;

import com.hasoook.hasoookmod.HasoookMod;
import com.hasoook.hasoookmod.datagen.item.tags.ModBlockTagsProvider;
import com.hasoook.hasoookmod.datagen.item.tags.ModItemTagsProvider;
import com.hasoook.hasoookmod.datagen.provider.ModSoundDefinitionsProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.concurrent.CompletableFuture;

// 用于注册数据生成器的类，该类通过EventBusSubscriber注解自动注册到MOD总线上
@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD, modid = HasoookMod.MODID)
public class ModDataGenerator {
    // 订阅GatherDataEvent事件，当数据收集事件触发时执行该方法
    @SubscribeEvent
    public static void register(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput output = generator.getPackOutput();
        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();

        // 为数据生成器添加一个自定义的数据包内置条目提供者
        generator.addProvider(event.includeServer(),new ModDatapackBuiltinEntriesProvider(output,lookupProvider));
        TagsProvider<Block> tagsprovider4 = generator.addProvider(event.includeServer(),new ModBlockTagsProvider(output,lookupProvider,HasoookMod.MODID,existingFileHelper));
        generator.addProvider(event.includeServer(),new ModItemTagsProvider(output,lookupProvider,tagsprovider4.contentsGetter(),HasoookMod.MODID,existingFileHelper));
        generator.addProvider(event.includeClient(), new ModSoundDefinitionsProvider(output, event.getExistingFileHelper()));
    }
}
