package net.hellomouse.alexscavesenriched.entity;

import com.github.alexmodguy.alexscaves.AlexsCaves;
import com.github.alexmodguy.alexscaves.client.ClientProxy;
import com.github.alexmodguy.alexscaves.client.particle.ACParticleRegistry;
import com.github.alexmodguy.alexscaves.server.block.blockentity.NuclearSirenBlockEntity;
import com.github.alexmodguy.alexscaves.server.block.poi.ACPOIRegistry;
import com.github.alexmodguy.alexscaves.server.entity.ACEntityRegistry;
import com.github.alexmodguy.alexscaves.server.entity.item.NuclearExplosionEntity;
import com.github.alexmodguy.alexscaves.server.potion.ACEffectRegistry;
import net.hellomouse.alexscavesenriched.*;
import net.hellomouse.alexscavesenriched.advancements.ACECriterionTriggers;
import net.hellomouse.alexscavesenriched.client.sound.RocketSound;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.EndGatewayBlock;
import net.minecraft.world.level.block.EndPortalBlock;
import net.minecraft.world.level.block.NetherPortalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.world.ForgeChunkManager;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.network.PlayMessages;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

import javax.annotation.Nullable;
import java.util.stream.Stream;

import static net.hellomouse.alexscavesenriched.ACESounds.ROCKET_WHISTLE;

public class RocketEntity extends AbstractArrow implements IRocketEntity {
    private static final EntityDataAccessor<Byte> IS_FLAME = SynchedEntityData.defineId(RocketEntity.class, EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<Integer> TYPE = SynchedEntityData.defineId(RocketEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> EXPLOSION_STRENGTH = SynchedEntityData.defineId(RocketEntity.class, EntityDataSerializers.FLOAT);

    // Bit flags
    public static final int TYPE_RADIOACTIVE = 1;
    public static final int TYPE_NUCLEAR = 1 << 1;
    public static final int TYPE_NEUTRON = 1 << 2;
    public static final int TYPE_MINI_NUKE = 1 << 3;

    // I give up getting portals to work - set vel to 0 at portal then save speed
    // to post portal speed, if non-0 restore it
    private static final EntityDataAccessor<Vector3f> POST_PORTAL_SPEED = SynchedEntityData.defineId(RocketEntity.class, EntityDataSerializers.VECTOR3);

    private boolean spawnedWhistleSound = false;

    public RocketEntity(EntityType entityType, Level level) {
        super(entityType, level);
    }

    public RocketEntity(Level level, LivingEntity shooter) {
        super(ACEEntityRegistry.ROCKET.get(), shooter, level);
    }

    public RocketEntity(Level level, double x, double y, double z) {
        super(ACEEntityRegistry.ROCKET.get(), x, y, z, level);
    }

    public RocketEntity(PlayMessages.SpawnEntity spawnEntity, Level level) {
        this(ACEEntityRegistry.ROCKET.get(), level);
        this.setBoundingBox(this.makeBoundingBox());
    }

    @Override
    protected void tickDespawn() { /* Never despawn */ }

    @Override
    public @NotNull Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag compoundTag) {
        super.readAdditionalSaveData(compoundTag);
        this.entityData.set(TYPE, compoundTag.getInt("type"));
        this.setPostPortalSpeed(new Vec3(
                compoundTag.getFloat("post_portal_speed_x"),
                compoundTag.getFloat("post_portal_speed_y"),
                compoundTag.getFloat("post_portal_speed_z")
        ));
    }

    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag compoundTag) {
        super.addAdditionalSaveData(compoundTag);
        compoundTag.putInt("type", this.entityData.get(TYPE));
        compoundTag.putDouble("post_portal_speed_x", this.getPostPortalSpeed().x);
        compoundTag.putDouble("post_portal_speed_y", this.getPostPortalSpeed().y);
        compoundTag.putDouble("post_portal_speed_z", this.getPostPortalSpeed().z);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(IS_FLAME, (byte) 0);
        this.entityData.define(TYPE, 0);
        this.entityData.define(EXPLOSION_STRENGTH, (float) AlexsCavesEnriched.CONFIG.rocket.nonNuclear.normal.explosionPower);
        this.entityData.define(POST_PORTAL_SPEED, new Vector3f(0.0F, 0.0F, 0.0F));
    }

