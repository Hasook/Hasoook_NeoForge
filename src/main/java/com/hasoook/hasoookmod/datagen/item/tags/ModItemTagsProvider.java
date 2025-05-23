package com.hasoook.hasoookmod.datagen.item.tags;

import com.hasoook.hasoookmod.item.ModItems;
import com.hasoook.hasoookmod.tags.ModItemTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class ModItemTagsProvider extends ItemTagsProvider {

    public ModItemTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, CompletableFuture<TagLookup<Block>> blockTags, String modId, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, blockTags, modId, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        this.tag(ModItemTags.SEPARATION_ITEMS).add(Items.TNT)
                .add(Items.TNT_MINECART)
                .add(Items.WIND_CHARGE);
        this.tag(ModItemTags.ENTITY_ENCHANTMENT).addTag(ItemTags.ARMOR_ENCHANTABLE)
                .addTag(ItemTags.CHEST_ARMOR_ENCHANTABLE)
                .addTag(ItemTags.DURABILITY_ENCHANTABLE)
                .addTag(ItemTags.BOW_ENCHANTABLE);
        this.tag(ModItemTags.LOUIS_XVI).addTag(ItemTags.SWORDS)
                .addTag(ItemTags.AXES)
                .add(Items.SHEARS);
        this.tag(ModItemTags.TORNADO_ITEMS).add(Items.WIND_CHARGE);
        this.tag(ModItemTags.RACIAL_DISCRIMINATION_ITEMS).addTag(ItemTags.SWORDS)
                .addTag(ItemTags.AXES)
                .addTag(ItemTags.WEAPON_ENCHANTABLE)
                .addTag(ItemTags.BOW_ENCHANTABLE)
                .add(Items.LEAD)
                .add(Items.CHEST);
        this.tag(ModItemTags.GRAVITY_GLOVE).add(ModItems.GRAVITY_GLOVE.get());
    }
}
