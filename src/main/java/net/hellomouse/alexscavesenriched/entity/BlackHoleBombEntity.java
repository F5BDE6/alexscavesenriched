package net.hellomouse.alexscavesenriched.entity;

import net.hellomouse.alexscavesenriched.ACEBlockRegistry;
import net.hellomouse.alexscavesenriched.ACEEntityRegistry;
import net.hellomouse.alexscavesenriched.AlexsCavesEnriched;
import net.hellomouse.alexscavesenriched.entity.abs.AbstractNuclearTntEntity;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.PlayMessages;

import javax.annotation.Nullable;

public class BlackHoleBombEntity extends AbstractNuclearTntEntity {
    public static final int DEFAULT_FUSE = 300;

    public BlackHoleBombEntity(EntityType<?> arg, Level arg2) {
        super(arg, arg2);
        this.blocksBuilding = true;
        this.dropItem = ACEBlockRegistry.BLACK_HOLE_BOMB.get();
    }

    public BlackHoleBombEntity(PlayMessages.SpawnEntity spawnEntity, Level level) {
        this(ACEEntityRegistry.BLACK_HOLE_BOMB.get(), level);
        this.setBoundingBox(this.makeBoundingBox());
    }

    public BlackHoleBombEntity(Level world, double x, double y, double z, @Nullable LivingEntity igniter) {
        this(ACEEntityRegistry.BLACK_HOLE_BOMB.get(), world);
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
    public void tick() {
        super.tick();

        if (this.level().isClientSide) {
            for (int i = 0; i < 5; i++) {
                Vec3 center = this.getEyePosition();
                Vec3 delta = new Vec3(
                        this.level().random.nextFloat() - 0.5,
                        this.level().random.nextFloat() - 0.5,
                        this.level().random.nextFloat() - 0.5
                ).normalize().scale(7.5F);
                Vec3 delta2 = delta.normalize().scale(-0.25);
                this.level().addParticle(ParticleTypes.LARGE_SMOKE, center.x + delta.x, center.y + delta.y - 1, center.z + delta.z,
                        delta2.x, delta2.y, delta2.z);
            }
        }
    }

    @Override
    protected void explode() {
        BlackHoleEntity explosion = ACEEntityRegistry.BLACK_HOLE.get().create(level());
        assert explosion != null;
        explosion.setExplosionSize(AlexsCavesEnriched.CONFIG.blackHole.radius);
        explosion.setIsExplosive(true);
        explosion.copyPosition(this);
        level().addFreshEntity(explosion);
    }
}
