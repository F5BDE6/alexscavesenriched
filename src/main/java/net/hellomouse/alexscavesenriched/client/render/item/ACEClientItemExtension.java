package net.hellomouse.alexscavesenriched.client.render.item;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Arm;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

@OnlyIn(Dist.CLIENT)
public class ACEClientItemExtension implements IClientItemExtensions {
    public static final ACEClientItemExtension INSTANCE = new ACEClientItemExtension();

    @Override
    public boolean applyForgeHandTransform(MatrixStack poseStack, ClientPlayerEntity player, Arm arm, ItemStack itemInHand, float partialTick, float equipProcess, float swingProcess) {
        int i = arm == Arm.RIGHT ? 1 : -1;
        poseStack.translate(i * 0.56F, -0.52F, -0.72F);
        return true;
    }
}
