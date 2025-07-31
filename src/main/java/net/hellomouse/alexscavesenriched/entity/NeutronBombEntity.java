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

public class NeutronBombEntity extends AbstractNuclearTntEntity {
    public static final int DEFAULT_FUSE = 300;

    public NeutronBombEntity(EntityType<?> arg, World arg2) {
        super(arg, arg2);
        this.intersectionChecked = true;
        this.dropItem = ACEBlockRegistry.NEUTRON_BOMB.get();
    }

    public NeutronBombEntity(PlayMessages.SpawnEntity spawnEntity, World level) {
        this(ACEEntityRegistry.NEUTRON_BOMB.get(), level);
        this.setBoundingBox(this.calculateBoundingBox());
    }

    public NeutronBombEntity(World world, double x, double y, double z, @Nullable LivingEntity igniter) {
        this(ACEEntityRegistry.NEUTRON_BOMB.get(), world);
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
        NeutronExplosionEntity explosion = ACEEntityRegistry.NEUTRON_EXPLOSION.get().create(getWorld());
        assert explosion != null;
        explosion.copyPositionAndRotation(this);
        explosion.setSize(AlexsCavesEnriched.CONFIG.neutron.radius);
        getWorld().spawnEntity(explosion);
    }
}
