package net.hellomouse.alexscavesenriched.client.render;

import net.hellomouse.alexscavesenriched.AlexsCavesEnriched;
import net.hellomouse.alexscavesenriched.entity.UraniumArrowEntity;
import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class UraniumArrowRenderer extends ArrowRenderer<UraniumArrowEntity> {
    public static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(AlexsCavesEnriched.MODID, "textures/entity/uranium_arrow_entity.png");

    public UraniumArrowRenderer(EntityRendererProvider.Context manager) {
        super(manager);
    }

    @Override
    public ResourceLocation getTexture(UraniumArrowEntity entity) {
        return TEXTURE;
    }

}