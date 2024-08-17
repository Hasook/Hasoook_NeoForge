package com.hasoook.hasoookmod.block;

import com.hasoook.hasoookmod.HasoookMod;
import com.hasoook.hasoookmod.block.custom.ConfusionFlower;
import com.hasoook.hasoookmod.item.ModItems;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModBlock {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(HasoookMod.MODID);

    public static final DeferredBlock<Block> CONFUSION_FLOWER = registerBlock("confusion_flower",
            ()-> new ConfusionFlower(MobEffects.CONFUSION,60,BlockBehaviour.Properties.ofFullCopy(Blocks.CORNFLOWER).noOcclusion().noCollission()));

    private static DeferredBlock<Block> registerBlock(String name, Supplier<Block> blockSupplier) {
        DeferredBlock<Block> register = BLOCKS.register(name, blockSupplier);
        ModItems.ITEMS.register(name,()-> new BlockItem(register.get(),new Item.Properties()));
        return register;
    }

    public static DeferredBlock<Block> registerSimpleBlock(String name, BlockBehaviour.Properties props) {
        DeferredBlock<Block> deferredBlock =  BLOCKS.registerSimpleBlock(name,props);
        ModItems.ITEMS.register(name,()-> new BlockItem(deferredBlock.get(),new Item.Properties()));
        return  deferredBlock;
    }

    public static DeferredBlock<Block> registerSimpleBlock(String name, BlockBehaviour.Properties props, Item.Properties properties) {
        DeferredBlock<Block> deferredBlock =  BLOCKS.registerSimpleBlock(name,props);
        ModItems.ITEMS.register(name,()-> new BlockItem(deferredBlock.get(),properties));
        return  deferredBlock;
    }

    public static void register(IEventBus eventBus){
        BLOCKS.register(eventBus);
    }
}
