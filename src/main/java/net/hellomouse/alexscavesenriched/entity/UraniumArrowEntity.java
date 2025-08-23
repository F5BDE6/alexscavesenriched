package net.hellomouse.alexscavesenriched.entity;

import com.github.alexmodguy.alexscaves.client.particle.ACParticleRegistry;
import com.github.alexmodguy.alexscaves.server.potion.ACEffectRegistry;
import net.hellomouse.alexscavesenriched.ACEItemRegistry;
import net.hellomouse.alexscavesenriched.AlexsCavesEnriched;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;

public class UraniumArrowEntity extends AbstractArrow {
    public UraniumArrowEntity(EntityType<UraniumArrowEntity> entityType, Level world) {
        super(entityType, world);
        this.setBaseDamage(AlexsCavesEnriched.CONFIG.uraniumArrow.baseDamage);
    }

    public UraniumArrowEntity(EntityType<UraniumArrowEntity> entityType, double x, double y, double z, Level world) {
        super(entityType, x, y, z, world);
        this.setBaseDamage(AlexsCavesEnriched.CONFIG.uraniumArrow.baseDamage);
    }

    public UraniumArrowEntity(EntityType<UraniumArrowEntity> entityType, LivingEntity shooter, Level world) {
        super(entityType, shooter, world);
        this.setBaseDamage(AlexsCavesEnriched.CONFIG.uraniumArrow.baseDamage);
    }

    @Override
    public @NotNull Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public void tick() {
        this.setPierceLevel((byte)100);
        super.tick();

        // Increased gravity for uranium arrows
        if (!this.isNoGravity() && !this.isNoPhysics()) {
            Vec3 vel = this.getDeltaMovement();
            this.setDeltaMovement(vel.x, vel.y - (double) 0.05F, vel.z);
        }

        if (this.getCommandSenderWorld().isClientSide && !this.inGround) {
            this.getCommandSenderWorld().addParticle(ACParticleRegistry.FALLOUT.get(), this.getX(), this.getY(), this.getZ(), 0.0D, 0.0D, 0.0D);
        }
    }

    @Override
    protected void doPostHurtEffects(LivingEntity target) {
        super.doPostHurtEffects(target);
        MobEffectInstance mobeffectinstance = new MobEffectInstance(ACEffectRegistry.IRRADIATED.get(),
                AlexsCavesEnriched.CONFIG.uraniumArrow.irradiationTime, 0);
        target.addEffect(mobeffectinstance, this.getEffectSource());
    }

    @Override
    protected void onHitEntity(@NotNull EntityHitResult ray) {
        this.setPierceLevel((byte)100);
        super.onHitEntity(ray);
    }

    @Override
    protected @NotNull ItemStack getPickupItem() {
        return new ItemStack(ACEItemRegistry.URANIUM_ARROW.get());
    }
}