package net.hellomouse.alexscavesenriched.client.render.item;

import com.github.alexmodguy.alexscaves.client.render.ACRenderTypes;
import com.github.alexmodguy.alexscaves.server.enchantment.ACEnchantmentRegistry;
import com.github.alexmodguy.alexscaves.server.item.RaygunItem;
import net.hellomouse.alexscavesenriched.ACEItemRegistry;
import net.hellomouse.alexscavesenriched.client.model.RaygunMk2Model;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
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
    private static final RaygunMk2Model RAYGUN_MK2_MODEL = new RaygunMk2Model();

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
    }

    private static VertexConsumer getVertexConsumerFoil(VertexConsumerProvider bufferIn, RenderLayer _default, Identifier resourceLocation, boolean foil) {
        return ItemRenderer.getItemGlintConsumer(bufferIn, _default, false, foil);
    }

    private static VertexConsumer getVertexConsumer(VertexConsumerProvider bufferIn, RenderLayer _default, Identifier resourceLocation) {
        return bufferIn.getBuffer(_default);
    }
}
