package net.hellomouse.alexscavesenriched.mixins.entity;

import com.github.alexmodguy.alexscaves.server.potion.ACEffectRegistry;
import net.hellomouse.alexscavesenriched.AlexsCavesEnriched;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;

@Mixin(net.minecraft.world.entity.monster.Monster.class)
public abstract class Monster extends PathfinderMob implements net.minecraft.world.entity.monster.Enemy {
    @Unique
    private boolean alexscavesenriched$mutated;
    @Unique
    private HashSet<Attribute> alexscavesenriched$attributesWhiteList;

    protected Monster(EntityType<? extends PathfinderMob> p_21683_, Level p_21684_) {
        super(p_21683_, p_21684_);
    }

    @Inject(method = {"<init>"},
            at = @At(value = "TAIL")
    )
    private void onConstruct(EntityType<? extends net.minecraft.world.entity.monster.Monster> p_33002_, Level p_33003_, CallbackInfo ci) {
        alexscavesenriched$attributesWhiteList = new HashSet<>(Arrays.asList(Attributes.ATTACK_SPEED, Attributes.ATTACK_KNOCKBACK, Attributes.MOVEMENT_SPEED));
    }

    @Unique
    private void alexscavesenriched$addRandomizedAttributeModifier(Attribute attribute) {
        var randomizedModifier = new AttributeModifier("Mutation", getCommandSenderWorld().random.nextDouble(), AttributeModifier.Operation.MULTIPLY_TOTAL);
        Objects.requireNonNull(self().getAttribute(attribute)).addPermanentModifier(randomizedModifier);
    }

    @Inject(
            method = {"tickMovement"},
            at = @At(value = "HEAD")
    )
    private void check_radioactive(CallbackInfo ci) {
        if (AlexsCavesEnriched.CONFIG.irradiationMutatesMobs && !alexscavesenriched$mutated &&
                ACEffectRegistry.IRRADIATED.isPresent() && self().hasEffect(ACEffectRegistry.IRRADIATED.get())) {
            var attributes = self().getAttributes();
            for (var attribute : attributes.attributes.keySet()) {
                if (alexscavesenriched$attributesWhiteList.contains(attribute)) {
                    alexscavesenriched$addRandomizedAttributeModifier(attribute);
                }
            }
            alexscavesenriched$mutated = true;
        }
    }
}