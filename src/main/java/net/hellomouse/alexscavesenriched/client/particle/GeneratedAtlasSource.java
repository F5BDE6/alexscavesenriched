package net.hellomouse.alexscavesenriched.client.particle;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.hellomouse.alexscavesenriched.AlexsCavesEnriched;
import net.hellomouse.alexscavesenriched.client.ACEClientHandler;
import net.minecraft.client.resource.metadata.AnimationResourceMetadata;
import net.minecraft.client.texture.SpriteContents;
import net.minecraft.client.texture.SpriteDimensions;
import net.minecraft.client.texture.atlas.AtlasSource;
import net.minecraft.client.texture.atlas.AtlasSourceType;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

public class GeneratedAtlasSource implements AtlasSource {
    public static final Codec<GeneratedAtlasSource> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                            Identifier.CODEC.fieldOf("resource").forGetter(arg -> arg.resource)
                    )
                    .apply(instance, GeneratedAtlasSource::new)
    );
    private final Identifier resource;

    public GeneratedAtlasSource(Identifier resource) {
        this.resource = resource;
    }

    @Override
    public void load(ResourceManager resourceManager, SpriteRegions regions) {
        if (resource.equals(DemonCoreGlowTexture.ID)) {
            DemonCoreGlowTexture.init();
            regions.add(DemonCoreGlowTexture.ID, () -> new SpriteContents(DemonCoreGlowTexture.ID,
                    new SpriteDimensions(AlexsCavesEnriched.CONFIG.demonCore.sprite.getSpriteWidth(), AlexsCavesEnriched.CONFIG.demonCore.sprite.getSpriteHeight()),
                    DemonCoreGlowTexture.CURRENT,
                    AnimationResourceMetadata.EMPTY));
        } else {
            throw new IllegalStateException();
        }
    }

    @Override
    public AtlasSourceType getType() {
        return ACEClientHandler.thing;
    }
}
