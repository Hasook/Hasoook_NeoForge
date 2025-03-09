package com.hasoook.hasoookmod.item.custom;

import net.minecraft.ChatFormatting;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.TicketType;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.LodestoneTracker;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.phys.AABB;
import org.apache.logging.log4j.LogManager;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class PetCompassItem extends Item {
    private static final int SEARCH_RADIUS = 512; // 搜索半径（格数）
    private static final int CHUNK_RADIUS = (int) Math.ceil(SEARCH_RADIUS / 16.0); // 区块半径
    public PetCompassItem(Item.Properties properties) {
        super(properties);
    }

    @Nullable
    public static GlobalPos getSpawnPosition(Level pLevel) {
        return pLevel.dimensionType().natural() ? GlobalPos.of(pLevel.dimension(), pLevel.getSharedSpawnPos()) : null;
    }

    /*@Override
    public boolean isFoil(ItemStack stack) {
        return stack.has(DataComponents.LODESTONE_TRACKER) || super.isFoil(stack);
    }*/

    @Override
    public void inventoryTick(@NotNull ItemStack stack, @NotNull Level level, @NotNull Entity entity, int slot, boolean selected) {
        if (level instanceof ServerLevel) {
            LodestoneTracker tracker = stack.get(DataComponents.LODESTONE_TRACKER);
            if (tracker != null) {
                LodestoneTracker updatedTracker = new LodestoneTracker(
                        tracker.target(),
                        tracker.tracked()
                );
                if (!updatedTracker.equals(tracker)) {
                    stack.set(DataComponents.LODESTONE_TRACKER, updatedTracker);
                }
            }
        }
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(Level level, Player player, @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide) {
            ServerLevel serverLevel = (ServerLevel) level;
            BlockPos playerPos = player.blockPosition();

            // 立即发送初始提示
            player.displayClientMessage(Component.translatable("item.hasoook.pet_compass.searching")
                    .withStyle(ChatFormatting.GRAY), true);

            // 使用 CompletableFuture 实现异步处理
            CompletableFuture.runAsync(() -> {
                Set<ChunkPos> loadedChunks = new HashSet<>();
                TamableAnimal nearestPet = null;
                double minDistance = Double.MAX_VALUE;

                try {
                    // 从内到外逐层加载区块
                    for (int radius = 0; radius <= CHUNK_RADIUS; radius++) {
                        final int currentRadius = radius;
                        Set<ChunkPos> currentLayer = getChunkSquare(new ChunkPos(playerPos), radius);

                        // 同步加载区块到主线程
                        serverLevel.getServer().execute(() -> {
                            for (ChunkPos chunk : currentLayer) {
                                serverLevel.getChunkSource().addRegionTicket(
                                        TicketType.FORCED, chunk, 1, chunk
                                );
                                loadedChunks.add(chunk);
                                serverLevel.getChunk(chunk.x, chunk.z, ChunkStatus.FULL, true);
                            }
                        });

                        // 动态更新提示（每层更新）
                        serverLevel.getServer().execute(() -> {
                            player.displayClientMessage(
                                    Component.translatable("item.hasoook.pet_compass.searching")
                                            .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC)
                                            .append(".".repeat(currentRadius % 3 + 1)),
                                    true
                            );
                        });

                        // 计算当前搜索半径
                        int currentSearchRadius = radius * 16;
                        AABB searchArea = new AABB(playerPos)
                                .inflate(currentSearchRadius, 256, currentSearchRadius);

                        // 搜索逻辑
                        List<TamableAnimal> pets = serverLevel.getEntitiesOfClass(
                                TamableAnimal.class,
                                searchArea,
                                pet -> isValidPet(pet, player)
                        );

                        // 更新最近宠物
                        for (TamableAnimal pet : pets) {
                            double distance = pet.distanceToSqr(player);
                            if (distance < minDistance) {
                                minDistance = distance;
                                nearestPet = pet;
                            }
                        }

                        // 提前终止条件
                        if (nearestPet != null &&
                                minDistance < Math.pow(currentSearchRadius, 2)) {
                            break;
                        }
                    }

                    // 主线程处理结果
                    TamableAnimal finalNearestPet = nearestPet;
                    serverLevel.getServer().execute(() -> {
                        if (finalNearestPet != null) {
                            BlockPos petPos = finalNearestPet.blockPosition();
                            GlobalPos globalPos = GlobalPos.of(serverLevel.dimension(), petPos);
                            LodestoneTracker tracker = new LodestoneTracker(Optional.of(globalPos), true);
                            stack.set(DataComponents.LODESTONE_TRACKER, tracker);

                            serverLevel.playSound(null, playerPos,
                                    SoundEvents.LODESTONE_COMPASS_LOCK,
                                    SoundSource.PLAYERS, 1.0F, 1.0F
                            );

                            // 找到宠物提示
                            Component foundMsg = Component.translatable("item.hasoook.pet_compass.found")
                                    .append(Objects.requireNonNull(finalNearestPet.getDisplayName()))
                                    .withStyle(ChatFormatting.GREEN, ChatFormatting.ITALIC);
                            player.displayClientMessage(foundMsg, true);
                        } else {
                            serverLevel.playSound(null, playerPos,
                                    SoundEvents.VILLAGER_NO,
                                    SoundSource.PLAYERS, 1.0F, 1.0F
                            );
                            player.displayClientMessage(
                                    Component.translatable("item.hasoook.pet_compass.no_pet"),
                                    true
                            );
                        }
                    });
                } finally {
                    // 安全卸载区块
                    serverLevel.getServer().execute(() -> {
                        for (ChunkPos chunk : loadedChunks) {
                            serverLevel.getChunkSource().removeRegionTicket(
                                    TicketType.FORCED, chunk, 1, chunk
                            );
                        }
                    });
                }
            }, serverLevel.getServer()).exceptionally(e -> {
                LogManager.getLogger().error("Pet compass search failed", e);
                return null;
            });

            return InteractionResultHolder.sidedSuccess(stack, false);
        }
        return InteractionResultHolder.pass(stack);
    }

    // 宠物有效性验证
    private boolean isValidPet(TamableAnimal pet, Player player) {
        return pet.isTame() &&
                pet.getOwnerUUID() != null &&
                pet.getOwnerUUID().equals(player.getUUID()) &&
                !pet.isInvisible(); // 排除隐身宠物
    }

    // 生成方形区块区域
    private Set<ChunkPos> getChunkSquare(ChunkPos center, int radius) {
        Set<ChunkPos> chunks = new HashSet<>();
        if (radius == 0) {
            chunks.add(center);
            return chunks;
        }
        // 仅加载最外层
        for (int x = -radius; x <= radius; x++) {
            chunks.add(new ChunkPos(center.x + x, center.z - radius));
            chunks.add(new ChunkPos(center.x + x, center.z + radius));
        }
        for (int z = -radius + 1; z < radius; z++) {
            chunks.add(new ChunkPos(center.x - radius, center.z + z));
            chunks.add(new ChunkPos(center.x + radius, center.z + z));
        }
        return chunks;
    }

    @Override
    public @NotNull String getDescriptionId(ItemStack stack) {
        return stack.has(DataComponents.LODESTONE_TRACKER)
                ? "item.hasoook.pet_compass_linked"
                : super.getDescriptionId(stack);
    }
}
