package net.hellomouse.alexscavesenriched.entity;

import net.hellomouse.alexscavesenriched.ACEEntityRegistry;
import net.hellomouse.alexscavesenriched.ACEParticleRegistry;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.*;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.GameStateChangeS2CPacket;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.network.PlayMessages;

public class FlamethrowerProjectileEntity extends ProjectileEntity {
    private static final TrackedData<ItemStack> ITEM = DataTracker.registerData(FlamethrowerProjectileEntity.class, TrackedDataHandlerRegistry.ITEM_STACK);;

    public FlamethrowerProjectileEntity(EntityType entityType, World world) {
        super(entityType, world);
    }

    public FlamethrowerProjectileEntity(double x, double y, double z, double vx, double vy, double vz, World world) {
        this(ACEEntityRegistry.FLAMETHROWER_PROJECTILE.get(), world);
        this.refreshPositionAndAngles(x, y, z, this.getYaw(), this.getPitch());
        this.setVelocity(vx, vy, vz);
        this.refreshPosition();
    }

    public FlamethrowerProjectileEntity(LivingEntity owner, double directionX, double directionY, double directionZ, World world) {
        this(owner.getX(), owner.getEyeY() - 0.4, owner.getZ(), directionX, directionY, directionZ, world);
        this.setOwner(owner);
        this.setRotation(owner.getYaw(), owner.getPitch());
    }

    public FlamethrowerProjectileEntity(PlayMessages.SpawnEntity spawnEntity, World world) {
        this(ACEEntityRegistry.FLAMETHROWER_PROJECTILE.get(), world);
        this.setBoundingBox(this.calculateBoundingBox());
    }

    @Override
    protected void initDataTracker() {
        this.getDataTracker().startTracking(ITEM, ItemStack.EMPTY);
    }

    @Override
    protected void onEntityHit(EntityHitResult entityHitResult) {
        super.onEntityHit(entityHitResult);
        float f = (float)this.getVelocity().length();
        int i = MathHelper.ceil(MathHelper.clamp((double)f * 2.0f, 0.0, 2.147483647E9));

        Entity entity = entityHitResult.getEntity();

        // Don't insta kill items and XP - leave chance for drop
        if ((entity instanceof ItemEntity || entity instanceof ExperienceOrbEntity) && this.getWorld().getRandom().nextInt(10) != 0)
            return;

        Entity owner = this.getOwner();
        DamageSource damageSource = owner instanceof LivingEntity ? this.getDamageSources().mobAttack((LivingEntity)owner) : this.getDamageSources().onFire();
        entity.setOnFireFor(7);

        if (entity.damage(damageSource, (float)i)) {
            if (entity instanceof LivingEntity livingEntity) {
                if (!this.getWorld().isClient && owner instanceof LivingEntity) {
                    EnchantmentHelper.onUserDamaged(livingEntity, owner);
                    EnchantmentHelper.onTargetDamaged((LivingEntity)owner, livingEntity);
                }

                if (livingEntity != owner && livingEntity instanceof PlayerEntity && owner instanceof ServerPlayerEntity && !this.isSilent())
                    ((ServerPlayerEntity)owner).networkHandler.sendPacket(new GameStateChangeS2CPacket(GameStateChangeS2CPacket.PROJECTILE_HIT_PLAYER, 0.0F));
            }
        } else {
            this.setVelocity(this.getVelocity().multiply(-0.1));
            this.setYaw(this.getYaw() + 180.0F);
            this.prevYaw += 180.0F;
        }
    }

    @Override
    public void tick() {
        Entity owner = this.getOwner();
        if (this.getWorld().random.nextInt() % 400 == 0) {
            this.discard();
            return;
        }

        if ((owner == null || !owner.isRemoved()) && this.getWorld().isChunkLoaded(this.getBlockPos())) {
            super.tick();

            // Fall down
            if (!this.hasNoGravity()) {
                Vec3d vec3d4 = this.getVelocity();
                this.setVelocity(vec3d4.x, vec3d4.y - 0.05, vec3d4.z);
            }

            // Create fire particles in same direction
            Vec3d v = this.getVelocity().multiply(0.3);
            ParticleEffect particleEffect = ParticleTypes.LAVA;
            int choice = this.getWorld().getRandom().nextInt(10);
            if (choice < 6)
                particleEffect = ACEParticleRegistry.FLAMETHROWER.get();
            else if (choice <= 9)
                particleEffect = ParticleTypes.LARGE_SMOKE;

            // Spawn more particle density
            for (int i = 0; i < 4; i++) {
                final float STEP = 0.4f;
                this.getWorld().addImportantParticle(particleEffect,
                        this.getX() + v.getX() * i * STEP,
                        this.getY() + v.getY() * i * STEP,
                        this.getZ() + v.getZ() * i * STEP,
                        v.getX(), v.getY(), v.getZ());
            }

            HitResult hitResult = ProjectileUtil.getCollision(this, this::canHit);
            if (hitResult.getType() != HitResult.Type.MISS)
                this.onCollision(hitResult);

            this.checkBlockCollision();
            Vec3d vec3d = this.getVelocity();
            double x = this.getX() + vec3d.x;
            double y = this.getY() + vec3d.y;
            double z = this.getZ() + vec3d.z;

            // Make bubbles in water
            if (this.isTouchingWater()) {
                for(int i = 0; i < 4; ++i)
                    this.getWorld().addParticle(ParticleTypes.BUBBLE, x - vec3d.x * 0.25, y - vec3d.y * 0.25, z - vec3d.z * 0.25, vec3d.x, vec3d.y, vec3d.z);
            }

            this.getWorld().addParticle(ParticleTypes.FLAME, x, y, z, 0.0, 0.0, 0.0);
            this.setPosition(x, y, z);
        } else {
            this.discard();
        }
    }

    @Override
    protected boolean canHit(Entity entity) {
        if (!entity.isAlive() || entity.isSpectator() || entity instanceof FlamethrowerProjectileEntity) return false;
        return entity.canBeHitByProjectile() || entity instanceof ItemEntity || entity instanceof ExperienceOrbEntity;
    }

    @Override
    public boolean doesRenderOnFire() { return false; }

    @Override
    public boolean canBeHitByProjectile() {
        return true;
    }

    @Override
    public boolean damage(DamageSource source, float amount) {
        if (!this.isInvulnerableTo(source)) {
            this.scheduleVelocityUpdate();
            Entity owner = source.getAttacker();
            if (owner != null) {
                if (!this.getWorld().isClient) {
                    Vec3d vec3d = owner.getRotationVector();
                    this.setVelocity(vec3d);
                    this.setOwner(owner);
                }
                return true;
            }
        }
        return false;
    }
}
