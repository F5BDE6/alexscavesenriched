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
import net.minecraft.block.AbstractFireBlock;
import net.minecraft.block.EndGatewayBlock;
import net.minecraft.block.EndPortalBlock;
import net.minecraft.block.NetherPortalBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.AreaEffectCloudEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.poi.PointOfInterestStorage;
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

public class RocketEntity extends PersistentProjectileEntity implements IRocketEntity {
    private static final TrackedData<Byte> IS_FLAME = DataTracker.registerData(RocketEntity.class, TrackedDataHandlerRegistry.BYTE);
    private static final TrackedData<Integer> TYPE = DataTracker.registerData(RocketEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<Float> EXPLOSION_STRENGTH = DataTracker.registerData(RocketEntity.class, TrackedDataHandlerRegistry.FLOAT);

    // Bit flags
    public static final int TYPE_RADIOACTIVE = 1;
    public static final int TYPE_NUCLEAR = 1 << 1;
    public static final int TYPE_NEUTRON = 1 << 2;
    public static final int TYPE_MINI_NUKE = 1 << 3;

    // I give up getting portals to work - set vel to 0 at portal then save speed
    // to post portal speed, if non-0 restore it
    private static final TrackedData<Vector3f> POST_PORTAL_SPEED = DataTracker.registerData(RocketEntity.class, TrackedDataHandlerRegistry.VECTOR3F);

    private boolean spawnedWhistleSound = false;

    public RocketEntity(EntityType entityType, World level) {
        super(entityType, level);
    }

    public RocketEntity(World level, LivingEntity shooter) {
        super(ACEEntityRegistry.ROCKET.get(), shooter, level);
    }

    public RocketEntity(World level, double x, double y, double z) {
        super(ACEEntityRegistry.ROCKET.get(), x, y, z, level);
    }

    public RocketEntity(PlayMessages.SpawnEntity spawnEntity, World level) {
        this(ACEEntityRegistry.ROCKET.get(), level);
        this.setBoundingBox(this.calculateBoundingBox());
    }

    @Override
    protected void age() { /* Never despawn */ }

    @Override
    public @NotNull Packet<ClientPlayPacketListener> createSpawnPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public void readCustomDataFromNbt(@NotNull NbtCompound compoundTag) {
        super.readCustomDataFromNbt(compoundTag);
        this.dataTracker.set(TYPE, compoundTag.getInt("type"));
        this.setPostPortalSpeed(new Vec3d(
                compoundTag.getFloat("post_portal_speed_x"),
                compoundTag.getFloat("post_portal_speed_y"),
                compoundTag.getFloat("post_portal_speed_z")
        ));
    }

    @Override
    public void writeCustomDataToNbt(@NotNull NbtCompound compoundTag) {
        super.writeCustomDataToNbt(compoundTag);
        compoundTag.putInt("type", this.dataTracker.get(TYPE));
        compoundTag.putDouble("post_portal_speed_x", this.getPostPortalSpeed().x);
        compoundTag.putDouble("post_portal_speed_y", this.getPostPortalSpeed().y);
        compoundTag.putDouble("post_portal_speed_z", this.getPostPortalSpeed().z);
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        this.dataTracker.startTracking(IS_FLAME, (byte) 0);
        this.dataTracker.startTracking(TYPE, 0);
        this.dataTracker.startTracking(EXPLOSION_STRENGTH, (float) AlexsCavesEnriched.CONFIG.rocket.nonNuclear.normal.explosionPower);
        this.dataTracker.startTracking(POST_PORTAL_SPEED, new Vector3f(0.0F, 0.0F, 0.0F));
    }

    public void setExplosionStrength(float f) {
        this.dataTracker.set(EXPLOSION_STRENGTH, f);
    }
    public float getExplosionStrength() {
        return this.dataTracker.get(EXPLOSION_STRENGTH);
    }

    public void setIsFlame(boolean f) {
        this.dataTracker.set(IS_FLAME, (byte) (f ? 1 : 0));
    }
    public boolean getIsFlame() {
        return this.dataTracker.get(IS_FLAME) != 0;
    }

    public void setIsRadioactive(boolean f) {
        this.dataTracker.set(TYPE, f ? TYPE_RADIOACTIVE : 0);
    }
    public boolean getIsRadioactive() {
        return (this.dataTracker.get(TYPE) & TYPE_RADIOACTIVE) != 0;
    }

    public void setIsNuclear(boolean f) {
        this.dataTracker.set(TYPE, f ? TYPE_NUCLEAR : 0);
    }
    public boolean getIsNuclear() {
        return (this.dataTracker.get(TYPE) & TYPE_NUCLEAR) != 0;
    }


    public void setIsNeutron(boolean f) {
        this.dataTracker.set(TYPE, f ? TYPE_NEUTRON : 0);
    }
    public boolean getIsNeutron() {
        return (this.dataTracker.get(TYPE) & TYPE_NEUTRON) != 0;
    }

    public void setIsMiniNuke(boolean f) { this.dataTracker.set(TYPE, f ? TYPE_MINI_NUKE : 0); }
    public boolean getIsMiniNuke() { return (this.dataTracker.get(TYPE) & TYPE_MINI_NUKE) != 0; }

    public void setPostPortalSpeed(Vec3d x) {
        this.dataTracker.set(POST_PORTAL_SPEED, new Vector3f(
                (float) x.x, (float) x.y, (float) x.z
        ));
    }

    public Vec3d getPostPortalSpeed() {
        Vector3f v = this.dataTracker.get(POST_PORTAL_SPEED);
        return new Vec3d(v.x, v.y, v.z);
    }

    @Nullable
    public Entity changeDimension(ServerWorld level, net.minecraftforge.common.util.ITeleporter teleporter) {
        var out = super.changeDimension(level, teleporter);
        if (out != null && (this.getIsNuclear() || this.getIsMiniNuke()) && this.getOwner() != null)
            ACECriterionTriggers.FIRE_NUKE_THROUGH_PORTAL.triggerForEntity(this.getOwner());
        return out;
    }

    public void detonate() {
        if (!this.getEntityWorld().isClient) {
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

    private Stream<BlockPos> getNearbySirens(ServerWorld world, int range) {
        PointOfInterestStorage pointofinterestmanager = world.getPointOfInterestStorage();
        return pointofinterestmanager.getPositions((poiTypeHolder) -> {
            assert ACPOIRegistry.NUCLEAR_SIREN.getKey() != null;
            return poiTypeHolder.matchesKey(ACPOIRegistry.NUCLEAR_SIREN.getKey());
        }, blockPos -> true, this.getBlockPos(), range, PointOfInterestStorage.OccupationStatus.ANY);
    }

    private void activateSiren(BlockPos pos) {
        BlockEntity var3 = this.getEntityWorld().getBlockEntity(pos);
        if (var3 instanceof NuclearSirenBlockEntity nuclearSirenBlock) {
            nuclearSirenBlock.setNearestNuclearBomb(this);
        }
    }

    protected void detonateNuclear() {
        Entity explosion = AlexsCavesEnriched.CONFIG.nuclear.useNewNuke ?
                (NuclearExplosion2Entity) ((EntityType<?>) ACEEntityRegistry.NUCLEAR_EXPLOSION2.get()).create(this.getEntityWorld()) :
                (NuclearExplosionEntity) ((EntityType<?>) ACEntityRegistry.NUCLEAR_EXPLOSION.get()).create(this.getEntityWorld());
        assert explosion != null;
        explosion.copyPositionAndRotation(this);
        if (explosion instanceof NuclearExplosionEntity)
            ((NuclearExplosionEntity) explosion).setSize((AlexsCaves.COMMON_CONFIG.nukeExplosionSizeModifier.get()).floatValue());
        else if (explosion instanceof NuclearExplosion2Entity)
            ((NuclearExplosion2Entity) explosion).setSize((AlexsCaves.COMMON_CONFIG.nukeExplosionSizeModifier.get()).floatValue());
        this.getEntityWorld().spawnEntity(explosion);
        this.discard();
    }

    protected void detonateMiniNuke() {
        Entity explosion = AlexsCavesEnriched.CONFIG.nuclear.useNewNuke ?
                (NuclearExplosion2Entity) ((EntityType<?>) ACEEntityRegistry.NUCLEAR_EXPLOSION2.get()).create(this.getEntityWorld()) :
                (NuclearExplosionEntity) ((EntityType<?>) ACEntityRegistry.NUCLEAR_EXPLOSION.get()).create(this.getEntityWorld());
        assert explosion != null;
        explosion.copyPositionAndRotation(this);
        if (explosion instanceof NuclearExplosionEntity)
            ((NuclearExplosionEntity) explosion).setSize((float)AlexsCavesEnriched.CONFIG.miniNukeRadius);
        else if (explosion instanceof NuclearExplosion2Entity)
            ((NuclearExplosion2Entity) explosion).setSize((float)AlexsCavesEnriched.CONFIG.miniNukeRadius);
        this.getEntityWorld().spawnEntity(explosion);
        this.discard();
    }

    protected void detonateNeutron() {
        Entity explosion = ((EntityType<?>) ACEEntityRegistry.NEUTRON_EXPLOSION.get()).create(this.getEntityWorld());
        assert explosion != null;
        explosion.copyPositionAndRotation(this);
        ((NeutronExplosionEntity) explosion).setSize(AlexsCavesEnriched.CONFIG.neutron.radius);
        this.getEntityWorld().spawnEntity(explosion);
        this.discard();
    }

    protected void detonateNormal() {
        this.getEntityWorld().createExplosion(null, this.getX(), this.getY(), this.getZ(), this.getExplosionStrength(), World.ExplosionSourceType.TNT);

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
                        if (this.getEntityWorld().getBlockState(blockpos).isAir() && this.getEntityWorld().getBlockState(blockpos.down())
                                .isOpaqueFullCube(this.getEntityWorld(), blockpos.down())) {
                            this.getEntityWorld().setBlockState(blockpos, AbstractFireBlock.getState(this.getEntityWorld(), blockpos));
                            break;
                        }
                    }
                }
            }

            Box bashBox = new Box(new BlockPos(this.getBlockX(), this.getBlockY(), this.getBlockZ()))
                    .expand(this.getExplosionStrength() * 2.5F);
            for (LivingEntity entity : this.getEntityWorld().getNonSpectatingEntities(LivingEntity.class, bashBox))
                entity.setOnFireFor(5);
        }
        this.discard();