    public float getExplosionStrength() {
        return this.entityData.get(EXPLOSION_STRENGTH);
    }

    public void setExplosionStrength(float f) {
        this.entityData.set(EXPLOSION_STRENGTH, f);
    }

    public boolean getIsFlame() {
        return this.entityData.get(IS_FLAME) != 0;
    }

    public void setIsFlame(boolean f) {
        this.entityData.set(IS_FLAME, (byte) (f ? 1 : 0));
    }

    public boolean getIsRadioactive() {
        return (this.entityData.get(TYPE) & TYPE_RADIOACTIVE) != 0;
    }

    public void setIsRadioactive(boolean f) {
        this.entityData.set(TYPE, f ? TYPE_RADIOACTIVE : 0);
    }

    public boolean getIsNuclear() {
        return (this.entityData.get(TYPE) & TYPE_NUCLEAR) != 0;
    }

    public void setIsNuclear(boolean f) {
        this.entityData.set(TYPE, f ? TYPE_NUCLEAR : 0);
    }

    public boolean getIsNeutron() {
        return (this.entityData.get(TYPE) & TYPE_NEUTRON) != 0;
    }

    public void setIsNeutron(boolean f) {
        this.entityData.set(TYPE, f ? TYPE_NEUTRON : 0);
    }

    public boolean getIsMiniNuke() {
        return (this.entityData.get(TYPE) & TYPE_MINI_NUKE) != 0;
    }

    public void setIsMiniNuke(boolean f) {
        this.entityData.set(TYPE, f ? TYPE_MINI_NUKE : 0);
    }

    public Vec3 getPostPortalSpeed() {
        Vector3f v = this.entityData.get(POST_PORTAL_SPEED);
        return new Vec3(v.x, v.y, v.z);
    }

    public void setPostPortalSpeed(Vec3 x) {
        this.entityData.set(POST_PORTAL_SPEED, new Vector3f(
                (float) x.x, (float) x.y, (float) x.z
        ));
    }

    @Nullable
    public Entity changeDimension(ServerLevel level, net.minecraftforge.common.util.ITeleporter teleporter) {
        var out = super.changeDimension(level, teleporter);
        if (out != null && (this.getIsNuclear() || this.getIsMiniNuke()) && this.getOwner() != null)
            ACECriterionTriggers.FIRE_NUKE_THROUGH_PORTAL.triggerForEntity(this.getOwner());
        return out;
    }

    public void detonate() {
        if (!this.getCommandSenderWorld().isClientSide) {
            if (this.getIsNuclear())
                this.detonateNuclear();
            else if (this.getIsNeutron())
                this.detonateNeutron();
            else if (this.getIsMiniNuke())
                this.detonateMiniNuke();
            else
                this.detonateNormal();
        }
    }

    private Stream<BlockPos> getNearbySirens(ServerLevel world, int range) {
        PoiManager pointofinterestmanager = world.getPoiManager();
        return pointofinterestmanager.findAll((poiTypeHolder) -> {
            assert ACPOIRegistry.NUCLEAR_SIREN.getKey() != null;
            return poiTypeHolder.is(ACPOIRegistry.NUCLEAR_SIREN.getKey());
        }, blockPos -> true, this.blockPosition(), range, PoiManager.Occupancy.ANY);
    }

    private void activateSiren(BlockPos pos) {
        BlockEntity var3 = this.getCommandSenderWorld().getBlockEntity(pos);
        if (var3 instanceof NuclearSirenBlockEntity nuclearSirenBlock) {
            nuclearSirenBlock.setNearestNuclearBomb(this);
        }
    }

