package net.hellomouse.alexscavesenriched.client.render.item;

import com.github.alexmodguy.alexscaves.client.render.ACRenderTypes;
import com.github.alexmodguy.alexscaves.server.enchantment.ACEnchantmentRegistry;
import com.github.alexmodguy.alexscaves.server.item.RaygunItem;
import net.hellomouse.alexscavesenriched.ACEItemRegistry;
import net.hellomouse.alexscavesenriched.client.model.RailgunModel;
import net.hellomouse.alexscavesenriched.client.model.RaygunMk2Model;
import net.hellomouse.alexscavesenriched.item.RailgunItem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.render.item.BuiltinModelItemRenderer;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ACEItemstackRenderer extends BuiltinModelItemRenderer {
    private static final Identifier RAYGUN_TEXTURE = Identifier.parse("alexscavesenriched:textures/entity/raygun/base.png");
    private static final Identifier RAYGUN_ACTIVE_TEXTURE = Identifier.parse("alexscavesenriched:textures/entity/raygun/base_on.png");
    private static final Identifier RAYGUN_BLUE_TEXTURE = Identifier.parse("alexscavesenriched:textures/entity/raygun/gamma.png");
    private static final Identifier RAYGUN_BLUE_ACTIVE_TEXTURE = Identifier.parse("alexscavesenriched:textures/entity/raygun/gamma_on.png");

    private static final Identifier RAILGUN_TEXTURE = Identifier.parse("alexscavesenriched:textures/entity/railgun/railgun_entity.png");
    private static final Identifier RAILGUN_GLOW_TEXTURE0 = Identifier.parse("alexscavesenriched:textures/entity/railgun/glow0.png");
    private static final Identifier RAILGUN_GLOW_TEXTURE1 = Identifier.parse("alexscavesenriched:textures/entity/railgun/glow1.png");
    private static final Identifier RAILGUN_GLOW_TEXTURE2 = Identifier.parse("alexscavesenriched:textures/entity/railgun/glow2.png");
    private static final Identifier RAILGUN_GLOW_TEXTURE3 = Identifier.parse("alexscavesenriched:textures/entity/railgun/glow3.png");

    private static final RaygunMk2Model RAYGUN_MK2_MODEL = new RaygunMk2Model();
    private static final RailgunModel RAILGUN_MODEL = new RailgunModel();

    public ACEItemstackRenderer() {
        super(null, null);
    }

    @Override
    public void render(ItemStack itemStackIn, ModelTransformationMode transformType, MatrixStack poseStack, VertexConsumerProvider bufferIn, int combinedLightIn, int combinedOverlayIn) {
        float partialTick = MinecraftClient.getInstance().getPartialTick();

        float ageInTicks;
        float pullAmount;
        float pulseAlpha;

        Identifier eyeTexture;
        if (itemStackIn.isOf(ACEItemRegistry.RAYGUN.get())) {
            ageInTicks = MinecraftClient.getInstance().player == null ? 0.0F : (float)MinecraftClient.getInstance().player.age + partialTick;
            pullAmount = RaygunItem.getLerpedUseTime(itemStackIn, partialTick) / 5.0F;
            pulseAlpha = pullAmount * (0.25F + 0.25F * (float)(1.0 + Math.sin(ageInTicks * 0.8F)));

            poseStack.push();
            poseStack.translate(0.85F, 0.5F, 0.5F);
            poseStack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-180.0F));
            poseStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180.0F));
            poseStack.push();
            poseStack.scale(0.9F, 0.9F, 0.9F);
            RAYGUN_MK2_MODEL.setAngles(null, pullAmount, ageInTicks, 0.0F, 0.0F, 0.0F);
            boolean gamma = itemStackIn.getEnchantmentLevel(ACEnchantmentRegistry.GAMMA_RAY.get()) > 0;

            Identifier texture = gamma ? RAYGUN_BLUE_TEXTURE : RAYGUN_TEXTURE;
            eyeTexture = gamma ? RAYGUN_BLUE_ACTIVE_TEXTURE : RAYGUN_ACTIVE_TEXTURE;
            RAYGUN_MK2_MODEL.render(poseStack, getVertexConsumerFoil(bufferIn, RenderLayer.getEntityCutoutNoCull(texture), texture, itemStackIn.hasGlint()), combinedLightIn, combinedOverlayIn, 1.0F, 1.0F, 1.0F, 1.0F);
            RAYGUN_MK2_MODEL.render(poseStack, getVertexConsumer(bufferIn, ACRenderTypes.getEyesAlphaEnabled(eyeTexture), eyeTexture), combinedLightIn, combinedOverlayIn, 1.0F, 1.0F, 1.0F, pulseAlpha);
            poseStack.pop();
            poseStack.pop();
        }
        else if (itemStackIn.isOf(ACEItemRegistry.RAILGUN.get())) {
            ageInTicks = MinecraftClient.getInstance().player == null ? 0.0F : (float)MinecraftClient.getInstance().player.age + partialTick;
            pullAmount = RaygunItem.getLerpedUseTime(itemStackIn, partialTick) / 5.0F;
            pulseAlpha = (float)RailgunItem.getCharge(itemStackIn) / RailgunItem.MAX_CHARGE;

            poseStack.push();
            poseStack.translate(0F, 1.5F, 0F);
            poseStack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-180.0F));
            poseStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180.0F));

            RAILGUN_MODEL.setAngles(null, pullAmount, ageInTicks, 0.0F, 0.0F, 0.0F);

            Identifier texture = RAILGUN_TEXTURE;
            if (pulseAlpha < 0.25)
                eyeTexture = RAILGUN_GLOW_TEXTURE0;
            else if (pulseAlpha < 0.5)
                eyeTexture = RAILGUN_GLOW_TEXTURE1;
            else if (pulseAlpha < 0.75)
                eyeTexture = RAILGUN_GLOW_TEXTURE2;
            else
                eyeTexture = RAILGUN_GLOW_TEXTURE3;
            RAILGUN_MODEL.render(poseStack, getVertexConsumerFoil(bufferIn, RenderLayer.getEntityCutoutNoCull(texture), texture, itemStackIn.hasGlint()), combinedLightIn, combinedOverlayIn, 1.0F, 1.0F, 1.0F, 1.0F);
            RAILGUN_MODEL.render(poseStack, getVertexConsumer(bufferIn, ACRenderTypes.getEyesAlphaEnabled(eyeTexture), eyeTexture), combinedLightIn, combinedOverlayIn, 1.0F, 1.0F, 1.0F, pulseAlpha * 0.8F);
            poseStack.pop();
        }
    }

    private static VertexConsumer getVertexConsumerFoil(VertexConsumerProvider bufferIn, RenderLayer _default, Identifier resourceLocation, boolean foil) {
        return ItemRenderer.getItemGlintConsumer(bufferIn, _default, false, foil);
    }

    private static VertexConsumer getVertexConsumer(VertexConsumerProvider bufferIn, RenderLayer _default, Identifier resourceLocation) {
        return bufferIn.getBuffer(_default);
    }
}
