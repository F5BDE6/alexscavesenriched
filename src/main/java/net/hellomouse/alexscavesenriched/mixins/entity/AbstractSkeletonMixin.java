package net.hellomouse.alexscavesenriched.mixins.entity;

import net.hellomouse.alexscavesenriched.ACEItemRegistry;
import net.hellomouse.alexscavesenriched.entity.RocketAttackGoal;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(AbstractSkeleton.class)
public abstract class AbstractSkeletonMixin extends Monster implements RangedAttackMob {
    @Final
    private final RocketAttackGoal<AbstractSkeleton> rocketLauncherGoal = new RocketAttackGoal(this, 1.0D, 20, 64.0F);

    protected AbstractSkeletonMixin(EntityType<? extends Monster> p_33002_, Level p_33003_) {
        super(p_33002_, p_33003_);
    }

    @Inject(
            method = {"reassessWeaponGoal"},
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/ai/goal/GoalSelector;addGoal(ILnet/minecraft/world/entity/ai/goal/Goal;)V",
                    shift = At.Shift.AFTER
            ),
            locals = LocalCapture.CAPTURE_FAILHARD,
            cancellable = true
    )
    private void nukeDamagesWither(CallbackInfo ci, ItemStack itemstack) {
        if (itemstack.is(ACEItemRegistry.ROCKET_LAUNCHER.get())) {
            rocketLauncherGoal.setMinAttackInterval(40);
            goalSelector.addGoal(4, rocketLauncherGoal);
            ci.cancel();
        }
    }
}