    protected void detonateNuclear() {
        Entity explosion = AlexsCavesEnriched.CONFIG.nuclear.useNewNuke ?
                (NuclearExplosion2Entity) ((EntityType<?>) ACEEntityRegistry.NUCLEAR_EXPLOSION2.get()).create(this.getCommandSenderWorld()) :
                (NuclearExplosionEntity) ((EntityType<?>) ACEntityRegistry.NUCLEAR_EXPLOSION.get()).create(this.getCommandSenderWorld());
        assert explosion != null;
        explosion.copyPosition(this);
        if (explosion instanceof NuclearExplosionEntity)
            ((NuclearExplosionEntity) explosion).setSize((AlexsCaves.COMMON_CONFIG.nukeExplosionSizeModifier.get()).floatValue());
        else if (explosion instanceof NuclearExplosion2Entity)
            ((NuclearExplosion2Entity) explosion).setSize((AlexsCaves.COMMON_CONFIG.nukeExplosionSizeModifier.get()).floatValue());
        this.getCommandSenderWorld().addFreshEntity(explosion);
        this.discard();
    }

    protected void detonateMiniNuke() {
        Entity explosion = AlexsCavesEnriched.CONFIG.nuclear.useNewNuke ?
                (NuclearExplosion2Entity) ((EntityType<?>) ACEEntityRegistry.NUCLEAR_EXPLOSION2.get()).create(this.getCommandSenderWorld()) :
                (NuclearExplosionEntity) ((EntityType<?>) ACEntityRegistry.NUCLEAR_EXPLOSION.get()).create(this.getCommandSenderWorld());
        assert explosion != null;
        explosion.copyPosition(this);
        if (explosion instanceof NuclearExplosionEntity)
            ((NuclearExplosionEntity) explosion).setSize((float)AlexsCavesEnriched.CONFIG.miniNukeRadius);
        else if (explosion instanceof NuclearExplosion2Entity)
            ((NuclearExplosion2Entity) explosion).setSize((float)AlexsCavesEnriched.CONFIG.miniNukeRadius);
        this.getCommandSenderWorld().addFreshEntity(explosion);
        this.discard();
    }

    protected void detonateNeutron() {
        Entity explosion = ((EntityType<?>) ACEEntityRegistry.NEUTRON_EXPLOSION.get()).create(this.getCommandSenderWorld());
        assert explosion != null;
        explosion.copyPosition(this);
        ((NeutronExplosionEntity) explosion).setSize(AlexsCavesEnriched.CONFIG.neutron.radius);
        this.getCommandSenderWorld().addFreshEntity(explosion);
        this.discard();
    }

