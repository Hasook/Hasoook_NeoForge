package com.hasoook.hasoookmod.block.custom;

import net.minecraft.world.level.block.Block;

public class GreenScreenBlock extends Block {
    public GreenScreenBlock(Properties p_49795_) {
        super(p_49795_.lightLevel((state) -> 15).noOcclusion());
    }

}
