package net.hellomouse.alexscavesenriched.client.render;

import net.hellomouse.alexscavesenriched.AlexsCavesEnriched;
import net.hellomouse.alexscavesenriched.client.entity.BlackHoleDiskModel;
import net.hellomouse.alexscavesenriched.client.entity.BlackHoleModel;
import net.hellomouse.alexscavesenriched.entity.BlackHoleEntity;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;

public class BlackHoleRenderer extends EntityRenderer<BlackHoleEntity> {
    protected final BlackHoleModel<BlackHoleEntity> MODEL;
    protected final BlackHoleDiskModel<BlackHoleEntity> MODEL_DISK;

    public BlackHoleRenderer(EntityRendererFactory.Context renderManager) {
        super(renderManager);
        MODEL = new BlackHoleModel<>(renderManager.getPart(BlackHoleModel.LAYER_LOCATION));
        MODEL_DISK = new BlackHoleDiskModel<>(renderManager.getPart(BlackHoleDiskModel.LAYER_LOCATION));
    }

    @Override
    public void render(BlackHoleEntity entity, float entityYaw, float partialTicks, MatrixStack poseStack, VertexConsumerProvider bufferSource, int lighting) {
        float ageInTicks = entity.age + partialTicks;
        float alpha = 1.0F;
        float s = entity.getCurrentSize();
        RenderLayer renderType = RenderLayer.getEntityCutout(this.getTexture(entity));
        VertexConsumer vertexConsumer = bufferSource.getBuffer(renderType);

        if (entity.getDecayDurationLeft() < 60)
            s *= Math.pow(entity.getDecayDurationLeft() / 60F, 4);

        poseStack.push();
        final float pitch =  20F;
        Vec3d scaleCenter = new Vec3d(-0.5D, 0.75D, 0D);
        poseStack.translate(0.5D, -0.6F, 0D);

        poseStack.translate(scaleCenter.x, scaleCenter.y, scaleCenter.z);
        poseStack.scale(s, s, s);
        poseStack.translate(-scaleCenter.x, -scaleCenter.y, -scaleCenter.z);
        poseStack.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(pitch));

        // The hole
        poseStack.push();
        poseStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(70.0F * ageInTicks));
        MODEL.setAngles(entity, 0.0F, 0.0F, ageInTicks, 0.0F, 0.0F);
        MODEL.render(poseStack, vertexConsumer, 15728880, OverlayTexture.DEFAULT_UV, 1.0F, 1.0F, 1.0F, alpha);
        poseStack.pop();

        // The disk, drawn as a sandwich since emmisive transparent layer is behind water
        MODEL_DISK.setAngles(entity, 0.0F, 0.0F, ageInTicks, 0.0F, 0.0F);
        drawDisk(0, ageInTicks, vertexConsumer, poseStack);

        RenderLayer renderTypeEmissive = RenderLayer.getEntityTranslucentEmissive(this.getTexture(entity));
        VertexConsumer vertexConsumerEmissive = bufferSource.getBuffer(renderTypeEmissive);
        drawDisk(1, ageInTicks, vertexConsumerEmissive, poseStack);
        drawDisk(-1, ageInTicks, vertexConsumerEmissive, poseStack);

        poseStack.pop();
        super.render(entity, entityYaw, partialTicks, poseStack, bufferSource, lighting);
    }

    private void drawDisk(int offset, float ageInTicks, VertexConsumer vertexConsumer, MatrixStack poseStack) {
        poseStack.push();
        poseStack.translate(0, offset * 0.01F, 0);
        poseStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((-70.0F + offset) * ageInTicks));
        MODEL_DISK.render(poseStack, vertexConsumer, 15728880, OverlayTexture.DEFAULT_UV, 1.0F, 1.0F, 1.0F, 1.0F);
        poseStack.pop();
    }

    @Override
    public Identifier getTexture(BlackHoleEntity entity) {
        return Identifier.fromNamespaceAndPath(AlexsCavesEnriched.MODID, "textures/entity/blackhole.png");
    }
}
