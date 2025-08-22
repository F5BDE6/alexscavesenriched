package net.hellomouse.alexscavesenriched.item;

import net.hellomouse.alexscavesenriched.ACEBlockRegistry;
import net.hellomouse.alexscavesenriched.ACEEntityRegistry;
import net.hellomouse.alexscavesenriched.ACEItemRegistry;
import net.hellomouse.alexscavesenriched.AlexsCavesEnriched;
import net.hellomouse.alexscavesenriched.block.BlackHoleBombBlock;
import net.hellomouse.alexscavesenriched.block.MiniNukeBlock;
import net.hellomouse.alexscavesenriched.block.NeutronBombBlock;
import net.hellomouse.alexscavesenriched.entity.RocketEntity;
import net.hellomouse.alexscavesenriched.entity.UraniumArrowEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockSource;
import net.minecraft.core.Position;
import net.minecraft.core.dispenser.AbstractProjectileDispenseBehavior;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.gameevent.GameEvent;
import org.jetbrains.annotations.NotNull;

public class ACEDispenserItemBehavior {
    public static void bootStrap() {
        DispenserBlock.registerBehavior(ACEItemRegistry.URANIUM_ARROW.get(), new AbstractProjectileDispenseBehavior() {
            protected @NotNull Projectile getProjectile(@NotNull Level level, @NotNull Position pos, @NotNull ItemStack itemStack) {
                return new UraniumArrowEntity(ACEEntityRegistry.URANIUM_ARROW.get(), pos.x(), pos.y(), pos.z(), level);
            }
        });
        DispenserBlock.registerBehavior(ACEItemRegistry.ROCKET_NORMAL.get(), new AbstractProjectileDispenseBehavior() {
            protected @NotNull Projectile getProjectile(@NotNull Level level, @NotNull Position pos, @NotNull ItemStack itemStack) {
                return new RocketEntity(level, pos.x(), pos.y(), pos.z());
            }

            protected float getPower() {
                return 5.1F;
            }
        });
        DispenserBlock.registerBehavior(ACEItemRegistry.ROCKET.get(), new AbstractProjectileDispenseBehavior() {
            protected @NotNull Projectile getProjectile(@NotNull Level level, @NotNull Position pos, @NotNull ItemStack itemStack) {
                var e = new RocketEntity(level, pos.x(), pos.y(), pos.z());
                e.setIsRadioactive(true);
                return e;
            }

            protected float getPower() {
                return (float) AlexsCavesEnriched.CONFIG.rocket.nonNuclear.dispenserPower;
            }

            protected float getUncertainty() {
                return (float) AlexsCavesEnriched.CONFIG.rocket.nonNuclear.dispenserUncertainty;
            }
        });
        DispenserBlock.registerBehavior(ACEItemRegistry.ROCKET_NUCLEAR.get(), new AbstractProjectileDispenseBehavior() {
            protected @NotNull Projectile getProjectile(@NotNull Level level, @NotNull Position pos, @NotNull ItemStack itemStack) {
                var e = new RocketEntity(level, pos.x(), pos.y(), pos.z());
                e.setIsNuclear(true);
                return e;
            }

            protected float getPower() {
                return (float) AlexsCavesEnriched.CONFIG.rocket.nuclear.dispenserPower;
            }

            protected float getUncertainty() {
                return 0.0F;
            }
        });
        DispenserBlock.registerBehavior(ACEItemRegistry.ROCKET_NEUTRON.get(), new AbstractProjectileDispenseBehavior() {
            protected @NotNull Projectile getProjectile(@NotNull Level level, @NotNull Position pos, @NotNull ItemStack itemStack) {
                var e = new RocketEntity(level, pos.x(), pos.y(), pos.z());
                e.setIsNeutron(true);
                return e;
            }

            protected float getPower() {
                return (float) AlexsCavesEnriched.CONFIG.rocket.nuclear.dispenserPower;
            }

            protected float getUncertainty() {
                return 0.0F;
            }
        });
        DispenserBlock.registerBehavior(ACEItemRegistry.ROCKET_MINI_NUKE.get(), new AbstractProjectileDispenseBehavior() {
            protected @NotNull Projectile getProjectile(@NotNull Level level, @NotNull Position pos, @NotNull ItemStack itemStack) {
                var e = new RocketEntity(level, pos.x(), pos.y(), pos.z());
                e.setIsMiniNuke(true);
                return e;
            }

            protected float getPower() {
                return (float) AlexsCavesEnriched.CONFIG.rocket.nuclear.dispenserPower;
            }

            protected float getUncertainty() {
                return 0.0F;
            }
        });

        DispenserBlock.registerBehavior(ACEBlockRegistry.MINI_NUKE.get(), new DefaultDispenseItemBehavior() {
            @Override
            protected ItemStack execute(BlockSource pointer, ItemStack stack) {
                Level level = pointer.getLevel();
                BlockPos blockpos = pointer.getPos().relative(pointer.getBlockState().getValue(DispenserBlock.FACING));
                MiniNukeBlock.detonateStatic(level, blockpos, null);
                level.gameEvent(null, GameEvent.ENTITY_PLACE, blockpos);
                stack.shrink(1);
                return stack;
            }
        });
        DispenserBlock.registerBehavior(ACEBlockRegistry.NEUTRON_BOMB.get(), new DefaultDispenseItemBehavior() {
            @Override
            protected ItemStack execute(BlockSource pointer, ItemStack stack) {
                Level level = pointer.getLevel();
                BlockPos blockpos = pointer.getPos().relative(pointer.getBlockState().getValue(DispenserBlock.FACING));
                NeutronBombBlock.detonateStatic(level, blockpos, null);
                level.gameEvent(null, GameEvent.ENTITY_PLACE, blockpos);
                stack.shrink(1);
                return stack;
            }
        });
        DispenserBlock.registerBehavior(ACEBlockRegistry.BLACK_HOLE_BOMB.get(), new DefaultDispenseItemBehavior() {
            @Override
            protected ItemStack execute(BlockSource pointer, ItemStack stack) {
                Level level = pointer.getLevel();
                BlockPos blockpos = pointer.getPos().relative(pointer.getBlockState().getValue(DispenserBlock.FACING));
                BlackHoleBombBlock.detonateStatic(level, blockpos, null);
                level.gameEvent(null, GameEvent.ENTITY_PLACE, blockpos);
                stack.shrink(1);
                return stack;
            }
        });
    }
}
