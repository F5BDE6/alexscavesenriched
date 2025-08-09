package net.hellomouse.alexscavesenriched.entity;

import com.github.alexmodguy.alexscaves.client.ClientProxy;
import com.github.alexmodguy.alexscaves.server.misc.ACMath;
import com.github.alexmodguy.alexscaves.server.misc.ACSoundRegistry;
import com.github.alexmodguy.alexscaves.server.misc.ACTagRegistry;
import net.hellomouse.alexscavesenriched.*;
import net.hellomouse.alexscavesenriched.client.ACEClientMod;
import net.hellomouse.alexscavesenriched.client.particle.BlackHoleSmokeParticle;
import net.hellomouse.alexscavesenriched.client.sound.BlackHoleSound;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.network.PlayMessages;
import org.jetbrains.annotations.NotNull;

import java.util.Stack;

public class BlackHoleEntity extends Entity {
    private static final int DEFAULT_DURATION = 40 * 60;
    private static final int EXPLOSION_DURATION = 20 * 45;
    private static final float DEFAULT_SIZE = 10;

    private boolean spawnedSound = false;
    private boolean spawnedParticle = false;

    // 16^3 subchunk actually
    // full = entire subchunk scheduled to be destroyed
    // partial = would be clipped by explosion
    private final Stack<BlockPos> toDestroyFullChunks;
    private final Stack<BlockPos> toDestroyPartialChunks;
    private boolean loadingChunks;
    public enum ExplosionState {CALCULATE_WHAT_TO_DESTROY, DESTORYING, DONE}
    private ExplosionState explosionState = ExplosionState.CALCULATE_WHAT_TO_DESTROY;

    // For collecting items
    private boolean gotWaterBucket = false;
    private boolean gotLavaBucket = false;

    private static final TrackedData<Float> SIZE;
    private static final TrackedData<Float> CURRENT_SIZE;
    private static final TrackedData<Integer> DECAY_DURATION_LEFT;
    private static final TrackedData<Boolean> NONDECAY;
    private static final TrackedData<Boolean> NO_GRIEFING;
    private static final TrackedData<Boolean> EXPLODING;
    private static final TrackedData<Integer> EXPLOSION_SIZE;

    public BlackHoleEntity(EntityType<?> entityType, World level) {
        super(entityType, level);
        this.toDestroyPartialChunks = new Stack<>();
        this.toDestroyFullChunks = new Stack<>();
        this.loadingChunks = false;
    }

    public BlackHoleEntity(PlayMessages.SpawnEntity spawnEntity, World level) {
        this(ACEEntityRegistry.BLACK_HOLE.get(), level);
        this.setBoundingBox(this.calculateBoundingBox());
    }

    public @NotNull Packet<ClientPlayPacketListener> createSpawnPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @OnlyIn(Dist.CLIENT)
    public void clientTick() {
        if (!spawnedSound) {
            spawnedSound = true;
            var sound = new BlackHoleSound(this, ACESounds.BLACKHOLE, SoundCategory.AMBIENT);
            MinecraftClient.getInstance().getSoundManager().playNextTick(sound);
        }
    }

    private void suckEntity(Entity entity, float powerMultiplier) {
        if (entity instanceof PlayerEntity player && ((player.isCreative() && player.getAbilities().flying) || player.isSpectator()))
            return;
        Vec3d dir = this.getPos().subtract(entity.getPos());
        double dis = Math.max(0.1, dir.length() - this.getCurrentSize());
        double suckPower = powerMultiplier * Math.min(0.5F / dis, 1F);
        suckPower = Math.min(10F, suckPower);
        entity.addVelocity(dir.normalize().multiply(suckPower));
    }

    @Override
    public void tick() {
        if (this.isExplosive()) {
            this.explodeTick();
        } else {
            this.postExplodeTick();
        }
    }

    @OnlyIn(Dist.CLIENT)
    public void explodeClientTick() {
        ACEClientMod.setNukeSky(ACEClientMod.NukeSkyType.BLACK_HOLE, 1F - (float)age / (EXPLOSION_DURATION * 2F));
    }

