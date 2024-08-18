package com.hasoook.hasoookmod.block.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.FlowerBlock;
import net.minecraft.world.level.block.state.BlockState;

public class ConfusionFlower extends FlowerBlock {
    public ConfusionFlower(Holder<MobEffect> pEffect, float pSeconds, Properties pProperties) {
        super(pEffect, pSeconds, pProperties);
    }

    @Override
    public void animateTick(BlockState pState, Level pLevel, BlockPos pPos, RandomSource pRandom) {
        double d0 = (double)pPos.getX() + 0.5 + (pRandom.nextDouble() - 0.5) * 0.2;
        double d1 = (double)pPos.getY() + 0.7 + (pRandom.nextDouble() - 0.5) * 0.2;
        double d2 = (double)pPos.getZ() + 0.5 + (pRandom.nextDouble() - 0.5) * 0.2;
        pLevel.addParticle(ParticleTypes.EFFECT, d0, d1, d2, 0, 0.1, 0);
    }

}
