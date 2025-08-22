package net.hellomouse.alexscavesenriched.client.render.item;

import com.github.alexmodguy.alexscaves.client.render.ACRenderTypes;
import com.github.alexmodguy.alexscaves.server.enchantment.ACEnchantmentRegistry;
import com.github.alexmodguy.alexscaves.server.item.RaygunItem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.hellomouse.alexscavesenriched.ACEItemRegistry;
import net.hellomouse.alexscavesenriched.client.model.RailgunModel;
import net.hellomouse.alexscavesenriched.client.model.RaygunMk2Model;
import net.hellomouse.alexscavesenriched.item.RailgunItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.render.*;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ACEItemstackRenderer extends BlockEntityWithoutLevelRenderer {
    private static final ResourceLocation RAYGUN_TEXTURE = ResourceLocation.parse("alexscavesenriched:textures/entity/raygun/base.png");
    private static final ResourceLocation RAYGUN_ACTIVE_TEXTURE = ResourceLocation.parse("alexscavesenriched:textures/entity/raygun/base_on.png");
    private static final ResourceLocation RAYGUN_BLUE_TEXTURE = ResourceLocation.parse("alexscavesenriched:textures/entity/raygun/gamma.png");
    private static final ResourceLocation RAYGUN_BLUE_ACTIVE_TEXTURE = ResourceLocation.parse("alexscavesenriched:textures/entity/raygun/gamma_on.png");

    private static final ResourceLocation RAILGUN_TEXTURE = ResourceLocation.parse("alexscavesenriched:textures/entity/railgun/railgun_entity.png");
    private static final ResourceLocation RAILGUN_GLOW_TEXTURE0 = ResourceLocation.parse("alexscavesenriched:textures/entity/railgun/glow0.png");
    private static final ResourceLocation RAILGUN_GLOW_TEXTURE1 = ResourceLocation.parse("alexscavesenriched:textures/entity/railgun/glow1.png");
    private static final ResourceLocation RAILGUN_GLOW_TEXTURE2 = ResourceLocation.parse("alexscavesenriched:textures/entity/railgun/glow2.png");
    private static final ResourceLocation RAILGUN_GLOW_TEXTURE3 = ResourceLocation.parse("alexscavesenriched:textures/entity/railgun/glow3.png");

    private static final RaygunMk2Model RAYGUN_MK2_MODEL = new RaygunMk2Model();
    private static final RailgunModel RAILGUN_MODEL = new RailgunModel();

    public ACEItemstackRenderer() {
        super(null, null);
    }

    private static VertexConsumer getVertexConsumerFoil(MultiBufferSource bufferIn, RenderType _default, ResourceLocation resourceLocation, boolean foil) {
        return ItemRenderer.getFoilBuffer(bufferIn, _default, false, foil);
    }

    private static VertexConsumer getVertexConsumer(MultiBufferSource bufferIn, RenderType _default, ResourceLocation resourceLocation) {
        return bufferIn.getBuffer(_default);
    }

    @Override
    public void renderByItem(ItemStack itemStackIn, ItemDisplayContext transformType, PoseStack poseStack, MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn) {
        float partialTick = Minecraft.getInstance().getPartialTick();

        float ageInTicks;
        float pullAmount;
        float pulseAlpha;

        ResourceLocation eyeTexture;
        if (itemStackIn.is(ACEItemRegistry.RAYGUN.get())) {
            ageInTicks = Minecraft.getInstance().player == null ? 0.0F : (float) Minecraft.getInstance().player.tickCount + partialTick;
            pullAmount = RaygunItem.getLerpedUseTime(itemStackIn, partialTick) / 5.0F;
            pulseAlpha = pullAmount * (0.25F + 0.25F * (float)(1.0 + Math.sin(ageInTicks * 0.8F)));

            poseStack.pushPose();
            poseStack.translate(0.85F, 0.5F, 0.5F);
            poseStack.mulPose(Axis.XP.rotationDegrees(-180.0F));
            poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
            poseStack.pushPose();
            poseStack.scale(0.9F, 0.9F, 0.9F);
            RAYGUN_MK2_MODEL.setupAnim(null, pullAmount, ageInTicks, 0.0F, 0.0F, 0.0F);
            boolean gamma = itemStackIn.getEnchantmentLevel(ACEnchantmentRegistry.GAMMA_RAY.get()) > 0;

            ResourceLocation texture = gamma ? RAYGUN_BLUE_TEXTURE : RAYGUN_TEXTURE;
            eyeTexture = gamma ? RAYGUN_BLUE_ACTIVE_TEXTURE : RAYGUN_ACTIVE_TEXTURE;
            RAYGUN_MK2_MODEL.renderToBuffer(poseStack, getVertexConsumerFoil(bufferIn, RenderType.entityCutoutNoCull(texture), texture, itemStackIn.hasFoil()), combinedLightIn, combinedOverlayIn, 1.0F, 1.0F, 1.0F, 1.0F);
            RAYGUN_MK2_MODEL.renderToBuffer(poseStack, getVertexConsumer(bufferIn, ACRenderTypes.getEyesAlphaEnabled(eyeTexture), eyeTexture), combinedLightIn, combinedOverlayIn, 1.0F, 1.0F, 1.0F, pulseAlpha);
            poseStack.popPose();
            poseStack.popPose();
        } else if (itemStackIn.is(ACEItemRegistry.RAILGUN.get())) {
            ageInTicks = Minecraft.getInstance().player == null ? 0.0F : (float) Minecraft.getInstance().player.tickCount + partialTick;
            pullAmount = RaygunItem.getLerpedUseTime(itemStackIn, partialTick) / 5.0F;
            pulseAlpha = (float)RailgunItem.getCharge(itemStackIn) / RailgunItem.MAX_CHARGE;

            RailgunItem railgunItem = (RailgunItem)itemStackIn.getItem();
            float raygunFireProgress = (float)railgunItem.getFireTick() / RailgunItem.FIRE_TICK_TIME;
            pulseAlpha = Math.max(pulseAlpha, raygunFireProgress > 0.8 ? 1.5F : raygunFireProgress);

            poseStack.pushPose();
            poseStack.translate(0F, 1.5F, 0F);
            poseStack.mulPose(Axis.XP.rotationDegrees(-180.0F));
            poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));

            RAILGUN_MODEL.setupAnim(null, pullAmount, ageInTicks, 0.0F, 0.0F, 0.0F);

            ResourceLocation texture = RAILGUN_TEXTURE;
            if (pulseAlpha < 0.25)
                eyeTexture = RAILGUN_GLOW_TEXTURE0;
            else if (pulseAlpha < 0.5)
                eyeTexture = RAILGUN_GLOW_TEXTURE1;
            else if (pulseAlpha < 0.75)
                eyeTexture = RAILGUN_GLOW_TEXTURE2;
            else
                eyeTexture = RAILGUN_GLOW_TEXTURE3;

            RAILGUN_MODEL.renderToBuffer(poseStack, getVertexConsumerFoil(bufferIn, RenderType.entityCutoutNoCull(texture), texture, itemStackIn.hasFoil()), combinedLightIn, combinedOverlayIn, 1.0F, 1.0F, 1.0F, 1.0F);
            RAILGUN_MODEL.renderToBuffer(poseStack, getVertexConsumer(bufferIn, ACRenderTypes.getEyesAlphaEnabled(eyeTexture), eyeTexture), combinedLightIn, combinedOverlayIn, 1.0F, 1.0F, 1.0F, Math.min(pulseAlpha * 0.8F, 1F));
            poseStack.popPose();
        }
    }
}
