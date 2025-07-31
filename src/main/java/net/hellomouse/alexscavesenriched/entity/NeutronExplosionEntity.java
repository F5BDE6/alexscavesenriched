package net.hellomouse.alexscavesenriched.entity;


import com.github.alexmodguy.alexscaves.AlexsCaves;
import com.github.alexmodguy.alexscaves.client.ClientProxy;
import com.github.alexmodguy.alexscaves.server.entity.living.RaycatEntity;
import com.github.alexmodguy.alexscaves.server.entity.living.TremorzillaEntity;
import com.github.alexmodguy.alexscaves.server.misc.ACDamageTypes;
import com.github.alexmodguy.alexscaves.server.misc.ACMath;
import com.github.alexmodguy.alexscaves.server.misc.ACSoundRegistry;
import com.github.alexmodguy.alexscaves.server.misc.ACTagRegistry;
import com.github.alexmodguy.alexscaves.server.potion.ACEffectRegistry;
import net.hellomouse.alexscavesenriched.ACEEntityRegistry;
import net.hellomouse.alexscavesenriched.ACEParticleRegistry;
import net.hellomouse.alexscavesenriched.ACERecipeRegistry;
import net.hellomouse.alexscavesenriched.AlexsCavesEnriched;
import net.hellomouse.alexscavesenriched.recipe.NeutronKillRecipe;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraftforge.common.world.ForgeChunkManager;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.network.PlayMessages;
import org.jetbrains.annotations.NotNull;

import java.util.Stack;

public class NeutronExplosionEntity extends Entity {
    private boolean spawnedParticle; // Mushroom cloud is a big particle

    // 16^3 subchunk actually
    // full = entire subchunk scheduled to be destroyed
    // partial = would be clipped by explosion
    private final Stack<BlockPos> toDestroyFullChunks;
    private final Stack<BlockPos> toDestroyPartialChunks;
    private boolean loadingChunks;

    private static final TrackedData<Float> SIZE;
    private static final TrackedData<Boolean> NO_GRIEFING;

    public enum ExplosionState {
        CALCULATE_WHAT_TO_DESTROY, DESTORYING, DONE
    }
    private ExplosionState explosionState = ExplosionState.CALCULATE_WHAT_TO_DESTROY;

    public NeutronExplosionEntity(EntityType<?> entityType, World level) {
        super(entityType, level);
        this.spawnedParticle = false;
        this.toDestroyPartialChunks = new Stack<>();
        this.toDestroyFullChunks = new Stack<>();
        this.loadingChunks = false;
    }

    public NeutronExplosionEntity(PlayMessages.SpawnEntity spawnEntity, World level) {
        this(ACEEntityRegistry.NEUTRON_EXPLOSION.get(), level);
        this.setBoundingBox(this.calculateBoundingBox());
    }

    public @NotNull Packet<ClientPlayPacketListener> createSpawnPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    public void tick() {
        super.tick();
        int chunksAffected = getChunksAffected();
        int radius = chunksAffected * 15;
        if (!spawnedParticle) {
            spawnedParticle = true;
            ClientProxy.renderNukeFlashFor = 8;
            playSound(ACSoundRegistry.NUCLEAR_EXPLOSION_RINGING.get(), 100, 50);

            getEntityWorld().addImportantParticle(ACEParticleRegistry.NEUTRON_BLAST.get(), true,
                    this.getX(), this.getY(), this.getZ(),
                0F, 0F, 0F);
        }
        if (age > 40 && explosionState == ExplosionState.DONE) {
            this.remove(RemovalReason.DISCARDED);
            return;
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
                if (toDestroyPartialChunks.isEmpty() && toDestroyFullChunks.isEmpty())
                    explosionState = ExplosionState.DONE;
            }
        }

