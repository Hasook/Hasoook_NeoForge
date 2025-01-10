package com.hasoook.hasoookmod.client.renderer.entity;

import com.hasoook.hasoookmod.effect.ModEffects;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.VillagerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.CrossedArmsItemLayer;
import net.minecraft.client.renderer.entity.layers.CustomHeadLayer;
import net.minecraft.client.renderer.entity.layers.VillagerProfessionLayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.npc.Villager;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class VillagerRenderer extends MobRenderer<Villager, VillagerModel<Villager>> {
    private static final ResourceLocation VILLAGER_BASE_SKIN = ResourceLocation.withDefaultNamespace("textures/entity/villager/villager.png");

    public VillagerRenderer(EntityRendererProvider.Context p_174437_) {
        super(p_174437_, new VillagerModel<>(p_174437_.bakeLayer(ModelLayers.VILLAGER)), 0.5F);
        this.addLayer(new CustomHeadLayer<>(this, p_174437_.getModelSet(), p_174437_.getItemInHandRenderer()));
        this.addLayer(new VillagerProfessionLayer<>(this, p_174437_.getResourceManager(), "villager"));
        this.addLayer(new CrossedArmsItemLayer<>(this, p_174437_.getItemInHandRenderer()));
    }

    protected void scale(Villager pLivingEntity, PoseStack pPoseStack, float pPartialTickTime) {
        float f = 0.9375F * pLivingEntity.getAgeScale();
        pPoseStack.scale(f, f, f);
    }

    protected float getShadowRadius(Villager pEntity) {
        float f = super.getShadowRadius(pEntity);
        return pEntity.isBaby() ? f * 0.5F : f;
    }

    public ResourceLocation getTextureLocation(Villager pEntity) {
        return VILLAGER_BASE_SKIN;
    }

    protected boolean isShaking(Villager pEntity) {
        return super.isShaking(pEntity) || pEntity.hasEffect(ModEffects.UNYIELDING);
    }
}
