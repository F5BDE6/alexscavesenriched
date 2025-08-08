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
import net.minecraft.block.DispenserBlock;
import net.minecraft.block.dispenser.ItemDispenserBehavior;
import net.minecraft.block.dispenser.ProjectileDispenserBehavior;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPointer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Position;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.NotNull;

public class ACEDispenserItemBehavior {
    public static void bootStrap() {
        DispenserBlock.registerBehavior(ACEItemRegistry.URANIUM_ARROW.get(), new ProjectileDispenserBehavior() {
            protected @NotNull ProjectileEntity createProjectile(@NotNull World level, @NotNull Position pos, @NotNull ItemStack itemStack) {
                return new UraniumArrowEntity(ACEEntityRegistry.URANIUM_ARROW.get(), pos.getX(), pos.getY(), pos.getZ(), level);
            }
        });
        DispenserBlock.registerBehavior(ACEItemRegistry.ROCKET_NORMAL.get(), new ProjectileDispenserBehavior() {
            protected @NotNull ProjectileEntity createProjectile(@NotNull World level, @NotNull Position pos, @NotNull ItemStack itemStack) {
                return new RocketEntity(level, pos.getX(), pos.getY(), pos.getZ());
            }

            protected float getForce() {
                return 5.1F;
            }
        });
        DispenserBlock.registerBehavior(ACEItemRegistry.ROCKET.get(), new ProjectileDispenserBehavior() {
            protected @NotNull ProjectileEntity createProjectile(@NotNull World level, @NotNull Position pos, @NotNull ItemStack itemStack) {
                var e = new RocketEntity(level, pos.getX(), pos.getY(), pos.getZ());
                e.setIsRadioactive(true);
                return e;
            }

            protected float getForce() {
                return (float) AlexsCavesEnriched.CONFIG.rocket.nonNuclear.dispenserPower;
            }
            protected float getVariation() {
                return (float) AlexsCavesEnriched.CONFIG.rocket.nonNuclear.dispenserUncertainty;
            }
        });
        DispenserBlock.registerBehavior(ACEItemRegistry.ROCKET_NUCLEAR.get(), new ProjectileDispenserBehavior() {
            protected @NotNull ProjectileEntity createProjectile(@NotNull World level, @NotNull Position pos, @NotNull ItemStack itemStack) {
                var e = new RocketEntity(level, pos.getX(), pos.getY(), pos.getZ());
                e.setIsNuclear(true);
                return e;
            }

            protected float getForce() {
                return (float) AlexsCavesEnriched.CONFIG.rocket.nuclear.dispenserPower;
            }
            protected float getVariation() {
                return 0.0F;
            }
        });
        DispenserBlock.registerBehavior(ACEItemRegistry.ROCKET_NEUTRON.get(), new ProjectileDispenserBehavior() {
            protected @NotNull ProjectileEntity createProjectile(@NotNull World level, @NotNull Position pos, @NotNull ItemStack itemStack) {
                var e = new RocketEntity(level, pos.getX(), pos.getY(), pos.getZ());
                e.setIsNeutron(true);
                return e;
            }

            protected float getForce() {
                return (float) AlexsCavesEnriched.CONFIG.rocket.nuclear.dispenserPower;
            }
            protected float getVariation() {
                return 0.0F;
            }
        });
        DispenserBlock.registerBehavior(ACEItemRegistry.ROCKET_MINI_NUKE.get(), new ProjectileDispenserBehavior() {
            protected @NotNull ProjectileEntity createProjectile(@NotNull World level, @NotNull Position pos, @NotNull ItemStack itemStack) {
                var e = new RocketEntity(level, pos.getX(), pos.getY(), pos.getZ());
                e.setIsMiniNuke(true);
                return e;
            }

            protected float getForce() {
                return (float) AlexsCavesEnriched.CONFIG.rocket.nuclear.dispenserPower;
            }
            protected float getVariation() {
                return 0.0F;
            }
        });

        DispenserBlock.registerBehavior(ACEBlockRegistry.MINI_NUKE.get(), new ItemDispenserBehavior() {
            @Override
            protected ItemStack dispenseSilently(BlockPointer pointer, ItemStack stack) {
                World level = pointer.getWorld();
                BlockPos blockpos = pointer.getPos().offset(pointer.getBlockState().get(DispenserBlock.FACING));
                MiniNukeBlock.detonateStatic(level, blockpos, null);
                level.emitGameEvent(null, GameEvent.ENTITY_PLACE, blockpos);
                stack.decrement(1);
                return stack;
            }
        });
        DispenserBlock.registerBehavior(ACEBlockRegistry.NEUTRON_BOMB.get(), new ItemDispenserBehavior() {
            @Override
            protected ItemStack dispenseSilently(BlockPointer pointer, ItemStack stack) {
                World level = pointer.getWorld();
                BlockPos blockpos = pointer.getPos().offset(pointer.getBlockState().get(DispenserBlock.FACING));
                NeutronBombBlock.detonateStatic(level, blockpos, null);
                level.emitGameEvent(null, GameEvent.ENTITY_PLACE, blockpos);
                stack.decrement(1);
                return stack;
            }
        });
        DispenserBlock.registerBehavior(ACEBlockRegistry.BLACK_HOLE_BOMB.get(), new ItemDispenserBehavior() {
            @Override
            protected ItemStack dispenseSilently(BlockPointer pointer, ItemStack stack) {
                World level = pointer.getWorld();
                BlockPos blockpos = pointer.getPos().offset(pointer.getBlockState().get(DispenserBlock.FACING));
                BlackHoleBombBlock.detonateStatic(level, blockpos, null);
                level.emitGameEvent(null, GameEvent.ENTITY_PLACE, blockpos);
                stack.decrement(1);
                return stack;
            }
        });
    }
}
