package com.hasoook.hasoookmod.effect;

import com.hasoook.hasoookmod.HasoookMod;
import com.hasoook.hasoookmod.effect.custom.ConfusionEffect;
import com.hasoook.hasoookmod.effect.custom.NormalEffect;
import com.hasoook.hasoookmod.effect.custom.SlimeyEffect;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModEffects {
    public static final DeferredRegister<MobEffect> MOB_EFFECTS =
            DeferredRegister.create(BuiltInRegistries.MOB_EFFECT, HasoookMod.MOD_ID);

    // 黏糊糊
    public static final Holder<MobEffect> SLIMEY_EFFECT = MOB_EFFECTS.register("slimey",
            () -> new SlimeyEffect(MobEffectCategory.NEUTRAL, 0x36ebab)
                    .addAttributeModifier(Attributes.MOVEMENT_SPEED,
                            ResourceLocation.fromNamespaceAndPath(HasoookMod.MOD_ID, "slimey"), -0.25f,
                            AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));

    // 混乱
    public static final Holder<MobEffect> CONFUSION = MOB_EFFECTS.register("confusion",
            () -> new ConfusionEffect(MobEffectCategory.HARMFUL, 0xe55590));

    // 罪恶
    public static final Holder<MobEffect> SIN = MOB_EFFECTS.register("sin",
            () -> new NormalEffect(MobEffectCategory.HARMFUL, 0x7794a2));

    // 不屈
    public static final Holder<MobEffect> UNYIELDING = MOB_EFFECTS.register("unyielding",
            () -> new NormalEffect(MobEffectCategory.BENEFICIAL, 0x828a9b));

    public static void register(IEventBus eventBus) {
        MOB_EFFECTS.register(eventBus);
    }
}
