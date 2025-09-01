package net.hellomouse.alexscavesenriched.mixins.entity;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.RangedBowAttackGoal;
import net.minecraft.world.entity.monster.RangedAttackMob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(RangedBowAttackGoal.class)
public interface RangedBowAttackGoalAccessor<T extends Mob & RangedAttackMob> {
    @Accessor("mob")
    T getMob();

}
