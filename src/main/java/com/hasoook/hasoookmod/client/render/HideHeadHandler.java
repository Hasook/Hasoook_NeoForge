package com.hasoook.hasoookmod.client.render;

import com.hasoook.hasoookmod.HasoookMod;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.QuadrupedModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLivingEvent;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

@OnlyIn(Dist.CLIENT)
@EventBusSubscriber(modid = HasoookMod.MOD_ID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public class HideHeadHandler {
    private static final Map<Entity, List<ModelPartState>> visibilityStates = new WeakHashMap<>();

    @SubscribeEvent
    public static void onRenderPre(RenderLivingEvent.Pre<?, ?> event) {
        Entity entity = event.getEntity();
        boolean louisXvi = entity.getPersistentData().getBoolean("louis_xvi");

        if (!louisXvi) return;

        List<ModelPartState> states = new ArrayList<>();
        Object model = event.getRenderer().getModel();

        if (model instanceof HumanoidModel<?> humanoidModel) {
            states.add(new ModelPartState(humanoidModel.head, humanoidModel.head.visible));
            states.add(new ModelPartState(humanoidModel.hat, humanoidModel.hat.visible));
            humanoidModel.head.visible = false;
            humanoidModel.hat.visible = false;
        } else if (model instanceof QuadrupedModel<?> quadrupedModel) {
            states.add(new ModelPartState(quadrupedModel.head, quadrupedModel.head.visible));
            quadrupedModel.head.visible = false;
        }

        // 尝试通过反射获取头部部分，适用于其他模型
        try {
            Field headField = model.getClass().getDeclaredField("head");
            headField.setAccessible(true);
            ModelPart head = (ModelPart) headField.get(model);
            if (head != null && states.stream().noneMatch(state -> state.modelPart == head)) {
                states.add(new ModelPartState(head, head.visible));
                head.visible = false;
            }
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