    protected void explodeTick() {
        if (this.getWorld().isClient) {
            this.explodeClientTick();
            this.clientTick();
        }

        int chunksAffected = getChunksAffected();
        int radius = chunksAffected * 15;
        if (!spawnedParticle) {
            spawnedParticle = true;
            ClientProxy.renderNukeFlashFor = 8;
            playSound(ACSoundRegistry.NUCLEAR_EXPLOSION_RINGING.get(), 100, 50);
        }

        this.setCurrentSize(Math.min(this.getCurrentSize() + 1, this.getExplosionSize() * 5));

        if (getEntityWorld().isClient) {
            if (explosionState != ExplosionState.DONE || age < EXPLOSION_DURATION - BlackHoleSmokeParticle.DEFAULT_AGE * 2) {
                for (int i = 0; i < 5; i++) {
                    Vec3d center = this.getPos().add(0, this.getCurrentSize() * 0.5, 0);
                    final double VEL = 7;
                    Vec3d delta = new Vec3d(
                            this.getWorld().random.nextFloat() - 0.5,
                            this.getWorld().random.nextFloat() - 0.5,
                            this.getWorld().random.nextFloat() - 0.5
                    ).normalize().multiply(BlackHoleSmokeParticle.DEFAULT_AGE * VEL * 2 + this.getCurrentSize() * 2F);
                    Vec3d delta2 = delta.normalize().multiply(-VEL * 0.5);
                    this.getWorld().addParticle(ACEParticleRegistry.BLACK_HOLE_SMOKE.get(),
                            center.x + delta.x, center.y + delta.y, center.z + delta.z,
                            delta2.x, delta2.y, delta2.z);
                }
            }

            this.clientTick();
        }

        if (!getEntityWorld().isClient && !isNoGriefing()) {
            if (!loadingChunks && !this.isRemoved()) {
                loadingChunks = true;
                loadChunksAround(true);
            }

            final int CHUNKS_TO_PROCESS_PER_TICK = 12;
            if (explosionState == ExplosionState.CALCULATE_WHAT_TO_DESTROY) {
                explosionState = ExplosionState.DESTORYING;
                BlockPos center = this.getBlockPos();
                for (int i = -chunksAffected; i <= chunksAffected; i++)
                    for (int j = -chunksAffected; j <= chunksAffected; j++)
                        for (int k = -chunksAffected; k <= chunksAffected; k++) {
                            var chunkPos = center.add(i * 16, j * 16, k * 16);
                            if (chunkPos.getSquaredDistance(center) < Math.pow(Math.max(0, 16 * (chunksAffected - 4)), 2) ||
                                    !NuclearExplosion2Entity.anyChunkVertexOutsideSphere(chunkPos, radius, center))
                                toDestroyFullChunks.push(chunkPos);
                            else
                                toDestroyPartialChunks.push(chunkPos);
                        }

                toDestroyFullChunks.sort((blockPos1, blockPos2) -> Double.compare(
                        NuclearExplosion2Entity.chunkBlockPosToDis(blockPos2, this.getBlockPos()),
                        NuclearExplosion2Entity.chunkBlockPosToDis(blockPos1, this.getBlockPos())));
                toDestroyPartialChunks.sort((blockPos1, blockPos2) -> Double.compare(
                        NuclearExplosion2Entity.chunkBlockPosToDis(blockPos2, this.getBlockPos()),
                        NuclearExplosion2Entity.chunkBlockPosToDis(blockPos1, this.getBlockPos())));
            } else if (explosionState == ExplosionState.DESTORYING) {
                int chunkToDestroyBudget = CHUNKS_TO_PROCESS_PER_TICK; // Chunks can destroy per tick
                while (chunkToDestroyBudget > 0 && !toDestroyFullChunks.isEmpty()) {
                    destroyChunk(radius, toDestroyFullChunks, false);
                    chunkToDestroyBudget--;
                }
                while (chunkToDestroyBudget > 0 && !toDestroyPartialChunks.isEmpty()) {
                    destroyChunk(radius, toDestroyPartialChunks, true);
                    chunkToDestroyBudget--;
                }
                if (toDestroyPartialChunks.isEmpty() && toDestroyFullChunks.isEmpty()) {
                    explosionState = ExplosionState.DONE;
                }
            } else if (explosionState == ExplosionState.DONE && age > EXPLOSION_DURATION) {
                this.setIsExplosive(false);
            }
        }

        // Damage entities
        Box killBox = this.getBoundingBox().expand(0.5);
        for (var entity : this.getEntityWorld().getNonSpectatingEntities(Entity.class, killBox)) {
            entity.damage(ACEDamageSources.causeBlackHoleDamage(getEntityWorld().getRegistryManager()), 500);
            if (!(entity instanceof LivingEntity) && !(entity instanceof BlackHoleEntity))
                entity.remove(RemovalReason.KILLED);
        }

        // Suck in entities
        Box suckBox = this.getBoundingBox().expand(radius * 1.1F, radius * 1.1F, radius * 1.1F);
        for (var entity : this.getEntityWorld().getNonSpectatingEntities(Entity.class, suckBox)) {
            this.suckEntity(entity, 10F);
        }
    }

