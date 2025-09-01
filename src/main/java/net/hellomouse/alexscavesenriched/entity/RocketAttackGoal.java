package net.hellomouse.alexscavesenriched.entity;

import net.hellomouse.alexscavesenriched.item.RocketItem;
import net.hellomouse.alexscavesenriched.mixins.entity.RangedBowAttackGoalAccessor;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.RangedBowAttackGoal;
import net.minecraft.world.entity.monster.RangedAttackMob;

public class RocketAttackGoal<T extends Mob & RangedAttackMob> extends RangedBowAttackGoal<T> {
    public RocketAttackGoal(T mob, double speedModifier, int attackIntervalMin, float attackRadius) {
        super(mob, speedModifier, attackIntervalMin, attackRadius);
    }

    @Override
    public boolean canUse() {
        return ((RangedBowAttackGoalAccessor<T>) this).getMob().getTarget() != null && this.isHoldingRocket();
    }

    protected boolean isHoldingRocket() {
        return ((RangedBowAttackGoalAccessor<T>) this).getMob().isHolding(is -> is.getItem() instanceof RocketItem);
    }

    @Override
    public boolean canContinueToUse() {
        return (this.canUse() || !((RangedBowAttackGoalAccessor<T>) this).getMob().getNavigation().isDone()) && this.isHoldingRocket();
    }

}
