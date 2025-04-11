package com.hasoook.hasoookmod.entity.client;

import com.hasoook.hasoookmod.HasoookMod;
import com.hasoook.hasoookmod.entity.custom.MeteoriteEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class MeteoriteRenderer extends LivingEntityRenderer<MeteoriteEntity, MeteoriteModel<MeteoriteEntity>> {
    public MeteoriteRenderer(EntityRendererProvider.Context pContext) {
        super(pContext, new MeteoriteModel<>(pContext.bakeLayer(MeteoriteModel.LAYER_LOCATION)), 0.5F);
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull MeteoriteEntity pEntity) {
        return ResourceLocation.fromNamespaceAndPath(HasoookMod.MOD_ID, "textures/entity/meteorite.png");
    }

    @Override
    public void render(MeteoriteEntity pEntity, float pEntityYaw, float pPartialTicks, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight) {
        int size = pEntity.getSize();
        pPoseStack.scale(size, size, size);

        super.render(pEntity, pEntityYaw, pPartialTicks, pPoseStack, pBuffer, pPackedLight);
    }

    @Override
    protected boolean shouldShowName(MeteoriteEntity entity) {
        return false; // 始终不渲染名称
    }
}
