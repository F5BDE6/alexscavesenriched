package net.hellomouse.alexscavesenriched.entity;

import com.github.alexmodguy.alexscaves.client.particle.ACParticleRegistry;
import com.github.alexmodguy.alexscaves.server.potion.ACEffectRegistry;
import net.hellomouse.alexscavesenriched.ACEItemRegistry;
import net.hellomouse.alexscavesenriched.AlexsCavesEnriched;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;

public class UraniumArrowEntity extends PersistentProjectileEntity {
    public UraniumArrowEntity(EntityType<UraniumArrowEntity> entityType, World world) {
        super(entityType, world);
        this.setDamage(AlexsCavesEnriched.CONFIG.uraniumArrow.baseDamage);
    }

    public UraniumArrowEntity(EntityType<UraniumArrowEntity> entityType, double x, double y, double z, World world) {
        super(entityType, x, y, z, world);
        this.setDamage(AlexsCavesEnriched.CONFIG.uraniumArrow.baseDamage);
    }

    public UraniumArrowEntity(EntityType<UraniumArrowEntity> entityType, LivingEntity shooter, World world) {
        super(entityType, shooter, world);
        this.setDamage(AlexsCavesEnriched.CONFIG.uraniumArrow.baseDamage);
    }

    @Override
    public @NotNull Packet<ClientPlayPacketListener> createSpawnPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public void tick() {
        this.setPierceLevel((byte)100);
        super.tick();

        // Increased gravity for uranium arrows
        if (!this.hasNoGravity() && !this.isNoClip()) {
            Vec3d vel = this.getVelocity();
            this.setVelocity(vel.x, vel.y - (double)0.05F, vel.z);
        }

        if (this.getEntityWorld().isClient && !this.inGround) {
            this.getEntityWorld().addParticle(ACParticleRegistry.FALLOUT.get(), this.getX(), this.getY(), this.getZ(), 0.0D, 0.0D, 0.0D);
        }
    }

    @Override
    protected void onHit(LivingEntity target) {
        super.onHit(target);
        StatusEffectInstance mobeffectinstance = new StatusEffectInstance(ACEffectRegistry.IRRADIATED.get(),
                AlexsCavesEnriched.CONFIG.uraniumArrow.irradiationTime, 0);
        target.addStatusEffect(mobeffectinstance, this.getEffectCause());
    }

    @Override
    protected void onEntityHit(@NotNull EntityHitResult ray) {
        this.setPierceLevel((byte)100);
        super.onEntityHit(ray);
    }

    @Override
    protected @NotNull ItemStack asItemStack() {
        return new ItemStack(ACEItemRegistry.URANIUM_ARROW.get());
    }
}