    protected void postExplodeTick() {
        if (this.getWorld().isClient && this.getDecayDurationLeft() > 60) {
            Vec3d center = this.getEyePos();
            Vec3d delta = new Vec3d(
                this.getWorld().random.nextFloat() - 0.5,
                this.getWorld().random.nextFloat() - 0.5,
                this.getWorld().random.nextFloat() - 0.5
            ).normalize().multiply(1.5F + this.getCurrentSize());
            Vec3d delta2 = delta.normalize().multiply(-0.15);
            this.getWorld().addParticle(ParticleTypes.SMALL_FLAME, center.x + delta.x, center.y + delta.y, center.z + delta.z,
                    delta2.x, delta2.y, delta2.z);
            this.clientTick();
        }

        if (this.getDecayDurationLeft() > 0) {
            if (getEntityWorld().getTime() % 2 == 0) {
                var targetSize = (this.getDecayDurationLeft() / (float) DEFAULT_DURATION) * this.getSize();
                if (targetSize < this.getCurrentSize() - 5)
                    this.setCurrentSize(this.getCurrentSize() - 1);
                else
                    this.setCurrentSize(targetSize);
            }
            if (!this.isNonDecaying())
                this.setDecayDurationLeft(this.getDecayDurationLeft() - 1);
        } else {
            ClientProxy.renderNukeFlashFor = 8;
            Vec3d center = this.getEyePos();
            for (int i = 0; i < 20; i++) {
                Vec3d delta = new Vec3d(
                        this.getWorld().random.nextFloat() - 0.5,
                        this.getWorld().random.nextFloat() - 0.5,
                        this.getWorld().random.nextFloat() - 0.5
                ).normalize().multiply(1.5F);
                Vec3d delta2 = delta.normalize().multiply(-0.3);
                this.getWorld().addParticle(ParticleTypes.LARGE_SMOKE, center.x, center.y, center.z,
                        delta2.x, delta2.y, delta2.z);
            }
            this.getEntityWorld().createExplosion(null, this.getX(), this.getY(), this.getZ(), 4, World.ExplosionSourceType.TNT);
            this.remove(RemovalReason.DISCARDED);
            return;
        }

        Box suckBox = this.getBoundingBox().expand( 20F);
        for (var entity : this.getWorld().getOtherEntities(this, suckBox)) {
            this.suckEntity(entity, 1.8F);
        }

        Box killBox = this.getBoundingBox().expand( 0.5F);
        for (var entity : this.getWorld().getOtherEntities(this, killBox)) {
            if (entity instanceof PlayerEntity player && player.isCreative())
                continue;
            if (entity instanceof ItemEntity item) {
                if (item.getStack().isOf(Items.WATER_BUCKET))
                    gotWaterBucket = true;
                else if (item.getStack().isOf(Items.LAVA_BUCKET))
                    gotLavaBucket = true;
                else if (item.getStack().isOf(Items.PHANTOM_MEMBRANE)) {
                    this.setNonDecaying(false);
                    gotWaterBucket = gotLavaBucket = false;
                }

                if (gotWaterBucket && gotLavaBucket)
                    this.setNonDecaying(true);
            }
            entity.setVelocity(0, 0, 0);
            entity.damage(ACEDamageSources.causeBlackHoleDamage(getEntityWorld().getRegistryManager()), 500);
            if (!(entity instanceof LivingEntity) && !(entity instanceof BlackHoleEntity))
                entity.remove(RemovalReason.KILLED);
        }
    }

