package com.hasoook.hasoookmod.entity.client;

import com.hasoook.hasoookmod.entity.custom.TornadoEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

@OnlyIn(Dist.CLIENT)
public class TornadoWindLayer extends RenderLayer<TornadoEntity, TornadoModel<TornadoEntity>> {
    private static final ResourceLocation TEXTURE_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/breeze/breeze_wind.png"); // 修改为Gecko的风效纹理
    private final TornadoModel<TornadoEntity> model;

    public TornadoWindLayer(EntityRendererProvider.Context pContext, RenderLayerParent<TornadoEntity, TornadoModel<TornadoEntity>> pRenderer) {
        super(pRenderer);
        this.model = new TornadoModel<>(pContext.bakeLayer(ModelLayers.BREEZE_WIND));
    }

    @Override
    public void render(
            @NotNull PoseStack poseStack,
            MultiBufferSource bufferSource,
            int packedLight,
            TornadoEntity entity,
            float limbSwing,
            float limbSwingAmount,
            float partialTick,
            float ageInTicks,
            float netHeadYaw,
            float headPitch
    ) {
        float f = (float)entity.tickCount + partialTick;
        VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.breezeWind(TEXTURE_LOCATION, this.xOffset(f) % 1.0F, 0.0F)); // 计算偏移
        this.model.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        TornadoRenderer.enable(this.model, this.model.wind()).renderToBuffer(poseStack, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY); // 渲染
    }

    private float xOffset(float tickCount) {
        return tickCount * 0.01F; // 调整偏移速度
    }
}
