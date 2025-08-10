package net.hellomouse.alexscavesenriched.client.render.block;

import net.hellomouse.alexscavesenriched.ACEBlockRegistry;
import net.hellomouse.alexscavesenriched.block.block_entity.CentrifugeBlockEntity;
import net.hellomouse.alexscavesenriched.block.block_entity.CentrifugeInventoryProxyBlockEntity;
import net.hellomouse.alexscavesenriched.block.centrifuge.CentrifugeMultiBlockProxyBlock;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.RotationAxis;

import java.util.Objects;

public class CentrifugeTopRenderer implements BlockEntityRenderer<CentrifugeInventoryProxyBlockEntity> {
    public CentrifugeTopRenderer(BlockEntityRendererFactory.Context ctx) {}

    @Override
    public void render(
            CentrifugeInventoryProxyBlockEntity entity,
            float tickDelta,
            MatrixStack matrices,
            VertexConsumerProvider vertexConsumers,
            int light,
            int overlay
    ) {
        // Rotational part
        float angle = 0F;
        if (entity.getTargetPos() != null) {
            var be = Objects.requireNonNull(entity.getWorld()).getBlockEntity(entity.getTargetPos());
            if (be instanceof CentrifugeBlockEntity centBe)
                angle = centBe.getRotation() + tickDelta * CentrifugeBlockEntity.ANGLE_PER_TICK * (centBe.getSpinSpeed() / ((float)CentrifugeBlockEntity.MAX_SPIN_SPEED));
        }

        matrices.push();
        matrices.translate(0.5, 0, 0.5);
        matrices.scale(0.75F, 1F, 0.75F);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(angle));
        matrices.translate(-0.5, 0, -0.5);
        MinecraftClient.getInstance().getBlockRenderManager().renderBlockAsEntity(ACEBlockRegistry.CENTRIFUGE_TOP.get().getDefaultState(), matrices, vertexConsumers, light, overlay);
        matrices.pop();
    }
}
