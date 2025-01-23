package com.hasoook.hasoookmod.entity;

import com.hasoook.hasoookmod.HasoookMod;
import com.hasoook.hasoookmod.entity.custom.TornadoEntity;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(BuiltInRegistries.ENTITY_TYPE, HasoookMod.MOD_ID);

    public static final Supplier<EntityType<TornadoEntity>> TORNADO =
            ENTITY_TYPES.register("tornado", () -> EntityType.Builder.of(TornadoEntity::new, MobCategory.CREATURE)
                    .sized(0.75f, 0.35f).build("tornado"));

    public static void register(IEventBus eventBus) {
        ENTITY_TYPES.register(eventBus);
    }
}
