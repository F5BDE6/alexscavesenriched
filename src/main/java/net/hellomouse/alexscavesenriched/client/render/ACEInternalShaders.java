package net.hellomouse.alexscavesenriched.client.render;

import javax.annotation.Nullable;

import net.minecraft.client.renderer.ShaderInstance;

public class ACEInternalShaders {
    private static ShaderInstance RadiationParticleShader;

    @Nullable
    public static ShaderInstance getRadiationParticleShader() {
        return RadiationParticleShader;
    }

    public static void setRadiationParticleShader(ShaderInstance shader) {
        RadiationParticleShader = shader;
    }
}