    // Explosion stuff
    // -----------------------------

    private int getChunksAffected() {
        return (int)Math.ceil(this.getExplosionSize());
    }

    private void destroyChunk(int radius, Stack<BlockPos> stack, boolean checkSphere) {
        BlockPos chunkCorner = stack.pop();
        BlockPos.Mutable carve = new BlockPos.Mutable();
        BlockPos.Mutable carveBelow = new BlockPos.Mutable();
        carve.set(chunkCorner);
        carveBelow.set(chunkCorner);

        int maxDeltaY = Math.min(getEntityWorld().getTopY() - chunkCorner.getY(), 17);
        for (int x = -1; x < 17; x++)
            for (int z = -1; z < 17; z++)
                for (int y = maxDeltaY; y >= 0; y--) {
                    if (chunkCorner.getY() + y < getEntityWorld().getBottomY()) break;
                    carve.set(chunkCorner.getX() + x, chunkCorner.getY() + y, chunkCorner.getZ() + z);

                    if (checkSphere) {
                        double yDist = ACMath.smin(0.6F - Math.abs(this.getBlockPos().getY() - carve.getY()) / (float) radius, 0.6F, 0.2F);
                        double distToCenter = carve.getSquaredDistance(this.getBlockPos().getX(), carve.getY() - 1, this.getBlockPos().getZ());
                        double targetRadius = yDist * radius * radius;

                        if (distToCenter <= targetRadius) {
                            float widthSimplexNoise1 = (ACMath.sampleNoise3D(carve.getX(), carve.getY(), carve.getZ(), radius) - 0.5F) * 0.45F + 0.55F;
                            targetRadius += yDist * (widthSimplexNoise1 * radius) * radius;
                            if (distToCenter >= targetRadius)
                                continue;
                        } else {
                            continue;
                        }
                    }

                    BlockState state = getEntityWorld().getBlockState(carve);
                    if ((!state.isAir() || !state.getFluidState().isEmpty()) && isDestroyable(state))
                        getEntityWorld().setBlockState(carve, Blocks.AIR.getDefaultState());
                }
    }

    private boolean isDestroyable(BlockState state) {
        return !state.isIn(ACTagRegistry.NUKE_PROOF); // && state.getBlock().getBlastResistance() < (float) AlexsCaves.COMMON_CONFIG.nukeMaxBlockExplosionResistance.get());
    }

    public void remove(@NotNull RemovalReason removalReason) {
        if (!this.getEntityWorld().isClient && this.loadingChunks) {
            this.loadingChunks = false;
            this.loadChunksAround(false);
        }
        super.remove(removalReason);
    }

    private void loadChunksAround(boolean load) {
        NuclearExplosion2Entity.loadChunksInRadius(getEntityWorld(), this, new ChunkPos(this.getBlockPos()), load, this.getChunksAffected());
    }

    // Black hole stuff
    // -----------------------------

    @Override
    public boolean shouldRender(double distance) {
        double d0 = this.getBoundingBox().getAverageSideLength() + this.getCurrentSize();
        if (Double.isNaN(d0)) d0 = 1.0;
        d0 *= 64.0;
        return distance < d0 * d0;
    }

    @Override
    public Box getVisibilityBoundingBox() {
        return this.getBoundingBox().expand(this.getCurrentSize() * 2);
    }

    @Override
    protected Box calculateBoundingBox() {
        return this.getDimensions(null).scaled(this.getCurrentSize()).getBoxAt(this.getPos());
    }

