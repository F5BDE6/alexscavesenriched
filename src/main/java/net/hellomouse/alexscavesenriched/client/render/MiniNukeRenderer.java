package net.hellomouse.alexscavesenriched.client.render;

import net.hellomouse.alexscavesenriched.ACEBlockRegistry;
import net.hellomouse.alexscavesenriched.entity.MiniNukeEntity;
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

public class MiniNukeRenderer extends EntityRenderer<MiniNukeEntity> {
    public MiniNukeRenderer(EntityRendererProvider.Context context) {
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

    public void render(MiniNukeEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource source, int lightIn) {
        super.render(entity, entityYaw, partialTicks, poseStack, source, lightIn);
        float progress = ((float) entity.tickCount + partialTicks) / MiniNukeEntity.DEFAULT_FUSE;
        float expandScale = 1.0F + (float)Math.sin((double)(progress * progress) * Math.PI) * 0.5F;
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees((float) (Math.cos((double) entity.tickCount * 3.25) * 1.2000000476837158 * (double) progress * Math.PI)));
        poseStack.scale(1.0F + progress * 0.03F, 1.0F, 1.0F + progress * 0.03F);
        poseStack.pushPose();
        poseStack.scale(expandScale, expandScale - progress * 0.3F, expandScale);
        poseStack.translate(-0.5, 0.0, -0.5);
        BlockState state = ACEBlockRegistry.MINI_NUKE.get().defaultBlockState();
        BakedModel bakedmodel = Minecraft.getInstance().getBlockRenderer().getBlockModel(state);
        float f = 1.0F - progress * 0.5F;
        float f1 = 1.0F + progress;
        float f2 = 1.0F - progress;

        for (RenderType rt : bakedmodel.getRenderTypes(state, RandomSource.create(42L), ModelData.EMPTY)) {
            renderModel(poseStack.last(), source.getBuffer(RenderTypeHelper.getEntityRenderType(rt, false)),
                    state, bakedmodel, f, f1, f2, 240, OverlayTexture.NO_OVERLAY, ModelData.EMPTY, rt);
        }
        poseStack.popPose();
        poseStack.popPose();
    }

    @Override
    public ResourceLocation getTexture(MiniNukeEntity entity) {
        return TextureAtlas.LOCATION_BLOCKS;
    }
}
