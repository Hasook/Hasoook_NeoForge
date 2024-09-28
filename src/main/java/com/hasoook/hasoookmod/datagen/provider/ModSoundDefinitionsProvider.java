package com.hasoook.hasoookmod.datagen.provider;

import com.hasoook.hasoookmod.HasoookMod;
import com.hasoook.hasoookmod.sound.ModSounds;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.common.data.SoundDefinition;
import net.neoforged.neoforge.common.data.SoundDefinitionsProvider;

public class ModSoundDefinitionsProvider extends SoundDefinitionsProvider {
    public ModSoundDefinitionsProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, HasoookMod.MODID, existingFileHelper);
    }

    @Override
    public void registerSounds() {
        add(ModSounds.BILI_COIN_THROW_SOUND, SoundDefinition.definition()
                .with(
                        sound("hasoook:item.huge_diamond_pickaxe.dong")
                )
                .subtitle(ModSounds.DONG)
                .replace(true)
        );
    }
}