    protected void initDataTracker() {
        this.dataTracker.startTracking(SIZE, DEFAULT_SIZE);
        this.dataTracker.startTracking(NO_GRIEFING, false);

        this.dataTracker.startTracking(CURRENT_SIZE, DEFAULT_SIZE);
        this.dataTracker.startTracking(DECAY_DURATION_LEFT, DEFAULT_DURATION);
        this.dataTracker.startTracking(NONDECAY, false);
        this.dataTracker.startTracking(EXPLOSION_SIZE, AlexsCavesEnriched.CONFIG.blackHole.radius);
        this.dataTracker.startTracking(EXPLODING, false);
    }

    public float getSize() {
        return this.dataTracker.get(SIZE);
    }
    public void setSize(float f) {
        this.setCurrentSize(f);
        this.dataTracker.set(SIZE, f);
        this.setBoundingBox(this.calculateBoundingBox());
    }

    @Override
    protected void readCustomDataFromNbt(NbtCompound compoundTag) {
        this.setDecayDurationLeft(compoundTag.getInt("DecayDurationLeft"));
        this.setNonDecaying(compoundTag.getBoolean("NonDecaying"));
        this.setCurrentSize(compoundTag.getFloat("CurrentSize"));
        this.setSize(compoundTag.getFloat("Size"));
        this.setExplosionSize(compoundTag.getInt("ExplosionSize"));
        this.setIsExplosive(compoundTag.getBoolean("IsExplosive"));
        this.loadingChunks = compoundTag.getBoolean("WasLoadingChunks");

        if (this.isExplosive())
            explosionState = ExplosionState.CALCULATE_WHAT_TO_DESTROY; // Reset calculation on reload
    }

    @Override
    protected void writeCustomDataToNbt(NbtCompound compoundTag) {
        compoundTag.putInt("DecayDurationLeft", this.getDecayDurationLeft());
        compoundTag.putBoolean("NonDecaying", this.isNonDecaying());
        compoundTag.putFloat("CurrentSize", this.getCurrentSize());
        compoundTag.putFloat("Size", this.getSize());
        compoundTag.putBoolean("IsExplosive", this.isExplosive());
        compoundTag.putInt("ExplosionSize", this.getExplosionSize());
        compoundTag.putBoolean("WasLoadingChunks", this.loadingChunks);
    }

    static {
        SIZE = DataTracker.registerData(BlackHoleEntity.class, TrackedDataHandlerRegistry.FLOAT);
        NO_GRIEFING = DataTracker.registerData(BlackHoleEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
        CURRENT_SIZE = DataTracker.registerData(BlackHoleEntity.class, TrackedDataHandlerRegistry.FLOAT);
        DECAY_DURATION_LEFT = DataTracker.registerData(BlackHoleEntity.class, TrackedDataHandlerRegistry.INTEGER);
        NONDECAY = DataTracker.registerData(BlackHoleEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
        EXPLODING = DataTracker.registerData(BlackHoleEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
        EXPLOSION_SIZE = DataTracker.registerData(BlackHoleEntity.class, TrackedDataHandlerRegistry.INTEGER);
    }

    public int getDecayDurationLeft() { return this.dataTracker.get(DECAY_DURATION_LEFT); }
    public void setDecayDurationLeft(int decayDurationLeft) { this.dataTracker.set(DECAY_DURATION_LEFT, decayDurationLeft); }
    public float getCurrentSize() { return this.dataTracker.get(CURRENT_SIZE); }
    public void setCurrentSize(float currentSize) { this.dataTracker.set(CURRENT_SIZE, currentSize); }
    public boolean isNonDecaying() { return this.dataTracker.get(NONDECAY); }
    public void setNonDecaying(boolean nonDecaying) { this.dataTracker.set(NONDECAY, nonDecaying); }
    public boolean isNoGriefing() {return this.dataTracker.get(NO_GRIEFING);}
    public void setNoGriefing(boolean noGriefing) { this.dataTracker.set(NO_GRIEFING, noGriefing); }
    public int getExplosionSize() { return this.dataTracker.get(EXPLOSION_SIZE); }
    public void setExplosionSize(int currentSize) { this.dataTracker.set(EXPLOSION_SIZE, currentSize); }
    public boolean isExplosive() { return this.dataTracker.get(EXPLODING); }
    public void setIsExplosive(boolean t) { this.dataTracker.set(EXPLODING, t); }
}
