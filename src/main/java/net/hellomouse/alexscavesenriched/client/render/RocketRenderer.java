package net.hellomouse.alexscavesenriched.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.hellomouse.alexscavesenriched.AlexsCavesEnriched;
import net.hellomouse.alexscavesenriched.client.entity.RocketModel;
import net.hellomouse.alexscavesenriched.client.entity.RocketNuclearModel;
import net.hellomouse.alexscavesenriched.entity.RocketEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class RocketRenderer extends EntityRenderer<RocketEntity> {
    protected final RocketModel MODEL;
    protected final RocketNuclearModel NUCLEAR_ROCKET_MODEL;

    public RocketRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager);
        MODEL = new RocketModel(renderManager.bakeLayer(RocketModel.LAYER_LOCATION));
        NUCLEAR_ROCKET_MODEL = new RocketNuclearModel(renderManager.bakeLayer(RocketNuclearModel.LAYER_LOCATION));
    }

    @Override
    public void render(RocketEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int lighting) {
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(Mth.lerp(partialTicks, entity.yRotO, entity.getYRot())));
        poseStack.mulPose(Axis.XN.rotationDegrees(Mth.lerp(partialTicks, entity.xRotO, entity.getXRot())));
        poseStack.translate(0.0D, 1.5F, -0.15D);
        poseStack.mulPose(Axis.XP.rotationDegrees(180));
        float f9 = (float) entity.shakeTime - partialTicks;
        if (f9 > 0.0F) {
            float f10 = -Mth.sin(f9 * 3.0F) * f9;
            poseStack.mulPose(Axis.ZP.rotationDegrees(f10));
        }
        float ageInTicks = entity.tickCount + partialTicks;
        float alpha = 1.0F;
        RenderType renderType = RenderType.entityCutout(this.getTextureLocation(entity));
        VertexConsumer vertexconsumer = bufferSource.getBuffer(renderType);

        if (entity.getIsNuclear() || entity.getIsNeutron()) {
            NUCLEAR_ROCKET_MODEL.setupAnim(entity, 0.0F, 0.0F, ageInTicks, 0.0F, 0.0F);
            NUCLEAR_ROCKET_MODEL.renderToBuffer(poseStack, vertexconsumer, 240, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, alpha);
        } else {
            MODEL.setupAnim(entity, 0.0F, 0.0F, ageInTicks, 0.0F, 0.0F);
            MODEL.renderToBuffer(poseStack, vertexconsumer, 240, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, alpha);
        }
        poseStack.popPose();
        super.render(entity, entityYaw, partialTicks, poseStack, bufferSource, lighting);
    }

    @Override
    public ResourceLocation getTextureLocation(RocketEntity entity) {
        if (entity.getIsRadioactive() || entity.getIsMiniNuke())
            return ResourceLocation.fromNamespaceAndPath(AlexsCavesEnriched.MODID, "textures/entity/rocket_entity.png");
        else if (entity.getIsNuclear())
            return ResourceLocation.fromNamespaceAndPath(AlexsCavesEnriched.MODID, "textures/entity/rocket_entity_nuclear.png");
        else if (entity.getIsNeutron())
            return ResourceLocation.fromNamespaceAndPath(AlexsCavesEnriched.MODID, "textures/entity/rocket_entity_neutron.png");
        return ResourceLocation.fromNamespaceAndPath(AlexsCavesEnriched.MODID, "textures/entity/rocket_entity_normal.png");
    }
}
