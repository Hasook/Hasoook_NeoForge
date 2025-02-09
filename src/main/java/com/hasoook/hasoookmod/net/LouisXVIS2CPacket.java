package com.hasoook.hasoookmod.net;

import com.hasoook.hasoookmod.HasoookMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class LouisXVIS2CPacket implements CustomPacketPayload {
    private final int entityId; // 实体ID
    private final boolean hideHead;

    public LouisXVIS2CPacket(int entityId, boolean hideHead) {
        this.entityId = entityId;
        this.hideHead = hideHead;
    }

    public static final Type<LouisXVIS2CPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(HasoookMod.MOD_ID, "louis_xvi_s2c_packet")
    );

    // 使用正确的 StreamCodec
    public static final StreamCodec<FriendlyByteBuf, LouisXVIS2CPacket> STREAM_CODEC = StreamCodec.of(
            (buf, packet) -> {
                buf.writeInt(packet.entityId);
                buf.writeBoolean(packet.hideHead);
            },
            buf -> new LouisXVIS2CPacket(buf.readInt(), buf.readBoolean())
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    // 客户端处理逻辑
    public static void handle(LouisXVIS2CPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            ClientLevel level = (ClientLevel) Minecraft.getInstance().level;
            if (level == null) return;

            Entity entity = level.getEntity(packet.entityId);
            if (entity != null) {
                entity.getPersistentData().putBoolean("louis_xvi", packet.hideHead);
            }
        });
    }
}
