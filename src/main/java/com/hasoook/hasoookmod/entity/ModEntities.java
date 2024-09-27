package com.hasoook.hasoookmod.entity;

import com.hasoook.hasoookmod.HasoookMod;
import com.hasoook.hasoookmod.entity.custom.PokerProjectileEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(Registries.ENTITY_TYPE, HasoookMod.MODID);

    public static final DeferredHolder<EntityType<?>,EntityType<PokerProjectileEntity>> POKER_PROJECTILE = ENTITY_TYPES.register("poker_projectile",
            ()-> EntityType.Builder.<PokerProjectileEntity>of(PokerProjectileEntity::new, MobCategory.MISC).sized(0.25f,0.25f).build("poker_projectile"));

    public static void register(IEventBus eventBus){
        ENTITY_TYPES.register(eventBus);
    }
}
