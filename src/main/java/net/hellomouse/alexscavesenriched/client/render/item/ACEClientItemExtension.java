package net.hellomouse.alexscavesenriched.client.render.item;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

@OnlyIn(Dist.CLIENT)
public class ACEClientItemExtension implements IClientItemExtensions {
    public static final ACEClientItemExtension INSTANCE = new ACEClientItemExtension();

    @Override
    public BlockEntityWithoutLevelRenderer getCustomRenderer() {
        return new ACEItemstackRenderer();
    }

    @Override
    public boolean applyForgeHandTransform(PoseStack poseStack, LocalPlayer player, HumanoidArm arm, ItemStack itemInHand, float partialTick, float equipProcess, float swingProcess) {
        int i = arm == HumanoidArm.RIGHT ? 1 : -1;
        poseStack.translate(i * 0.56F, -0.52F, -0.72F);
        return true;
    }
}
