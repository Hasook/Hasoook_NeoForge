package com.hasoook.hasoookmod.entity;

import com.hasoook.hasoookmod.HasoookMod;
import com.hasoook.hasoookmod.entity.custom.MeteoriteEntity;
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
                    .sized(0.1f, 0.1f).build("tornado"));
    public static final Supplier<EntityType<MeteoriteEntity>> METEORITE =
            ENTITY_TYPES.register("meteorite", () -> EntityType.Builder.of(MeteoriteEntity::new, MobCategory.CREATURE)
                    .sized(1f, 1f).build("meteorite"));

    public static void register(IEventBus eventBus) {
        ENTITY_TYPES.register(eventBus);
    }
}
