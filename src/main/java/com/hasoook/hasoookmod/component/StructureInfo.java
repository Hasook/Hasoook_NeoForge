package com.mrbysco.structurecompass.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public record StructureInfo(BlockPos pos, ResourceKey<Level> dimension) {
	public static final Codec<StructureInfo> CODEC = RecordCodecBuilder.create(inst -> inst.group(
					BlockPos.CODEC.fieldOf("pos").forGetter(StructureInfo::pos),
					ResourceKey.codec(Registries.DIMENSION).fieldOf("dimension").forGetter(StructureInfo::dimension))
			.apply(inst, StructureInfo::new));
	public static final StreamCodec<RegistryFriendlyByteBuf, StructureInfo> STREAM_CODEC = StreamCodec.composite(
			BlockPos.STREAM_CODEC,
			StructureInfo::pos,
			ResourceKey.streamCodec(Registries.DIMENSION),
			StructureInfo::dimension,
			StructureInfo::new
	);
}
