package com.hasoook.hasoookmod.client;

import com.hasoook.hasoookmod.HasoookMod;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
@Mod(value = HasoookMod.MOD_ID, dist = Dist.CLIENT)
public class ClientHasoookMod {
    public ClientHasoookMod(IEventBus modEventBus, ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }
}
