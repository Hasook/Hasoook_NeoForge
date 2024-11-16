package com.hasoook.hasoookmod.mixin.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.world.Clearable;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.CampfireBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CampfireBlockEntity.class)
public class CampfireBlockEntityMixin extends BlockEntity implements Clearable {
    @Shadow @Final private NonNullList<ItemStack> items;

    public CampfireBlockEntityMixin(BlockEntityType<?> pType, BlockPos pPos, BlockState pBlockState) {
        super(pType, pPos, pBlockState);
    }

    @Override
    public void clearContent() {
        this.items.clear();
    }

    // 使用 @Inject 注入代码，修改掉落物数量
    @Inject(method = "cookTick", at = @At("HEAD"), cancellable = true)
    private static void injectCookTick(Level pLevel, BlockPos pPos, BlockState pState, CampfireBlockEntity pBlockEntity, CallbackInfo ci) {
        // 假设掉落物数量是通过某个变量或方法来控制的
        // 你可以将掉落物数量固定为 4
        pBlockEntity.getItems().forEach(itemStack -> {
            // 这里只是个示例，具体代码可能根据你想修改的行为而不同
            // 比如增加掉落次数或修改某些条件
            if (!itemStack.isEmpty()) {
                // 你可以在此处增加掉落的逻辑
                for (int i = 0; i < 4; i++) {
                    // 使用 Containers.dropItemStack 或其他方法来掉落物品
                    Containers.dropItemStack(pLevel, pPos.getX(), pPos.getY(), pPos.getZ(), itemStack);
                }
            }
        });
    }

}
