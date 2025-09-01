package net.hellomouse.alexscavesenriched.entity;

import com.github.alexmodguy.alexscaves.server.potion.ACEffectRegistry;
import net.hellomouse.alexscavesenriched.ACEEntityRegistry;
import net.hellomouse.alexscavesenriched.ACEParticleRegistry;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.game.ClientboundGameEventPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.PlayMessages;

public class FlamethrowerProjectileEntity extends Projectile {
    private static final EntityDataAccessor<Byte> IS_SOUL = SynchedEntityData.defineId(FlamethrowerProjectileEntity.class, EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<ItemStack> ITEM = SynchedEntityData.defineId(FlamethrowerProjectileEntity.class, EntityDataSerializers.ITEM_STACK);

    public FlamethrowerProjectileEntity(EntityType entityType, Level world) {
        super(entityType, world);
    }

    public FlamethrowerProjectileEntity(double x, double y, double z, double vx, double vy, double vz, Level world) {
        this(ACEEntityRegistry.FLAMETHROWER_PROJECTILE.get(), world);
        this.moveTo(x, y, z, this.getYRot(), this.getXRot());
        this.setDeltaMovement(vx, vy, vz);
        this.reapplyPosition();
    }

    public FlamethrowerProjectileEntity(LivingEntity owner, double directionX, double directionY, double directionZ, Level world) {
        this(owner.getX(), owner.getEyeY() - 0.4, owner.getZ(), directionX, directionY, directionZ, world);
        this.setOwner(owner);
        this.setRot(owner.getYRot(), owner.getXRot());
    }

    public FlamethrowerProjectileEntity(PlayMessages.SpawnEntity spawnEntity, Level world) {
        this(ACEEntityRegistry.FLAMETHROWER_PROJECTILE.get(), world);
        this.setBoundingBox(this.makeBoundingBox());
    }

    @Override
    protected void defineSynchedData() {
        this.getEntityData().define(IS_SOUL, (byte) 0);
        this.getEntityData().define(ITEM, ItemStack.EMPTY);
    }

    public boolean isSoul() {
        return this.entityData.get(IS_SOUL) != 0;
    }
    public void setIsSoul(boolean f) {
        this.entityData.set(IS_SOUL, (byte) (f ? 1 : 0));
    }

    @Override
    protected void onHitEntity(EntityHitResult entityHitResult) {
        super.onHitEntity(entityHitResult);
        float speed = (float) this.getDeltaMovement().length();
        float damageScale = isSoul() ? 1.5F : 1F;
        int damage = Mth.ceil(Mth.clamp((double) speed * 2.0f * damageScale, 0.0, 2.147483647E9));

        Entity entity = entityHitResult.getEntity();

        // Don't insta kill items and XP - leave chance for drop
        if ((entity instanceof ItemEntity || entity instanceof ExperienceOrb) && this.level().getRandom().nextInt(10) != 0)
            return;

        Entity owner = this.getOwner();
        DamageSource damageSource = owner instanceof LivingEntity ? this.damageSources().mobAttack((LivingEntity) owner) : this.damageSources().onFire();
        entity.setSecondsOnFire(7);
        if (isSoul() && entity instanceof LivingEntity livingEntity) {
            livingEntity.addEffect(new MobEffectInstance(ACEffectRegistry.IRRADIATED.get(),
                    20, 0, false, false, true));
        }

        if (entity.hurt(damageSource, (float) damage)) {
            if (entity instanceof LivingEntity livingEntity) {
                if (!this.level().isClientSide && owner instanceof LivingEntity) {
                    EnchantmentHelper.doPostHurtEffects(livingEntity, owner);
                    EnchantmentHelper.doPostDamageEffects((LivingEntity) owner, livingEntity);
                }

                if (livingEntity != owner && livingEntity instanceof Player && owner instanceof ServerPlayer && !this.isSilent())
                    ((ServerPlayer) owner).connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.ARROW_HIT_PLAYER, 0.0F));
            }
        } else {
            this.setDeltaMovement(this.getDeltaMovement().scale(-0.1));
            this.setYRot(this.getYRot() + 180.0F);
            this.yRotO += 180.0F;
        }
    }

    @Override
    public void tick() {
        Entity owner = this.getOwner();
        if (this.level().random.nextInt() % 400 == 0) {
            this.discard();
            return;
        }

        if ((owner == null || !owner.isRemoved()) && this.level().hasChunkAt(this.blockPosition())) {
            super.tick();

            // Fall down
            if (!this.isNoGravity()) {
                Vec3 vec3d4 = this.getDeltaMovement();
                this.setDeltaMovement(vec3d4.x, vec3d4.y - 0.05, vec3d4.z);
            }

            // Create fire particles in same direction
            Vec3 v = this.getDeltaMovement().scale(0.3);

            ParticleOptions particleEffect;
            boolean soulVersion = isSoul();

            if (soulVersion) { // Soul fire version
                particleEffect = ParticleTypes.SOUL_FIRE_FLAME;
                int choice = this.level().getRandom().nextInt(10);
                if (choice < 6)
                    particleEffect = ACEParticleRegistry.SOUL_FLAMETHROWER.get();
                else if (choice <= 9)
                    particleEffect = ParticleTypes.LARGE_SMOKE;
            } else {
                particleEffect = ParticleTypes.LAVA;
                int choice = this.level().getRandom().nextInt(10);
                if (choice < 6)
                    particleEffect = ACEParticleRegistry.FLAMETHROWER.get();
                else if (choice <= 9)
                    particleEffect = ParticleTypes.LARGE_SMOKE;
            }

            // Spawn more particle density
            for (int i = 0; i < 4; i++) {
                final float STEP = 0.4f;
                this.level().addAlwaysVisibleParticle(particleEffect,
                        this.getX() + v.x() * i * STEP,
                        this.getY() + v.y() * i * STEP,
                        this.getZ() + v.z() * i * STEP,
                        v.x(), v.y(), v.z());
            }

            HitResult hitResult = ProjectileUtil.getHitResultOnMoveVector(this, this::canHitEntity);
            if (hitResult.getType() != HitResult.Type.MISS)
                this.onHit(hitResult);

            this.checkInsideBlocks();
            Vec3 vec3d = this.getDeltaMovement();
            double x = this.getX() + vec3d.x;
            double y = this.getY() + vec3d.y;
            double z = this.getZ() + vec3d.z;

            // Make bubbles in water
            if (this.isInWater()) {
                for(int i = 0; i < 4; ++i)
                    this.level().addParticle(ParticleTypes.BUBBLE, x - vec3d.x * 0.25, y - vec3d.y * 0.25, z - vec3d.z * 0.25, vec3d.x, vec3d.y, vec3d.z);
            }

            this.level().addParticle(soulVersion ? ParticleTypes.SOUL_FIRE_FLAME : ParticleTypes.FLAME, x, y, z, 0.0, 0.0, 0.0);
            this.setPos(x, y, z);
        } else {
            this.discard();
        }
    }

    @Override
    protected boolean canHitEntity(Entity entity) {
        if (!entity.isAlive() || entity.isSpectator() || entity instanceof FlamethrowerProjectileEntity) return false;
        return entity.canBeHitByProjectile() || entity instanceof ItemEntity || entity instanceof ExperienceOrb;
    }

    @Override
    public boolean displayFireAnimation() {
        return false;
    }

    @Override
    public boolean canBeHitByProjectile() {
        return true;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (!this.isInvulnerableTo(source)) {
            this.markHurt();
            Entity owner = source.getEntity();
            if (owner != null) {
                if (!this.level().isClientSide) {
                    Vec3 vec3d = owner.getLookAngle();
                    this.setDeltaMovement(vec3d);
                    this.setOwner(owner);
                }
                return true;
            }
        }
        return false;
    }
}
