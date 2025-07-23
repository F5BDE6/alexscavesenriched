package net.hellomouse.alexscavesenriched.client.render;

import net.hellomouse.alexscavesenriched.AlexsCavesEnriched;
import net.hellomouse.alexscavesenriched.entity.UraniumArrowEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.ProjectileEntityRenderer;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

public class UraniumArrowRenderer extends ProjectileEntityRenderer<UraniumArrowEntity> {
    public static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(AlexsCavesEnriched.MODID, "textures/entity/uranium_arrow_entity.png");

    public UraniumArrowRenderer(EntityRendererFactory.Context manager) {
        super(manager);
    }

    @Override
    public Identifier getTexture(UraniumArrowEntity entity) {
        return TEXTURE;
    }

}