package net.hellomouse.alexscavesenriched.item;

import net.hellomouse.alexscavesenriched.ACEEntityRegistry;
import net.hellomouse.alexscavesenriched.ACEItemRegistry;
import net.hellomouse.alexscavesenriched.AlexsCavesEnriched;
import net.hellomouse.alexscavesenriched.entity.RocketEntity;
import net.hellomouse.alexscavesenriched.entity.UraniumArrowEntity;
import net.minecraft.block.DispenserBlock;
import net.minecraft.block.dispenser.ProjectileDispenserBehavior;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Position;
import net.minecraft.world.World;
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
    }
}
