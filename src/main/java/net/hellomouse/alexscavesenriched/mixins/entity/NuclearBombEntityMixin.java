package net.hellomouse.alexscavesenriched.mixins.entity;

import com.github.alexmodguy.alexscaves.AlexsCaves;
import com.github.alexmodguy.alexscaves.server.entity.item.NuclearBombEntity;
import net.hellomouse.alexscavesenriched.ACEEntityRegistry;
import net.hellomouse.alexscavesenriched.AlexsCavesEnriched;
import net.hellomouse.alexscavesenriched.entity.NuclearExplosion2Entity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(NuclearBombEntity.class)
public abstract class NuclearBombEntityMixin extends Entity {
    public NuclearBombEntityMixin(EntityType<?> type, Level world) {
        super(type, world);
    }

    @Inject(method = {"explode"}, at = @At(value = "HEAD"), cancellable = true, remap = false)
    private void explodeNew(CallbackInfo ci) {
        if (AlexsCavesEnriched.CONFIG.nuclear.useNewNuke) {
            NuclearExplosion2Entity explosion = (NuclearExplosion2Entity) ((EntityType<?>) ACEEntityRegistry.NUCLEAR_EXPLOSION2.get()).create(this.level());
            assert explosion != null;
            explosion.copyPosition(this);
            explosion.setSize(AlexsCaves.COMMON_CONFIG.nukeExplosionSizeModifier.get().floatValue());
            this.level().addFreshEntity(explosion);
            ci.cancel();
        }
    }
}
