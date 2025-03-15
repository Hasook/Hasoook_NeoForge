package com.hasoook.hasoookmod.net;

import com.hasoook.hasoookmod.HasoookMod;
import com.hasoook.hasoookmod.event.enchantment.MobControlHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ControlInputPacket(
        boolean forward,
        boolean backward,
        boolean left,
        boolean right,
        boolean jump,
        boolean sprinting,
        boolean sneaking
) implements CustomPacketPayload {

    public static final Type<ControlInputPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(HasoookMod.MOD_ID, "control_input")
    );

    public static final StreamCodec<FriendlyByteBuf, ControlInputPacket> STREAM_CODEC = StreamCodec.of(
            (buf, packet) -> {
                buf.writeBoolean(packet.forward);
                buf.writeBoolean(packet.backward);
                buf.writeBoolean(packet.left);
                buf.writeBoolean(packet.right);
                buf.writeBoolean(packet.jump);
                buf.writeBoolean(packet.sprinting);
                buf.writeBoolean(packet.sneaking);
            },
            buf -> new ControlInputPacket(
                    buf.readBoolean(),
                    buf.readBoolean(),
                    buf.readBoolean(),
                    buf.readBoolean(),
                    buf.readBoolean(),
                    buf.readBoolean(),
                    buf.readBoolean()
            )
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(ControlInputPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer serverPlayer) {
                MobControlHandler.handleControlInput(
                        serverPlayer,
                        packet.forward,
                        packet.backward,
                        packet.left,
                        packet.right,
                        packet.jump,
                        packet.sprinting,
                        packet.sneaking
                );
            }
        });
    }
}