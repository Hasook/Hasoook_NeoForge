package com.hasoook.hasoookmod.net;

import com.hasoook.hasoookmod.HasoookMod;
import com.hasoook.hasoookmod.gamerule.ModGameRules;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SyncGameRulePacket(boolean ruleState) implements CustomPacketPayload {
    public static final Type<SyncGameRulePacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(HasoookMod.MOD_ID, "sync_game_rule")
    );

    public static final StreamCodec<FriendlyByteBuf, SyncGameRulePacket> STREAM_CODEC = StreamCodec.of(
            (buf, packet) -> buf.writeBoolean(packet.ruleState),
            buf -> new SyncGameRulePacket(buf.readBoolean())
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    // 客户端处理逻辑
    public static void handle(SyncGameRulePacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (Minecraft.getInstance().level != null) {
                // 更新客户端本地的规则状态
                Minecraft.getInstance().level.getGameRules()
                        .getRule(ModGameRules.RANDOM_BLOCK_DROPS)
                        .set(packet.ruleState(), null);
            }
        });
    }
}