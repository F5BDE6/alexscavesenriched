package net.hellomouse.alexscavesenriched.client.render;

import net.hellomouse.alexscavesenriched.AlexsCavesEnriched;
import net.hellomouse.alexscavesenriched.client.entity.RocketModel;
import net.hellomouse.alexscavesenriched.client.entity.RocketNuclearModel;
import net.hellomouse.alexscavesenriched.entity.RocketEntity;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraftforge.client.ForgeRenderTypes;

public class RocketRenderer extends EntityRenderer<RocketEntity> {
    protected final RocketModel MODEL;
    protected final RocketNuclearModel NUCLEAR_ROCKET_MODEL;

    public RocketRenderer(EntityRendererFactory.Context renderManager) {
        super(renderManager);
        MODEL = new RocketModel(renderManager.getPart(RocketModel.LAYER_LOCATION));
        NUCLEAR_ROCKET_MODEL = new RocketNuclearModel(renderManager.getPart(RocketNuclearModel.LAYER_LOCATION));
    }

    @Override
    public void render(RocketEntity entity, float entityYaw, float partialTicks, MatrixStack poseStack, VertexConsumerProvider bufferSource, int lighting) {
        poseStack.push();
        poseStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(MathHelper.lerp(partialTicks, entity.prevYaw, entity.getYaw())));
        poseStack.multiply(RotationAxis.NEGATIVE_X.rotationDegrees(MathHelper.lerp(partialTicks, entity.prevPitch, entity.getPitch())));
        poseStack.translate(0.0D, 1.5F, -0.15D);
        poseStack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(180));
        float f9 = (float) entity.shake - partialTicks;
        if (f9 > 0.0F) {
            float f10 = -MathHelper.sin(f9 * 3.0F) * f9;
            poseStack.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(f10));
        }
        float ageInTicks = entity.age + partialTicks;
        float alpha = 1.0F;
        RenderLayer renderType = ForgeRenderTypes.getUnlitTranslucent(this.getTexture(entity));
        VertexConsumer vertexconsumer = bufferSource.getBuffer(renderType);

        if (entity.getIsNuclear() || entity.getIsNeutron()) {
            NUCLEAR_ROCKET_MODEL.setAngles(entity, 0.0F, 0.0F, ageInTicks, 0.0F, 0.0F);
            NUCLEAR_ROCKET_MODEL.render(poseStack, vertexconsumer, 240, OverlayTexture.DEFAULT_UV, 1.0F, 1.0F, 1.0F, alpha);
        } else {
            MODEL.setAngles(entity, 0.0F, 0.0F, ageInTicks, 0.0F, 0.0F);
            MODEL.render(poseStack, vertexconsumer, 240, OverlayTexture.DEFAULT_UV, 1.0F, 1.0F, 1.0F, alpha);
        }
        poseStack.pop();
        super.render(entity, entityYaw, partialTicks, poseStack, bufferSource, lighting);
    }

    @Override
    public Identifier getTexture(RocketEntity entity) {
        if (entity.getIsRadioactive())
            return Identifier.fromNamespaceAndPath(AlexsCavesEnriched.MODID, "textures/entity/rocket_entity.png");
        else if (entity.getIsNuclear())
            return Identifier.fromNamespaceAndPath(AlexsCavesEnriched.MODID, "textures/entity/rocket_entity_nuclear.png");
        else if (entity.getIsNeutron())
            return Identifier.fromNamespaceAndPath(AlexsCavesEnriched.MODID, "textures/entity/rocket_entity_neutron.png");
        return Identifier.fromNamespaceAndPath(AlexsCavesEnriched.MODID, "textures/entity/rocket_entity_normal.png");
    }
}
