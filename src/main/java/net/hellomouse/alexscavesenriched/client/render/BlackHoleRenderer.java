package net.hellomouse.alexscavesenriched.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.hellomouse.alexscavesenriched.AlexsCavesEnriched;
import net.hellomouse.alexscavesenriched.client.entity.BlackHoleDiskModel;
import net.hellomouse.alexscavesenriched.client.entity.BlackHoleModel;
import net.hellomouse.alexscavesenriched.entity.BlackHoleEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

public class BlackHoleRenderer extends EntityRenderer<BlackHoleEntity> {
    protected final BlackHoleModel<BlackHoleEntity> MODEL;
    protected final BlackHoleDiskModel<BlackHoleEntity> MODEL_DISK;

    public BlackHoleRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager);
        MODEL = new BlackHoleModel<>(renderManager.bakeLayer(BlackHoleModel.LAYER_LOCATION));
        MODEL_DISK = new BlackHoleDiskModel<>(renderManager.bakeLayer(BlackHoleDiskModel.LAYER_LOCATION));
    }

    @Override
    public void render(BlackHoleEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int lighting) {
        float ageInTicks = entity.tickCount + partialTicks;
        float alpha = 1.0F;
        float s = entity.getCurrentSize();
        RenderType renderType = RenderType.entityCutout(this.getTextureLocation(entity));
        VertexConsumer vertexConsumer = bufferSource.getBuffer(renderType);

        if (entity.getDecayDurationLeft() < 60)
            s *= Math.pow(entity.getDecayDurationLeft() / 60F, 4);

        poseStack.pushPose();
        final float pitch =  20F;
        Vec3 scaleCenter = new Vec3(-0.5D, 0.75D, 0D);
        poseStack.translate(0.5D, -0.6F, 0D);

        poseStack.translate(scaleCenter.x, scaleCenter.y, scaleCenter.z);
        poseStack.scale(s, s, s);
        poseStack.translate(-scaleCenter.x, -scaleCenter.y, -scaleCenter.z);
        poseStack.mulPose(Axis.ZP.rotationDegrees(pitch));

        // The hole
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(70.0F * ageInTicks));
        MODEL.setupAnim(entity, 0.0F, 0.0F, ageInTicks, 0.0F, 0.0F);
        MODEL.renderToBuffer(poseStack, vertexConsumer, 15728880, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, alpha);
        poseStack.popPose();

        // The disk, drawn as a sandwich since emmisive transparent layer is behind water
        MODEL_DISK.setupAnim(entity, 0.0F, 0.0F, ageInTicks, 0.0F, 0.0F);
        drawDisk(0, ageInTicks, vertexConsumer, poseStack);

        RenderType renderTypeEmissive = RenderType.entityTranslucentEmissive(this.getTextureLocation(entity));
        VertexConsumer vertexConsumerEmissive = bufferSource.getBuffer(renderTypeEmissive);
        drawDisk(1, ageInTicks, vertexConsumerEmissive, poseStack);
        drawDisk(-1, ageInTicks, vertexConsumerEmissive, poseStack);

        poseStack.popPose();
        super.render(entity, entityYaw, partialTicks, poseStack, bufferSource, lighting);
    }

    @Override
    public ResourceLocation getTextureLocation(BlackHoleEntity entity) {
        return ResourceLocation.fromNamespaceAndPath(AlexsCavesEnriched.MODID, "textures/entity/blackhole.png");
    }

    private void drawDisk(int offset, float ageInTicks, VertexConsumer vertexConsumer, PoseStack poseStack) {
        poseStack.pushPose();
        poseStack.translate(0, offset * 0.01F, 0);
        poseStack.mulPose(Axis.YP.rotationDegrees((-70.0F + offset) * ageInTicks));
        MODEL_DISK.renderToBuffer(poseStack, vertexConsumer, 15728880, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
        poseStack.popPose();
    }


}