    protected void detonateNormal() {
        this.getCommandSenderWorld().explode(null, this.getX(), this.getY(), this.getZ(), this.getExplosionStrength(), Level.ExplosionInteraction.TNT);

        if (this.getIsFlame()) {
            final int radius = (int) Math.ceil(this.getExplosionStrength() * 1.4F);
            for (int dz = -radius; dz <= radius; dz++) {
                for (int dx = -radius; dx <= radius; dx++) {
                    if (this.random.nextInt(7) != 0)
                        continue;
                    if (dx * dx + dz * dz > radius * radius)
                        continue;

                    for (int dy = 0; dy < radius; dy++) {
                        BlockPos blockpos = new BlockPos(this.getBlockX() + dx, this.getBlockY() - dy, this.getBlockZ() + dz);
                        if (this.getCommandSenderWorld().getBlockState(blockpos).isAir() && this.getCommandSenderWorld().getBlockState(blockpos.below())
                                .isSolidRender(this.getCommandSenderWorld(), blockpos.below())) {
                            this.getCommandSenderWorld().setBlockAndUpdate(blockpos, BaseFireBlock.getState(this.getCommandSenderWorld(), blockpos));
                            break;
                        }
                    }
                }
            }

            AABB bashBox = new AABB(new BlockPos(this.getBlockX(), this.getBlockY(), this.getBlockZ()))
                    .inflate(this.getExplosionStrength() * 2.5F);
            for (LivingEntity entity : this.getCommandSenderWorld().getEntitiesOfClass(LivingEntity.class, bashBox))
                entity.setSecondsOnFire(5);
        }
        this.discard();

        if (this.getIsRadioactive() && AlexsCavesEnriched.CONFIG.rocket.nonNuclear.uranium.irradiationTime > 0 && AlexsCavesEnriched.CONFIG.rocket.nonNuclear.uranium.irradiationRadius > 0.0 && AlexsCavesEnriched.CONFIG.rocket.nonNuclear.uranium.irradiationPotionTime > 0) {
            // Search below for surface to place radiation cloud
            double deltaY = 0;
            for (; deltaY < 5.0; deltaY += 1.0) {
                BlockPos blockpos = new BlockPos(this.getBlockX(), this.getBlockY() - (int) deltaY, this.getBlockZ());
                if (this.getCommandSenderWorld().getBlockState(blockpos).isAir() &&
                        this.getCommandSenderWorld().getBlockState(blockpos.below()).isSolidRender(this.getCommandSenderWorld(), blockpos.below()))
                    break;
            }

            AreaEffectCloud areaeffectcloud = new AreaEffectCloud(this.getCommandSenderWorld(), this.getX(), this.getY() - deltaY + 1.2f, this.getZ());
            areaeffectcloud.setParticle(ACParticleRegistry.FALLOUT.get());
            areaeffectcloud.setFixedColor(0);
            areaeffectcloud.addEffect(new MobEffectInstance(ACEffectRegistry.IRRADIATED.get(), (int) AlexsCavesEnriched.CONFIG.rocket.nonNuclear.uranium.irradiationPotionTime, 1));
            areaeffectcloud.setRadius((float) AlexsCavesEnriched.CONFIG.rocket.nonNuclear.uranium.irradiationRadius);
            areaeffectcloud.setDuration(AlexsCavesEnriched.CONFIG.rocket.nonNuclear.uranium.irradiationTime);
            areaeffectcloud.setRadiusPerTick(-areaeffectcloud.getRadius() / (float) areaeffectcloud.getDuration());
            this.getCommandSenderWorld().addFreshEntity(areaeffectcloud);
        }
    }

    @Override
    protected void onHitEntity(@NotNull EntityHitResult res) {
        super.onHitEntity(res);
        this.detonate();
    }