        if (this.getIsRadioactive() && AlexsCavesEnriched.CONFIG.rocket.nonNuclear.uranium.irradiationTime > 0 && AlexsCavesEnriched.CONFIG.rocket.nonNuclear.uranium.irradiationRadius > 0.0 && AlexsCavesEnriched.CONFIG.rocket.nonNuclear.uranium.irradiationPotionTime > 0) {
            // Search below for surface to place radiation cloud
            double deltaY = 0;
            for (; deltaY < 5.0; deltaY += 1.0) {
                BlockPos blockpos = new BlockPos(this.getBlockX(), this.getBlockY() - (int) deltaY, this.getBlockZ());
                if (this.getEntityWorld().getBlockState(blockpos).isAir() &&
                        this.getEntityWorld().getBlockState(blockpos.down()).isOpaqueFullCube(this.getEntityWorld(), blockpos.down()))
                    break;
            }

            AreaEffectCloudEntity areaeffectcloud = new AreaEffectCloudEntity(this.getEntityWorld(), this.getX(), this.getY() - deltaY + 1.2f, this.getZ());
            areaeffectcloud.setParticleType(ACParticleRegistry.FALLOUT.get());
            areaeffectcloud.setColor(0);
            areaeffectcloud.addEffect(new StatusEffectInstance(ACEffectRegistry.IRRADIATED.get(), (int) AlexsCavesEnriched.CONFIG.rocket.nonNuclear.uranium.irradiationPotionTime, 1));
            areaeffectcloud.setRadius((float) AlexsCavesEnriched.CONFIG.rocket.nonNuclear.uranium.irradiationRadius);
            areaeffectcloud.setDuration(AlexsCavesEnriched.CONFIG.rocket.nonNuclear.uranium.irradiationTime);
            areaeffectcloud.setRadiusGrowth(-areaeffectcloud.getRadius() / (float) areaeffectcloud.getDuration());
            this.getEntityWorld().spawnEntity(areaeffectcloud);
        }
    }

    @Override
    protected void onEntityHit(@NotNull EntityHitResult res) {
        super.onEntityHit(res);
        this.detonate();
    }

    protected void reliablePortalStep() {
        if (!AlexsCavesEnriched.CONFIG.rocket.reliableWithPortals) return;
        if (this.getEntityWorld().isClient) return;

        Vec3d postPortalSpeed = this.getPostPortalSpeed();
        if (postPortalSpeed.lengthSquared() > 0) {
            this.setVelocity(postPortalSpeed);
            this.setPostPortalSpeed(new Vec3d(0.0, 0.0, 0.0));
        }

        Vec3d vel = this.getVelocity();
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
        Vec3d tMax = new Vec3d(
                ray.getX() != 0 ? ((double) step.getX() / ray.getX()) : 10000.0F,
                ray.getY() != 0 ? ((double) step.getY() / ray.getY()) : 10000.0F,
                ray.getZ() != 0 ? ((double) step.getZ() / ray.getZ()) : 10000.0F
        );
        Vec3d tDelta = new Vec3d(tMax.getX(), tMax.getY(), tMax.getZ());
        int stepCount = 0;

        while (!currentPos.equals(endPos)) {
            stepCount++;
            if (stepCount > 70)
                break; // Prevent ultra velocity rockets from lagging too much

            if (tMax.getX() < tMax.getY()) {
                if (tMax.getX() < tMax.getZ()) {
                    currentPos = currentPos.add(new BlockPos(step.getX(), 0, 0));
                    tMax = tMax.add(new Vec3d(tDelta.getX(), 0.0, 0.0));
                } else {
                    currentPos = currentPos.add(new BlockPos(0, 0, step.getZ()));
                    tMax = tMax.add(new Vec3d(0, 0.0, tDelta.getZ()));
                }
            } else {
                if (tMax.getY() < tMax.getZ()) {
                    currentPos = currentPos.add(new BlockPos(0, step.getY(), 0));
                    tMax = tMax.add(new Vec3d(0, tDelta.getY(), 0));
                } else {
                    currentPos = currentPos.add(new BlockPos(0, 0, step.getZ()));
                    tMax = tMax.add(new Vec3d(0, 0.0, tDelta.getZ()));
                }
            }

            var block = this.getEntityWorld().getBlockState(currentPos);
            if (block.getBlock() instanceof NetherPortalBlock || block.getBlock() instanceof EndGatewayBlock || block.getBlock() instanceof EndPortalBlock) {
                this.setPostPortalSpeed(vel);
                this.setVelocity(vel.normalize().multiply(0.01));
                this.setPosition(currentPos.getX(), currentPos.getY(), currentPos.getZ());
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    public void clientTick() {
        if (!spawnedWhistleSound) {
            var rocketSoundInstance = new RocketSound(this, ACESounds.ROCKET_WHISTLE, SoundCategory.AMBIENT);
            MinecraftClient.getInstance().getSoundManager().playNextTick(rocketSoundInstance);
            this.spawnedWhistleSound = true;
        }
    }

    public void tick() {
        this.reliablePortalStep();
        super.tick();

        if (this.inGround) {
            this.detonate();
        } else {
            if (this.getEntityWorld().isClient) {
                if (this.getVelocity().length() > 0.5F) {
                    Vec3d vec = this.getVelocity().multiply(-1.0);
                    Vec3d center1 = this.getPos();
                    Vec3d center2 = this.getPos().add(vec.multiply(0.5));
                    this.getEntityWorld().addParticle(ParticleTypes.FIREWORK, true, center1.x, center1.y, center1.z,
                            0, 0, 0);
                    this.getEntityWorld().addParticle(ParticleTypes.FIREWORK, true, center2.x, center2.y, center2.z,
                            0, 0, 0);
                }

                this.clientTick();
            } else {
                // this.loadChunk();
                if ((this.getIsNuclear() || this.getIsNeutron() || this.getIsMiniNuke()) && (this.age + this.getId()) % 10 == 0) {
                    if (this.getEntityWorld() instanceof ServerWorld serverLevel)
                        this.getNearbySirens(serverLevel, 256).forEach(this::activateSiren);
                }
            }
        }
    }

    protected @NotNull SoundEvent getHitSound() {
        return SoundEvents.ENTITY_GENERIC_EXPLODE;
    }

    @Override
    protected @NotNull ItemStack asItemStack() {
        return new ItemStack(ACEItemRegistry.ROCKET.get());
    }
}

