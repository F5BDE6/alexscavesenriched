package net.hellomouse.alexscavesenriched.client.render;

import net.hellomouse.alexscavesenriched.ACEBlockRegistry;
import net.hellomouse.alexscavesenriched.entity.BlackHoleBombEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.RenderTypeHelper;
import net.minecraftforge.client.model.data.ModelData;

import javax.annotation.Nullable;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import java.util.List;

public class BlackHoleBombRenderer extends EntityRenderer<BlackHoleBombEntity> {
    public BlackHoleBombRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.0F;
    }

    public static void renderModel(PoseStack.Pose p_111068_, VertexConsumer p_111069_, @Nullable BlockState p_111070_, BakedModel p_111071_, float p_111072_, float p_111073_, float p_111074_, int p_111075_, int p_111076_, ModelData modelData, RenderType renderType) {
        RandomSource randomsource = RandomSource.create();
        Direction[] var14 = Direction.values();
        for (Direction direction : var14) {
            randomsource.setSeed(42L);
            renderQuadList(p_111068_, p_111069_, p_111072_, p_111073_, p_111074_, p_111071_.getQuads(p_111070_, direction, randomsource, modelData, renderType), p_111075_, p_111076_);
        }
        randomsource.setSeed(42L);
        renderQuadList(p_111068_, p_111069_, p_111072_, p_111073_, p_111074_, p_111071_.getQuads(p_111070_, null, randomsource, modelData, renderType), p_111075_, p_111076_);
    }

    private static void renderQuadList(PoseStack.Pose p_111059_, VertexConsumer p_111060_, float p_111061_, float p_111062_, float p_111063_, List<BakedQuad> p_111064_, int p_111065_, int p_111066_) {
        for (BakedQuad bakedquad : p_111064_) {
            float f = Mth.clamp(p_111061_, 0.0F, 1.0F);
            float f1 = Mth.clamp(p_111062_, 0.0F, 1.0F);
            float f2 = Mth.clamp(p_111063_, 0.0F, 1.0F);
            p_111060_.putBulkData(p_111059_, bakedquad, f, f1, f2, p_111065_, p_111066_);
        }
    }

    public void render(BlackHoleBombEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int lighting) {
        super.render(entity, entityYaw, partialTicks, poseStack, bufferSource, lighting);
        float progress = (entity.getFuse() + partialTicks) / BlackHoleBombEntity.DEFAULT_FUSE;
        float expandScale = 1F + (float) Math.sin(progress * progress * Math.PI) * 0.5F;
        float spinSpeed = entity.tickCount * (10F * progress * progress);

        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(spinSpeed));
        poseStack.scale(progress, progress, progress);
        poseStack.pushPose();
        poseStack.scale(expandScale, expandScale - progress * 0.3F, expandScale);
        poseStack.translate(-0.5, 0.0, -0.5);

        BlockState state = (ACEBlockRegistry.BLACK_HOLE_BOMB.get()).defaultBlockState();
        BakedModel bakedmodel = Minecraft.getInstance().getBlockRenderer().getBlockModel(state);

        float gre = 1.0F - progress * 0.5F;
        float red = 1.0F + progress;
        float blu = 1.0F - progress;

        for (RenderType rt : bakedmodel.getRenderTypes(state, RandomSource.create(42L), ModelData.EMPTY))
            renderModel(poseStack.last(), bufferSource.getBuffer(RenderTypeHelper.getEntityRenderType(rt, false)), state, bakedmodel, red, blu, gre, 240, OverlayTexture.NO_OVERLAY, ModelData.EMPTY, rt);

        poseStack.popPose();
        poseStack.popPose();
    }

    @Override
    public ResourceLocation getTexture(BlackHoleBombEntity entity) {
        return TextureAtlas.LOCATION_BLOCKS;
    }
}