    protected void reliablePortalStep() {
        if (!AlexsCavesEnriched.CONFIG.rocket.reliableWithPortals) return;
        if (this.getCommandSenderWorld().isClientSide) return;

        Vec3 postPortalSpeed = this.getPostPortalSpeed();
        if (postPortalSpeed.lengthSqr() > 0) {
            this.setDeltaMovement(postPortalSpeed);
            this.setPostPortalSpeed(new Vec3(0.0, 0.0, 0.0));
        }

        Vec3 vel = this.getDeltaMovement();
        if (vel.length() < 1.0F) return; // No need to check - vanilla check is reliable for low speeds

        BlockPos currentPos = new BlockPos(this.getBlockX(), this.getBlockY(), this.getBlockZ());
        BlockPos endPos = new BlockPos(
                (int) (currentPos.getX() + (vel.x >= 0 ? Math.ceil(vel.x) : Math.floor(vel.x))),
                (int) (currentPos.getY() + (vel.y >= 0 ? Math.ceil(vel.y) : Math.floor(vel.y))),
                (int) (currentPos.getZ() + (vel.z >= 0 ? Math.ceil(vel.z) : Math.floor(vel.z))));
        BlockPos ray = endPos.subtract(currentPos);
        BlockPos step = new BlockPos(
                ray.getX() >= 0 ? 1 : -1,
                ray.getY() >= 0 ? 1 : -1,
                ray.getZ() >= 0 ? 1 : -1
        );
        Vec3 tMax = new Vec3(
                ray.getX() != 0 ? ((double) step.getX() / ray.getX()) : 10000.0F,
                ray.getY() != 0 ? ((double) step.getY() / ray.getY()) : 10000.0F,
                ray.getZ() != 0 ? ((double) step.getZ() / ray.getZ()) : 10000.0F
        );
        Vec3 tDelta = new Vec3(tMax.x(), tMax.y(), tMax.z());
        int stepCount = 0;

        while (!currentPos.equals(endPos)) {
            stepCount++;
            if (stepCount > 70)
                break; // Prevent ultra velocity rockets from lagging too much

            if (tMax.x() < tMax.y()) {
                if (tMax.x() < tMax.z()) {
                    currentPos = currentPos.offset(new BlockPos(step.getX(), 0, 0));
                    tMax = tMax.add(new Vec3(tDelta.x(), 0.0, 0.0));
                } else {
                    currentPos = currentPos.offset(new BlockPos(0, 0, step.getZ()));
                    tMax = tMax.add(new Vec3(0, 0.0, tDelta.z()));
                }
            } else {
                if (tMax.y() < tMax.z()) {
                    currentPos = currentPos.offset(new BlockPos(0, step.getY(), 0));
                    tMax = tMax.add(new Vec3(0, tDelta.y(), 0));
                } else {
                    currentPos = currentPos.offset(new BlockPos(0, 0, step.getZ()));
                    tMax = tMax.add(new Vec3(0, 0.0, tDelta.z()));
                }
            }

            var block = this.getCommandSenderWorld().getBlockState(currentPos);
            if (block.getBlock() instanceof NetherPortalBlock || block.getBlock() instanceof EndGatewayBlock || block.getBlock() instanceof EndPortalBlock) {
                this.setPostPortalSpeed(vel);
                this.setDeltaMovement(vel.normalize().scale(0.01));
                this.setPos(currentPos.getX(), currentPos.getY(), currentPos.getZ());
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    public void clientTick() {
        if (!spawnedWhistleSound) {
            var rocketSoundInstance = new RocketSound(this, ACESounds.ROCKET_WHISTLE, SoundSource.AMBIENT);
            Minecraft.getInstance().getSoundManager().queueTickingSound(rocketSoundInstance);
            this.spawnedWhistleSound = true;
        }
    }

    public void tick() {
        this.reliablePortalStep();
        super.tick();

        if (this.inGround) {
            this.detonate();
        } else {
            if (this.getCommandSenderWorld().isClientSide) {
                if (this.getDeltaMovement().length() > 0.5F) {
                    Vec3 vec = this.getDeltaMovement().scale(-1.0);
                    Vec3 center1 = this.position();
                    Vec3 center2 = this.position().add(vec.scale(0.5));
                    this.getCommandSenderWorld().addParticle(ParticleTypes.FIREWORK, true, center1.x, center1.y, center1.z,
                            0, 0, 0);
                    this.getCommandSenderWorld().addParticle(ParticleTypes.FIREWORK, true, center2.x, center2.y, center2.z,
                            0, 0, 0);
                }

                this.clientTick();
            } else {
                // this.loadChunk();
                if ((this.getIsNuclear() || this.getIsNeutron() || this.getIsMiniNuke()) && (this.tickCount + this.getId()) % 10 == 0) {
                    if (this.getCommandSenderWorld() instanceof ServerLevel serverLevel)
                        this.getNearbySirens(serverLevel, 256).forEach(this::activateSiren);
                }
            }
        }
    }

    protected @NotNull SoundEvent getDefaultHitGroundSoundEvent() {
        return SoundEvents.GENERIC_EXPLODE;
    }

    @Override
    protected @NotNull ItemStack getPickupItem() {
        return new ItemStack(ACEItemRegistry.ROCKET.get());
    }
}

