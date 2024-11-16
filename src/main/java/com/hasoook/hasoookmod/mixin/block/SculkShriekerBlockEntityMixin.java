package com.hasoook.hasoookmod.mixin.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.SculkShriekerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEventListener;
import net.minecraft.world.level.gameevent.vibrations.VibrationSystem;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(SculkShriekerBlockEntity.class)
public abstract class SculkShriekerBlockEntityMixin extends BlockEntity implements GameEventListener.Provider<VibrationSystem.Listener>, VibrationSystem {
    @Shadow @Final private Listener vibrationListener;
    @Shadow private Data vibrationData;
    @Shadow @Final private User vibrationUser;
    @Shadow private int warningLevel;
    @Shadow protected abstract boolean canRespond(ServerLevel pLevel);
    @Shadow protected abstract boolean tryToWarn(ServerLevel pLevel, ServerPlayer pPlayer);
    @Shadow protected abstract void shriek(ServerLevel pLevel, @org.jetbrains.annotations.Nullable Entity pSourceEntity);

    public SculkShriekerBlockEntityMixin(BlockEntityType<?> pType, BlockPos pPos, BlockState pBlockState) {
        super(pType, pPos, pBlockState);
    }

    @Override
    public @NotNull Listener getListener() {
        return this.vibrationListener;
    }

    @Override
    public @NotNull Data getVibrationData() {
        return this.vibrationData;
    }

    @Override
    public @NotNull User getVibrationUser() {
        return this.vibrationUser;
    }
}
