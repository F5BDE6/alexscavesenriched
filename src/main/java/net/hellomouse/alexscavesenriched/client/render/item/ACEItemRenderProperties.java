package net.hellomouse.alexscavesenriched.client.render.item;

import net.minecraft.client.render.item.BuiltinModelItemRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

@OnlyIn(Dist.CLIENT)
public class ACEItemRenderProperties implements IClientItemExtensions {
    public BuiltinModelItemRenderer getCustomRenderer() {
        return new ACEItemstackRenderer();
    }
}
