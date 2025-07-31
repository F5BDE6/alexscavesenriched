package net.hellomouse.alexscavesenriched.entity;

import net.hellomouse.alexscavesenriched.ACEBlockRegistry;
import net.hellomouse.alexscavesenriched.ACEEntityRegistry;
import net.hellomouse.alexscavesenriched.AlexsCavesEnriched;
import net.hellomouse.alexscavesenriched.entity.abs.AbstractNuclearTntEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.world.World;
import net.minecraftforge.network.PlayMessages;

import javax.annotation.Nullable;

public class MiniNukeEntity extends AbstractNuclearTntEntity {
    public static final int DEFAULT_FUSE = 200;

    public MiniNukeEntity(EntityType<?> arg, World arg2) {
        super(arg, arg2);
        this.intersectionChecked = true;
        this.dropItem = ACEBlockRegistry.MINI_NUKE.get();
    }

    public MiniNukeEntity(PlayMessages.SpawnEntity spawnEntity, World level) {
        this(ACEEntityRegistry.MINI_NUKE.get(), level);
        this.setBoundingBox(this.calculateBoundingBox());
    }

    public MiniNukeEntity(World world, double x, double y, double z, @Nullable LivingEntity igniter) {
        this(ACEEntityRegistry.MINI_NUKE.get(), world);
        this.setPosition(x, y, z);
        double d = world.random.nextDouble() * (float) (Math.PI * 2);
        this.setVelocity(-Math.sin(d) * 0.02, 0.2F, -Math.cos(d) * 0.02);
        this.setFuse(DEFAULT_FUSE);
        this.prevX = x;
        this.prevY = y;
        this.prevZ = z;
        this.causingEntity = igniter;
    }

    @Override
    protected void explode() {
        NuclearExplosion2Entity explosion = ACEEntityRegistry.NUCLEAR_EXPLOSION2.get().create(getWorld());
        assert explosion != null;
        explosion.copyPositionAndRotation(this);
        explosion.setSize(AlexsCavesEnriched.CONFIG.miniNukeRadius);
        getWorld().spawnEntity(explosion);
    }
}
