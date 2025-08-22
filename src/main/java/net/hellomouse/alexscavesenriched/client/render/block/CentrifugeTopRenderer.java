package net.hellomouse.alexscavesenriched.client.render.block;

import net.hellomouse.alexscavesenriched.ACEBlockRegistry;
import net.hellomouse.alexscavesenriched.block.block_entity.CentrifugeBlockEntity;
import net.hellomouse.alexscavesenriched.block.block_entity.CentrifugeInventoryProxyBlockEntity;
import net.hellomouse.alexscavesenriched.block.centrifuge.CentrifugeMultiBlockProxyBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.entity.BlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import java.util.Objects;

public class CentrifugeTopRenderer implements BlockEntityRenderer<CentrifugeInventoryProxyBlockEntity> {
    public CentrifugeTopRenderer(BlockEntityRendererProvider.Context ctx) {
    }

    @Override
    public void render(
            CentrifugeInventoryProxyBlockEntity entity,
            float tickDelta,
            PoseStack matrices,
            MultiBufferSource vertexConsumers,
            int light,
            int overlay
    ) {
        // Rotational part
        float angle = 0F;
        if (entity.getTargetPos() != null) {
            var be = Objects.requireNonNull(entity.getLevel()).getBlockEntity(entity.getTargetPos());
            if (be instanceof CentrifugeBlockEntity centBe)
                angle = centBe.getRotation() + tickDelta * CentrifugeBlockEntity.ANGLE_PER_TICK * (centBe.getSpinSpeed() / ((float)CentrifugeBlockEntity.MAX_SPIN_SPEED));
        }

        matrices.pushPose();
        matrices.translate(0.5, 0, 0.5);
        matrices.scale(0.75F, 1F, 0.75F);
        matrices.mulPose(Axis.YP.rotationDegrees(angle));
        matrices.translate(-0.5, 0, -0.5);
        Minecraft.getInstance().getBlockRenderer().renderSingleBlock(ACEBlockRegistry.CENTRIFUGE_TOP.get().defaultBlockState(), matrices, vertexConsumers, light, overlay);
        matrices.popPose();
    }
}
