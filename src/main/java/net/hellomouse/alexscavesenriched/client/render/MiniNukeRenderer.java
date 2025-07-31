package net.hellomouse.alexscavesenriched.client.render;

import net.hellomouse.alexscavesenriched.ACEBlockRegistry;
import net.hellomouse.alexscavesenriched.entity.MiniNukeEntity;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.random.Random;
import net.minecraftforge.client.RenderTypeHelper;
import net.minecraftforge.client.model.data.ModelData;

import javax.annotation.Nullable;
import java.util.List;

public class MiniNukeRenderer extends EntityRenderer<MiniNukeEntity> {
    public MiniNukeRenderer(EntityRendererFactory.Context context) {
        super(context);
        this.shadowRadius = 0.0F;
    }

    public void render(MiniNukeEntity entity, float entityYaw, float partialTicks, MatrixStack poseStack, VertexConsumerProvider source, int lightIn) {
        super.render(entity, entityYaw, partialTicks, poseStack, source, lightIn);
        float progress = ((float)entity.age + partialTicks) / MiniNukeEntity.DEFAULT_FUSE;
        float expandScale = 1.0F + (float)Math.sin((double)(progress * progress) * Math.PI) * 0.5F;
        poseStack.push();
        poseStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((float)(Math.cos((double)entity.age * 3.25) * 1.2000000476837158 * (double)progress * Math.PI)));
        poseStack.scale(1.0F + progress * 0.03F, 1.0F, 1.0F + progress * 0.03F);
        poseStack.push();
        poseStack.scale(expandScale, expandScale - progress * 0.3F, expandScale);
        poseStack.translate(-0.5, 0.0, -0.5);
        BlockState state = ACEBlockRegistry.MINI_NUKE.get().getDefaultState();
        BakedModel bakedmodel = MinecraftClient.getInstance().getBlockRenderManager().getModel(state);
        float f = 1.0F - progress * 0.5F;
        float f1 = 1.0F + progress;
        float f2 = 1.0F - progress;

        for (RenderLayer rt : bakedmodel.getRenderTypes(state, Random.create(42L), ModelData.EMPTY)) {
            renderModel(poseStack.peek(), source.getBuffer(RenderTypeHelper.getEntityRenderType(rt, false)),
                    state, bakedmodel, f, f1, f2, 240, OverlayTexture.DEFAULT_UV, ModelData.EMPTY, rt);
        }
        poseStack.pop();
        poseStack.pop();
    }


    public static void renderModel(MatrixStack.Entry p_111068_, VertexConsumer p_111069_, @Nullable BlockState p_111070_, BakedModel p_111071_, float p_111072_, float p_111073_, float p_111074_, int p_111075_, int p_111076_, ModelData modelData, RenderLayer renderType) {
        Random randomsource = Random.create();
        Direction[] var14 = Direction.values();
        for (Direction direction : var14) {
            randomsource.setSeed(42L);
            renderQuadList(p_111068_, p_111069_, p_111072_, p_111073_, p_111074_, p_111071_.getQuads(p_111070_, direction, randomsource, modelData, renderType), p_111075_, p_111076_);
        }
        randomsource.setSeed(42L);
        renderQuadList(p_111068_, p_111069_, p_111072_, p_111073_, p_111074_, p_111071_.getQuads(p_111070_, null, randomsource, modelData, renderType), p_111075_, p_111076_);
    }

    private static void renderQuadList(MatrixStack.Entry p_111059_, VertexConsumer p_111060_, float p_111061_, float p_111062_, float p_111063_, List<BakedQuad> p_111064_, int p_111065_, int p_111066_) {
        for (BakedQuad bakedquad : p_111064_) {
            float f = MathHelper.clamp(p_111061_, 0.0F, 1.0F);
            float f1 = MathHelper.clamp(p_111062_, 0.0F, 1.0F);
            float f2 = MathHelper.clamp(p_111063_, 0.0F, 1.0F);
            p_111060_.quad(p_111059_, bakedquad, f, f1, f2, p_111065_, p_111066_);
        }
    }

    @Override
    public Identifier getTexture(MiniNukeEntity entity) {
        return SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE;
    }
}
