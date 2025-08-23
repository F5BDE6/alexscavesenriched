package net.hellomouse.alexscavesenriched.entity;

import net.hellomouse.alexscavesenriched.ACEBlockRegistry;
import net.hellomouse.alexscavesenriched.ACEEntityRegistry;
import net.hellomouse.alexscavesenriched.AlexsCavesEnriched;
import net.hellomouse.alexscavesenriched.entity.abs.AbstractNuclearTntEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.PlayMessages;

import javax.annotation.Nullable;

public class MiniNukeEntity extends AbstractNuclearTntEntity {
    public static final int DEFAULT_FUSE = 200;

    public MiniNukeEntity(EntityType<?> arg, Level arg2) {
        super(arg, arg2);
        this.blocksBuilding = true;
        this.dropItem = ACEBlockRegistry.MINI_NUKE.get();
    }

    public MiniNukeEntity(PlayMessages.SpawnEntity spawnEntity, Level level) {
        this(ACEEntityRegistry.MINI_NUKE.get(), level);
        this.setBoundingBox(this.makeBoundingBox());
    }

    public MiniNukeEntity(Level world, double x, double y, double z, @Nullable LivingEntity igniter) {
        this(ACEEntityRegistry.MINI_NUKE.get(), world);
        this.setPos(x, y, z);
        double d = world.random.nextDouble() * (float) (Math.PI * 2);
        this.setDeltaMovement(-Math.sin(d) * 0.02, 0.2F, -Math.cos(d) * 0.02);
        this.setFuse(DEFAULT_FUSE);
        this.xo = x;
        this.yo = y;
        this.zo = z;
        this.causingEntity = igniter;
    }

    @Override
    protected void explode() {
        NuclearExplosion2Entity explosion = ACEEntityRegistry.NUCLEAR_EXPLOSION2.get().create(level());
        assert explosion != null;
        explosion.copyPosition(this);
        explosion.setSize(AlexsCavesEnriched.CONFIG.miniNukeRadius);
        level().addFreshEntity(explosion);
    }
}
