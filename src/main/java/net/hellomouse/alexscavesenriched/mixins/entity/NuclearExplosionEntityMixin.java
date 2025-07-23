package net.hellomouse.alexscavesenriched.mixins.entity;

import com.github.alexmodguy.alexscaves.server.entity.item.NuclearExplosionEntity;
import com.github.alexmodguy.alexscaves.server.misc.ACDamageTypes;
import net.hellomouse.alexscavesenriched.AlexsCavesEnriched;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Iterator;

@Mixin(NuclearExplosionEntity.class)
public abstract class NuclearExplosionEntityMixin extends Entity {
    public NuclearExplosionEntityMixin(EntityType<?> entityType, World level) {
        super(entityType, level);
    }

    @Inject(
            method = {"tick"},
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/LivingEntity;damage(Lnet/minecraft/entity/damage/DamageSource;F)Z",
                    shift = At.Shift.AFTER
            ),
            locals = LocalCapture.CAPTURE_FAILSOFT
    )
    private void nukeDamagesWither(CallbackInfo ci, int chunksAffected, int radius, Box killBox, float flingStrength, float maximumDistance, Iterator var6, LivingEntity entity, float dist, float damage, Vec3d vec3, float playerFling) {
        if (AlexsCavesEnriched.CONFIG.nuclear.letNukeKillWither && entity instanceof WitherEntity)
            entity.damage(ACDamageTypes.causeIntentionalGameDesign(getEntityWorld().getRegistryManager()), damage);
    }
}
