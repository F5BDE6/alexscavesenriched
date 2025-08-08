package net.hellomouse.alexscavesenriched.mixins.entity;

import net.hellomouse.alexscavesenriched.ACEItemRegistry;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.RangedAttackMob;
import net.minecraft.entity.ai.goal.BowAttackGoal;
import net.minecraft.entity.mob.AbstractSkeletonEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(AbstractSkeletonEntity.class)
public abstract class AbstractSkeletonMixin extends HostileEntity implements RangedAttackMob {
    @Final
    private final BowAttackGoal<AbstractSkeletonEntity> rocketLauncherGoal = new BowAttackGoal<>(this, 1.0D, 20, 64.0F);

    protected AbstractSkeletonMixin(EntityType<? extends HostileEntity> p_33002_, World p_33003_) {
        super(p_33002_, p_33003_);
    }

    @Inject(
            method = {"updateAttackType"},
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/ai/goal/GoalSelector;add(ILnet/minecraft/entity/ai/goal/Goal;)V",
                    shift = At.Shift.AFTER
            ),
            locals = LocalCapture.CAPTURE_FAILHARD,
            cancellable = true
    )
    private void nukeDamagesWither(CallbackInfo ci, ItemStack itemstack) {
        if (itemstack.isOf(ACEItemRegistry.ROCKET_LAUNCHER.get())) {
            rocketLauncherGoal.setAttackInterval(40);
            goalSelector.add(4, rocketLauncherGoal);
            ci.cancel();
        }
    }
}
