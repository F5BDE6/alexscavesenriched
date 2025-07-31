package net.hellomouse.alexscavesenriched.entity;

import net.hellomouse.alexscavesenriched.ACEBlockRegistry;
import net.hellomouse.alexscavesenriched.ACEEntityRegistry;
import net.hellomouse.alexscavesenriched.AlexsCavesEnriched;
import net.hellomouse.alexscavesenriched.entity.abs.AbstractNuclearTntEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.network.PlayMessages;

import javax.annotation.Nullable;

public class BlackHoleBombEntity extends AbstractNuclearTntEntity {
    public static final int DEFAULT_FUSE = 300;

    public BlackHoleBombEntity(EntityType<?> arg, World arg2) {
        super(arg, arg2);
        this.intersectionChecked = true;
        this.dropItem = ACEBlockRegistry.BLACK_HOLE_BOMB.get();
    }

    public BlackHoleBombEntity(PlayMessages.SpawnEntity spawnEntity, World level) {
        this(ACEEntityRegistry.BLACK_HOLE_BOMB.get(), level);
        this.setBoundingBox(this.calculateBoundingBox());
    }

    public BlackHoleBombEntity(World world, double x, double y, double z, @Nullable LivingEntity igniter) {
        this(ACEEntityRegistry.BLACK_HOLE_BOMB.get(), world);
        this.setPosition(x, y, z);
        double d = world.random.nextDouble() * (float) (Math.PI * 2);
        this.setVelocity(-Math.sin(d) * 0.02, 0.2F, -Math.cos(d) * 0.02);
        this.setFuse(DEFAULT_FUSE);
        this.prevX = x;
        this.prevY = y;
        this.prevZ = z;
        this.causingEntity = igniter;
        this.setGlowing(true);
    }

    @Override
    public void tick() {
        super.tick();

        if (this.getWorld().isClient) {
            for (int i = 0; i < 5; i++) {
                Vec3d center = this.getEyePos();
                Vec3d delta = new Vec3d(
                        this.getWorld().random.nextFloat() - 0.5,
                        this.getWorld().random.nextFloat() - 0.5,
                        this.getWorld().random.nextFloat() - 0.5
                ).normalize().multiply(7.5F);
                Vec3d delta2 = delta.normalize().multiply(-0.25);
                this.getWorld().addParticle(ParticleTypes.LARGE_SMOKE, center.x + delta.x, center.y + delta.y - 1, center.z + delta.z,
                        delta2.x, delta2.y, delta2.z);
            }
        }
    }

    @Override
    protected void explode() {
        BlackHoleEntity explosion = ACEEntityRegistry.BLACK_HOLE.get().create(getWorld());
        assert explosion != null;
        explosion.setExplosionSize(AlexsCavesEnriched.CONFIG.blackHole.radius);
        explosion.setIsExplosive(true);
        explosion.copyPositionAndRotation(this);
        getWorld().spawnEntity(explosion);
    }
}
