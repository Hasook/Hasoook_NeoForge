package com.hasoook.hasoookmod.client.render.itme;

import com.hasoook.hasoookmod.item.ModItems;
import com.hasoook.hasoookmod.item.custom.PetCompassItem;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.ClampedItemPropertyFunction;
import net.minecraft.client.renderer.item.CompassItemPropertyFunction;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CompassItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.LodestoneTracker;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

import javax.annotation.Nullable;

@EventBusSubscriber(modid = "hasoook", bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientSetup {
    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            ItemProperties.register(
                    ModItems.PET_COMPASS.get(),
                    ResourceLocation.fromNamespaceAndPath("hasoook", "angle"),
                    new CompassItemPropertyFunction(
                            (level, stack, entity) -> {
                                LodestoneTracker tracker = stack.get(DataComponents.LODESTONE_TRACKER);
                                return tracker != null ? tracker.target().orElse(null) : null;
                            }
                    )
            );
        });
    }

    // 完全复刻原版 CompassItemPropertyFunction 和 CompassWobble
    private static class CustomCompassItemPropertyFunction implements ClampedItemPropertyFunction {
        private final CompassWobble wobble = new CompassWobble();
        private final CompassWobble wobbleRandom = new CompassWobble();
        private final CompassTarget target;

        public CustomCompassItemPropertyFunction(CompassTarget target) {
            this.target = target;
        }

        @Override
        public float unclampedCall(ItemStack pStack, @Nullable ClientLevel pLevel, @Nullable LivingEntity pEntity, int pSeed) {
            Entity entity = (Entity)(pEntity != null ? pEntity : pStack.getEntityRepresentation());
            if (entity == null) {
                return 0.0F;
            } else {
                pLevel = this.tryFetchLevelIfMissing(entity, pLevel);
                return pLevel == null ? 0.0F : this.getCompassRotation(pStack, pLevel, pSeed, entity);
            }
        }

        private float getCompassRotation(ItemStack stack, ClientLevel level, int seed, Entity entity) {
            LodestoneTracker tracker = stack.get(DataComponents.LODESTONE_TRACKER);
            GlobalPos targetPos = tracker != null ? tracker.target().orElse(null) : null;
            long gameTime = level.getGameTime();

            // 在getCompassRotation方法中添加调试输出
            System.out.println("Current dimension: " + level.dimension());
            System.out.println("Target position: " + targetPos);

            if (!isValidCompassTargetPos(entity, targetPos)) {
                return getRandomlySpinningRotation(seed, gameTime);
            } else {
                // 添加维度检查
                if (targetPos.dimension() != level.dimension()) {
                    return getRandomlySpinningRotation(seed, gameTime);
                }
                return getRotationTowardsCompassTarget(entity, gameTime, targetPos.pos());
            }
        }

        private float getRandomlySpinningRotation(int seed, long ticks) {
            if (wobbleRandom.shouldUpdate(ticks)) {
                wobbleRandom.update(ticks, Math.random());
            }
            double angle = wobbleRandom.rotation + (float) hash(seed) / 2.1474836E9F;
            return Mth.positiveModulo((float) angle, 1.0F);
        }

        private float getRotationTowardsCompassTarget(Entity pEntity, long pTicks, BlockPos pPos) {
            double d0 = this.getAngleFromEntityToPos(pEntity, pPos);
            double d1 = this.getWrappedVisualRotationY(pEntity);
            if (pEntity instanceof Player player && player.isLocalPlayer() && player.level().tickRateManager().runsNormally()) {
                if (this.wobble.shouldUpdate(pTicks)) {
                    this.wobble.update(pTicks, 0.5 - (d1 - 0.25));
                }

                double d3 = d0 + this.wobble.rotation;
                return Mth.positiveModulo((float)d3, 1.0F);
            }

            double d2 = 0.5 - (d1 - 0.25 - d0);
            return Mth.positiveModulo((float)d2, 1.0F);
        }

        @Nullable
        private ClientLevel tryFetchLevelIfMissing(Entity entity, @Nullable ClientLevel level) {
            return (level == null && entity.level() instanceof ClientLevel clientLevel)
                    ? clientLevel
                    : level;
        }

        private boolean isValidCompassTargetPos(Entity entity, @Nullable GlobalPos pos) {
            return pos != null
                    && pos.dimension().equals(entity.level().dimension())
                    && pos.pos().distToCenterSqr(entity.position()) >= 1.0E-5;
        }

        private double getAngleFromEntityToPos(Entity pEntity, BlockPos pPos) {
            Vec3 vec3 = Vec3.atCenterOf(pPos);
            return Math.atan2(vec3.z() - pEntity.getZ(), vec3.x() - pEntity.getX()) / (float) (Math.PI * 2);
        }

        private double getWrappedVisualRotationY(Entity entity) {
            return Mth.positiveModulo(entity.getVisualRotationYInDegrees() / 360.0, 1.0);
        }

        private int hash(int value) {
            return value * 1327217883;
        }

        public float clamp(float value, float min, float max) {
            return Mth.clamp(value, 0.0F, 1.0F);
        }

        @FunctionalInterface
        public interface CompassTarget {
            @Nullable
            GlobalPos getPos(ClientLevel level, ItemStack stack, Entity entity);
        }
    }

    @OnlyIn(Dist.CLIENT)
    static class CompassWobble {
        double rotation;
        private double deltaRotation;
        private long lastUpdateTick;

        boolean shouldUpdate(long pTicks) {
            return this.lastUpdateTick != pTicks;
        }

        void update(long pTicks, double pRotation) {
            this.lastUpdateTick = pTicks;
            double d0 = pRotation - this.rotation;
            d0 = Mth.positiveModulo(d0 + 0.5, 1.0) - 0.5;
            this.deltaRotation += d0 * 0.1;
            this.deltaRotation *= 0.8;
            this.rotation = Mth.positiveModulo(this.rotation + this.deltaRotation, 1.0);
        }
    }
}
