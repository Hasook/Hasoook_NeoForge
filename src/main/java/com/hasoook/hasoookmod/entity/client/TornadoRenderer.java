package com.hasoook.hasoookmod.entity.client;

import com.hasoook.hasoookmod.HasoookMod;
import com.hasoook.hasoookmod.entity.custom.TornadoEntity;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class TornadoRenderer extends MobRenderer<TornadoEntity, TornadoModel<TornadoEntity>> {
    public TornadoRenderer(EntityRendererProvider.Context context) {
        super(context, new TornadoModel<>(context.bakeLayer(TornadoModel.LAYER_LOCATION)), 0.25f);
        this.addLayer(new TornadoWindLayer(context,this)); // 添加风效层
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull TornadoEntity entity) {
        return ResourceLocation.fromNamespaceAndPath(HasoookMod.MOD_ID, "textures/entity/tornado/transparent.png");
    }

    public static TornadoModel<TornadoEntity> enable(TornadoModel<TornadoEntity> model, ModelPart... parts) {
        model.head().visible = false;
        for (ModelPart part : parts) {
            part.visible = true;
        }
        return model;
    }
}
