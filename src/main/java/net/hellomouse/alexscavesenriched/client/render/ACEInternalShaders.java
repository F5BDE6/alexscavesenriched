package net.hellomouse.alexscavesenriched.client.render;

import javax.annotation.Nullable;
import net.minecraft.client.gl.ShaderProgram;

public class ACEInternalShaders {
    private static ShaderProgram RadiationParticleShader;

    @Nullable
    public static ShaderProgram getRadiationParticleShader(){
        return RadiationParticleShader;
    }
    public static void setRadiationParticleShader(ShaderProgram shader){
        RadiationParticleShader = shader;
    }
}
