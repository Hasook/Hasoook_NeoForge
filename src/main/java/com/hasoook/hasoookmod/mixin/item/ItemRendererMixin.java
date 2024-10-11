package com.hasoook.hasoookmod.mixin.item;

import com.hasoook.hasoookmod.HasoookMod;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.LlamaSpit;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemRenderer.class)
public class ItemRendererMixin {

    @Unique
    private final Minecraft mc = Minecraft.getInstance();

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    public void renderItem(ItemStack pItemStack, ItemDisplayContext pDisplayContext, boolean pLeftHand, PoseStack pPoseStack, MultiBufferSource pBufferSource, int pCombinedLight, int pCombinedOverlay, BakedModel pModel, CallbackInfo ci) {
        // 检查是否是特定物品
        if (BuiltInRegistries.ITEM.getKey(pItemStack.getItem()).equals(ResourceLocation.fromNamespaceAndPath(HasoookMod.MODID, "spit"))) {
            // 取消默认渲染
            ci.cancel();

            // 渲染生物模型，例如羊驼
            LlamaSpit llamaSpit = new LlamaSpit(EntityType.LLAMA_SPIT, mc.level);
            pPoseStack.pushPose();
            // 使用 org.joml.Quaternionf 进行旋转
            // Quaternionf rotation = new Quaternionf().rotateY((float) Math.toRadians(180));
            // matrices.multiply(rotation);

            pPoseStack.scale(1F, 1F, 1F);// 调整缩放比例
            mc.getEntityRenderDispatcher().render(llamaSpit, 0, 0, 0, 0.0F, 1.0F, pPoseStack, pBufferSource, pCombinedLight);
            pPoseStack.popPose();
        }
    }
}
