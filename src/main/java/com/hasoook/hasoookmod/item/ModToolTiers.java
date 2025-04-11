package com.hasoook.hasoookmod.item;

import com.hasoook.hasoookmod.util.ModTags;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.SimpleTier;

public class ModToolTiers {
    public static final Tier HUGE_DIAMOND = new SimpleTier(ModTags.Blocks.INCORRECT_FOR_BISMUTH_TOOL,
            15610, 4f, 3f, 28, () -> Ingredient.of(Items.DIAMOND));
}
