package net.hellomouse.alexscavesenriched.mixins.entity;

import com.github.alexmodguy.alexscaves.server.potion.ACEffectRegistry;
import net.hellomouse.alexscavesenriched.AlexsCavesEnriched;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.attribute.AttributeContainer;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.world.World;

@Mixin(net.minecraft.entity.mob.HostileEntity.class)
public abstract class Monster extends PathAwareEntity implements net.minecraft.entity.mob.Monster {
    @Unique
    private boolean alexscavesenriched$mutated;
    @Unique
    private HashSet<EntityAttribute> alexscavesenriched$attributesWhiteList;

    protected Monster(EntityType<? extends PathAwareEntity> p_21683_, World p_21684_) {
        super(p_21683_, p_21684_);
    }

    @Inject(method = {"<init>"},
            at = @At(value = "TAIL")
    )
    private void onConstruct(EntityType<? extends net.minecraft.entity.mob.HostileEntity> p_33002_, World p_33003_, CallbackInfo ci) {
        alexscavesenriched$attributesWhiteList = new HashSet<>(Arrays.asList(EntityAttributes.GENERIC_ATTACK_SPEED, EntityAttributes.GENERIC_ATTACK_KNOCKBACK, EntityAttributes.GENERIC_MOVEMENT_SPEED));
    }

    @Unique
    private void alexscavesenriched$addRandomizedAttributeModifier(EntityAttribute attribute) {
        var randomizedModifier = new EntityAttributeModifier("Mutation", getEntityWorld().random.nextDouble(), EntityAttributeModifier.Operation.MULTIPLY_TOTAL);
        Objects.requireNonNull(self().getAttributeInstance(attribute)).addPersistentModifier(randomizedModifier);
    }

    @Inject(
            method = {"tickMovement"},
            at = @At(value = "HEAD")
    )
    private void check_radioactive(CallbackInfo ci) {
        if (AlexsCavesEnriched.CONFIG.irradiationMutatesMobs && !alexscavesenriched$mutated &&
                ACEffectRegistry.IRRADIATED.isPresent() && self().hasStatusEffect(ACEffectRegistry.IRRADIATED.get())) {
            var attributes = self().getAttributes();
            for (var attribute : attributes.custom.keySet()) {
                if (alexscavesenriched$attributesWhiteList.contains(attribute)) {
                    alexscavesenriched$addRandomizedAttributeModifier(attribute);
                }
            }
            alexscavesenriched$mutated = true;
        }
    }
}