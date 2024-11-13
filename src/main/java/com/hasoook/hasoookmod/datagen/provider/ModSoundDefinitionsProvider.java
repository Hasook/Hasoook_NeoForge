package com.hasoook.hasoookmod.datagen.provider;

import com.hasoook.hasoookmod.HasoookMod;
import com.hasoook.hasoookmod.sound.ModSounds;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.common.data.SoundDefinition;
import net.neoforged.neoforge.common.data.SoundDefinitionsProvider;

public class ModSoundDefinitionsProvider extends SoundDefinitionsProvider {
    public ModSoundDefinitionsProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, HasoookMod.MOD_ID, existingFileHelper);
    }

    @Override
    public void registerSounds() {
        add(ModSounds.DONG, SoundDefinition.definition()
                .with(
                        sound("hasoook:item.huge_diamond_pickaxe.dong")
                )
                .subtitle(ModSounds.SUBTITLES_DONG)
                .replace(true)
        );
    }
}
