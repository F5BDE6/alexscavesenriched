package net.hellomouse.alexscavesenriched.mixins.entity;

import com.github.alexmodguy.alexscaves.server.entity.item.NuclearExplosionEntity;
import com.github.alexmodguy.alexscaves.server.misc.ACDamageTypes;
import net.hellomouse.alexscavesenriched.AlexsCavesEnriched;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Iterator;

@Mixin(NuclearExplosionEntity.class)
public abstract class NuclearExplosionEntityMixin extends Entity {
    public NuclearExplosionEntityMixin(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(
            method = {"tick"},
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/LivingEntity;hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z",
                    shift = At.Shift.AFTER
            ),
            locals = LocalCapture.CAPTURE_FAILSOFT
    )
    private void nukeDamagesWither(CallbackInfo ci, int chunksAffected, int radius, AABB killBox, float flingStrength, float maximumDistance, Iterator var6, LivingEntity entity, float dist, float damage, Vec3 vec3, float playerFling) {
        if (AlexsCavesEnriched.CONFIG.nuclear.letNukeKillWither && entity instanceof WitherBoss)
            entity.hurt(ACDamageTypes.causeIntentionalGameDesign(getCommandSenderWorld().registryAccess()), damage);
    }
}