        // Damage entities
        Box killBox = this.getBoundingBox().expand(radius * 1.1F, radius * 1.1F, radius * 1.1F);
        for (LivingEntity entity : this.getEntityWorld().getNonSpectatingEntities(LivingEntity.class, killBox)) {
            entity.addStatusEffect(new StatusEffectInstance(ACEffectRegistry.IRRADIATED.get(),
                    AlexsCavesEnriched.CONFIG.neutron.irradiationPotionTime,
                    AlexsCavesEnriched.CONFIG.neutron.irradiationPotionPower,
                    false, false, true));

            if (age <= 2) {
                float damage = AlexsCavesEnriched.CONFIG.neutron.burstDamage;
                if (entity instanceof RaycatEntity || entity instanceof TremorzillaEntity)
                    damage = 0;
                else if (entity.getType().isIn(ACTagRegistry.RESISTS_RADIATION))
                    damage *= 0.25F;
                if (damage > 0)
                    entity.damage(ACDamageTypes.causeNukeDamage(getEntityWorld().getRegistryManager()), damage);
            }
        }
    }

    private void loadChunksAround(boolean load) {
        NuclearExplosion2Entity.loadChunksInRadius(getEntityWorld(), this, new ChunkPos(this.getBlockPos()), load, this.getChunksAffected());
    }

    public static void tryTransmuteBlock(World world, BlockPos blockpos) {
        var currentState = world.getBlockState(blockpos);
        var recipes = world.getRecipeManager().listAllOfType(ACERecipeRegistry.NEUTRON_KILL_TYPE.get());
        for (NeutronKillRecipe recipe : recipes) {
            if (recipe.matches(currentState) && (recipe.getChance() == 1.0F || world.getRandom().nextFloat() <= recipe.getChance())) {
                // Don't replace fluids though
                if (recipe.getIsTag() && recipe.getInputLocation().equals(
                        Identifier.parse("minecraft:replaceable")) && !currentState.getFluidState().isEmpty())
                    continue;

                currentState = recipe.getOutput().getDefaultState();
                world.setBlockState(blockpos, currentState);
                break;
            }
        }
    }

    private void destroyChunk(int radius, Stack<BlockPos> stack, boolean checkSphere) {
        BlockPos chunkCorner = stack.pop();
        BlockPos.Mutable carve = new BlockPos.Mutable();
        BlockPos.Mutable carveBelow = new BlockPos.Mutable();
        carve.set(chunkCorner);
        carveBelow.set(chunkCorner);

        int maxDeltaY = Math.min(getEntityWorld().getTopY() - chunkCorner.getY(), 16);
        for (int x = 0; x < 16; x++)
            for (int z = 0; z < 16; z++)
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
                    if (!state.isAir() || !state.getFluidState().isEmpty())
                        tryTransmuteBlock(getEntityWorld(), carve);
                }
    }

    public void remove(@NotNull RemovalReason removalReason) {
        if (!this.getEntityWorld().isClient && this.loadingChunks) {
            this.loadingChunks = false;
            this.loadChunksAround(false);
        }
        super.remove(removalReason);
    }

    private int getChunksAffected() {
        return (int)Math.ceil(this.getSize());
    }

    protected void initDataTracker() {
        this.dataTracker.startTracking(SIZE, 1.0F);
        this.dataTracker.startTracking(NO_GRIEFING, false);
    }

    public float getSize() {
        return this.dataTracker.get(SIZE);
    }

    public void setSize(float f) {
        this.dataTracker.set(SIZE, f);
    }

    public boolean isNoGriefing() {
        return this.dataTracker.get(NO_GRIEFING);
    }

    public void setNoGriefing(boolean noGriefing) {
        this.dataTracker.set(NO_GRIEFING, noGriefing);
    }

    protected void readCustomDataFromNbt(NbtCompound compoundTag) {
        this.loadingChunks = compoundTag.getBoolean("WasLoadingChunks");
    }

    protected void writeCustomDataToNbt(NbtCompound compoundTag) {
        compoundTag.putBoolean("WasLoadingChunks", this.loadingChunks);
    }

    static {
        SIZE = DataTracker.registerData(NeutronExplosionEntity.class, TrackedDataHandlerRegistry.FLOAT);
        NO_GRIEFING = DataTracker.registerData(NeutronExplosionEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    }
}
