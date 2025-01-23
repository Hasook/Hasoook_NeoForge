package com.hasoook.hasoookmod.entity.client;

import com.hasoook.hasoookmod.HasoookMod;
import com.hasoook.hasoookmod.entity.custom.TornadoEntity;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class TornadoModel<T extends TornadoEntity> extends HierarchicalModel<T> {
    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(HasoookMod.MOD_ID, "gecko"), "main");

    private static final float WIND_TOP_SPEED = 0.6F;
    private static final float WIND_MIDDLE_SPEED = 0.8F;
    private static final float WIND_BOTTOM_SPEED = 1.0F;
    private final ModelPart root;
    private final ModelPart head;
    private final ModelPart eyes;
    private final ModelPart wind;
    private final ModelPart windTop;
    private final ModelPart windMid;
    private final ModelPart windBottom;
    private final ModelPart rods;

    public TornadoModel(ModelPart root) {
        this.root = root;
        this.wind = root.getChild("wind_body");
        this.windBottom = this.wind.getChild("wind_bottom");
        this.windMid = this.windBottom.getChild("wind_mid");
        this.windTop = this.windMid.getChild("wind_top");
        this.head = root.getChild("body").getChild("head");
        this.eyes = this.head.getChild("eyes");
        this.rods = root.getChild("body").getChild("rods");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();
        PartDefinition partdefinition1 = partdefinition.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));
        PartDefinition partdefinition2 = partdefinition1.addOrReplaceChild("rods", CubeListBuilder.create(), PartPose.offset(0.0F, 8.0F, 0.0F));
        partdefinition2.addOrReplaceChild(
                "rod_1",
                CubeListBuilder.create().texOffs(0, 17).addBox(-1.0F, 0.0F, -3.0F, 2.0F, 8.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(2.5981F, -3.0F, 1.5F, -2.7489F, -1.0472F, 3.1416F)
        );
        partdefinition2.addOrReplaceChild(
                "rod_2",
                CubeListBuilder.create().texOffs(0, 17).addBox(-1.0F, 0.0F, -3.0F, 2.0F, 8.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-2.5981F, -3.0F, 1.5F, -2.7489F, 1.0472F, 3.1416F)
        );
        partdefinition2.addOrReplaceChild(
                "rod_3",
                CubeListBuilder.create().texOffs(0, 17).addBox(-1.0F, 0.0F, -3.0F, 2.0F, 8.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.0F, -3.0F, -3.0F, 0.3927F, 0.0F, 0.0F)
        );
        PartDefinition partdefinition3 = partdefinition1.addOrReplaceChild(
                "head",
                CubeListBuilder.create()
                        .texOffs(4, 24)
                        .addBox(-5.0F, -5.0F, -4.2F, 10.0F, 3.0F, 4.0F, new CubeDeformation(0.0F))
                        .texOffs(0, 0)
                        .addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 4.0F, 0.0F)
        );
        partdefinition3.addOrReplaceChild(
                "eyes",
                CubeListBuilder.create()
                        .texOffs(4, 24)
                        .addBox(-5.0F, -5.0F, -4.2F, 10.0F, 3.0F, 4.0F, new CubeDeformation(0.0F))
                        .texOffs(0, 0)
                        .addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 0.0F, 0.0F)
        );
        PartDefinition partdefinition4 = partdefinition.addOrReplaceChild("wind_body", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));
        PartDefinition partdefinition5 = partdefinition4.addOrReplaceChild(
                "wind_bottom",
                CubeListBuilder.create().texOffs(1, 83).addBox(-2.5F, -7.0F, -2.5F, 5.0F, 7.0F, 5.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 24.0F, 0.0F)
        );
        PartDefinition partdefinition6 = partdefinition5.addOrReplaceChild(
                "wind_mid",
                CubeListBuilder.create()
                        .texOffs(74, 28)
                        .addBox(-6.0F, -6.0F, -6.0F, 12.0F, 6.0F, 12.0F, new CubeDeformation(0.0F))
                        .texOffs(78, 32)
                        .addBox(-4.0F, -6.0F, -4.0F, 8.0F, 6.0F, 8.0F, new CubeDeformation(0.0F))
                        .texOffs(49, 71)
                        .addBox(-2.5F, -6.0F, -2.5F, 5.0F, 6.0F, 5.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, -7.0F, 0.0F)
        );
        partdefinition6.addOrReplaceChild(
                "wind_top",
                CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-9.0F, -8.0F, -9.0F, 18.0F, 8.0F, 18.0F, new CubeDeformation(0.0F))
                        .texOffs(6, 6)
                        .addBox(-6.0F, -8.0F, -6.0F, 12.0F, 8.0F, 12.0F, new CubeDeformation(0.0F))
                        .texOffs(105, 57)
                        .addBox(-2.5F, -8.0F, -2.5F, 5.0F, 8.0F, 5.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, -6.0F, 0.0F)
        );
        return LayerDefinition.create(meshdefinition, 32, 32);
    }

    @Override
    public void setupAnim(T pEntity, float pLimbSwing, float pLimbSwingAmount, float pAgeInTicks, float pNetHeadYaw, float pHeadPitch) {
        this.root().getAllParts().forEach(ModelPart::resetPose);
        float f = pAgeInTicks * (float) Math.PI * -0.1F;
        this.windTop.x = Mth.cos(f) * 1.0F * 0.6F;
        this.windTop.z = Mth.sin(f) * 1.0F * 0.6F;
        this.windMid.x = Mth.sin(f) * 0.5F * 0.8F;
        this.windMid.z = Mth.cos(f) * 0.8F;
        this.windBottom.x = Mth.cos(f) * -0.25F * 1.0F;
        this.windBottom.z = Mth.sin(f) * -0.25F * 1.0F;
        this.head.y = 4.0F + Mth.cos(f) / 4.0F;
        this.rods.yRot = pAgeInTicks * (float) Math.PI * 0.1F;
    }

    @Override
    public ModelPart root() {
        return this.root;
    }

    public ModelPart head() {
        return this.head;
    }

    public ModelPart eyes() {
        return this.eyes;
    }

    public ModelPart rods() {
        return this.rods;
    }

    public ModelPart wind() {
        return this.wind;
    }
}
