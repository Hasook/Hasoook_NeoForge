package com.hasoook.hasoookmod.net;

import com.hasoook.hasoookmod.HasoookMod;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import static com.hasoook.hasoookmod.event.enchantment.MobControlHandler.handleLeftClick;

public record LeftClickAirPacket() implements CustomPacketPayload {
    public static final Type<LeftClickAirPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(HasoookMod.MOD_ID, "left_click_air")
    );

    // 定义 StreamCodec（编码与解码逻辑）
    public static final StreamCodec<FriendlyByteBuf, LeftClickAirPacket> STREAM_CODEC = StreamCodec.of(
            (buf, packet) -> {},
            buf -> new LeftClickAirPacket()
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    // 数据包处理器
    public static void handle(LeftClickAirPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer serverPlayer) {
                // 在这里处理左击空气逻辑
                handleLeftClick(serverPlayer);
            }
        });
    }
}
