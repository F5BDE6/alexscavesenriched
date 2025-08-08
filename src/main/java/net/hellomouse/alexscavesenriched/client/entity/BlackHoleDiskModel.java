package net.hellomouse.alexscavesenriched.client.entity;

import net.hellomouse.alexscavesenriched.AlexsCavesEnriched;
import net.minecraft.client.model.*;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.Identifier;

public class BlackHoleDiskModel<T extends Entity> extends EntityModel<T> {
    // This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
    public static final EntityModelLayer LAYER_LOCATION = new EntityModelLayer(Identifier.fromNamespaceAndPath(AlexsCavesEnriched.MODID, "blackhole_disk"), "main");
    private final ModelPart parent;
    private final ModelPart disk;

    public BlackHoleDiskModel(ModelPart root) {
        this.parent = root.getChild("parent");
        this.disk = this.parent.getChild("disk");
    }

    public static TexturedModelData createBodyLayer() {
        ModelData meshdefinition = new ModelData();
        ModelPartData partdefinition = meshdefinition.getRoot();

        ModelPartData parent = partdefinition.addChild("parent", ModelPartBuilder.create(), ModelTransform.of(0.0F, 24.0F, 0.0F, 0F, 0.0F, 0.0F));
        ModelPartData disk = parent.addChild("disk", ModelPartBuilder.create().uv(-64, 48)
                .cuboid(-31.0F, -4.0F, -33.0F, 64.0F, 0.0F, 64.0F, new Dilation(0.0F)),
                ModelTransform.pivot(0.0F, 0.0F, 0.0F));
        return TexturedModelData.of(meshdefinition, 128, 128);
    }

    @Override
    public void setAngles(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
    }

    @Override
    public void render(MatrixStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        parent.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}
