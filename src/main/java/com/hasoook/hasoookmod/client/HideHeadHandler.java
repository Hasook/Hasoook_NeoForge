package com.hasoook.hasoookmod.client;

import com.hasoook.hasoookmod.effect.ModEffects;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RenderLivingEvent;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

@OnlyIn(Dist.CLIENT)
public class HideHeadHandler {
    private static final Map<Entity, List<ModelPartState>> visibilityStates = new WeakHashMap<>();

    @SubscribeEvent
    public static void onRenderPre(RenderLivingEvent.Pre<?, ?> event) {
        Entity entity = event.getEntity();
        boolean louisXvi = entity.getPersistentData().getBoolean("louis_xvi"); // 获取实体的nbt
        if (!louisXvi) return;

        List<ModelPartState> states = new ArrayList<>();
        Object model = event.getRenderer().getModel();

        if (model instanceof HumanoidModel<?> humanoidModel) {
            states.add(new ModelPartState(humanoidModel.head, humanoidModel.head.visible));
            states.add(new ModelPartState(humanoidModel.hat, humanoidModel.hat.visible));
            humanoidModel.head.visible = false;
            humanoidModel.hat.visible = false;
        }

        try {
            Field headField = model.getClass().getDeclaredField("head");
            headField.setAccessible(true);
            ModelPart head = (ModelPart) headField.get(model);
            states.add(new ModelPartState(head, head.visible));
            head.visible = false;
        } catch (Exception ignored) {}

        if (!states.isEmpty()) {
            visibilityStates.put(entity, states);
        }
    }

    @SubscribeEvent
    public static void onRenderPost(RenderLivingEvent.Post<?, ?> event) {
        Entity entity = event.getEntity();
        List<ModelPartState> states = visibilityStates.remove(entity);

        if (states != null) {
            for (ModelPartState state : states) {
                state.modelPart.visible = state.originalVisibility;
            }
        }
    }

    private static class ModelPartState {
        final ModelPart modelPart;
        final boolean originalVisibility;

        ModelPartState(ModelPart part, boolean visible) {
            modelPart = part;
            originalVisibility = visible;
        }
    }
}
