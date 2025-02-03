package com.hasoook.hasoookmod.client;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RenderLivingEvent;

import java.lang.reflect.Field;

@OnlyIn(Dist.CLIENT)
public class HideHeadHandler {
    @SubscribeEvent
    public static void onRenderLiving(RenderLivingEvent.Pre<?, ?> event) {
        // 直接检查并隐藏通用模型的头部
        if (event.getRenderer().getModel() instanceof HumanoidModel<?> humanoidModel) {
            humanoidModel.head.visible = false; // 隐藏头
            humanoidModel.hat.visible = false; // 隐藏帽子层
        }

        // 其他生物模型的适配（例如非人形生物）
        tryHideHeadByReflection(event.getRenderer().getModel());
    }

    // 通过反射处理非人形模型的头部
    private static void tryHideHeadByReflection(Object model) {
        try {
            Field headField = model.getClass().getDeclaredField("head");
            headField.setAccessible(true);
            ModelPart head = (ModelPart) headField.get(model);
            head.visible = false;
        } catch (Exception ignored) {
            // 无 head 字段或访问失败时忽略
        }
    }
}
