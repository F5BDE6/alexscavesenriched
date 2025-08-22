package net.hellomouse.alexscavesenriched.entity.abs;

import com.github.alexmodguy.alexscaves.client.particle.ACParticleRegistry;
import com.github.alexmodguy.alexscaves.server.block.blockentity.NuclearSirenBlockEntity;
import com.github.alexmodguy.alexscaves.server.block.poi.ACPOIRegistry;
import com.github.alexmodguy.alexscaves.server.misc.ACSoundRegistry;
import com.google.common.base.Predicates;
import net.minecraft.core.BlockPos;
import net.minecraft.entity.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.TraceableEntity;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.Tags;

import javax.annotation.Nullable;
import java.util.stream.Stream;

public abstract class AbstractNuclearTntEntity extends Entity implements TraceableEntity {
    protected static final EntityDataAccessor<Integer> FUSE = SynchedEntityData.defineId(AbstractNuclearTntEntity.class, EntityDataSerializers.INT);
    public static final int DEFAULT_FUSE = 30;
    @Nullable
    protected LivingEntity causingEntity;

    protected ItemLike dropItem = null;
    protected boolean triggerSiren = true;

    public AbstractNuclearTntEntity(EntityType<?> type, Level world) {
        super(type, world);
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(FUSE, DEFAULT_FUSE);
    }

    @Override
    protected MovementEmission getMovementEmission() {
        return MovementEmission.NONE;
    }

    @Override
    public boolean isPickable() {
        return !this.isRemoved();
    }

    @Override
    public void tick() {
        if (!this.isNoGravity())
            this.setDeltaMovement(this.getDeltaMovement().add(0.0, -0.04, 0.0));

        if (this.triggerSiren && (tickCount + this.getId()) % 10 == 0 && level() instanceof ServerLevel serverLevel)
            getNearbySirens(serverLevel, 256).forEach(this::activateSiren);

        this.move(MoverType.SELF, this.getDeltaMovement());
        this.setDeltaMovement(this.getDeltaMovement().scale(0.98));
        if (this.onGround())
            this.setDeltaMovement(this.getDeltaMovement().multiply(0.7, -0.5, 0.7));

        int i = this.getFuse() - 1;
        this.setFuse(i);
        if (i <= 0) {
            this.discard();
            if (!this.level().isClientSide)
                this.explode();
        } else {
            this.updateInWaterStateAndDoFluidPushing();
            if (this.level().isClientSide && DEFAULT_FUSE - i > 10 && random.nextFloat() < 0.3F && this.onGround()) {
                Vec3 center = this.getEyePosition();
                this.level().addParticle(ACParticleRegistry.PROTON.get(), center.x, center.y, center.z, center.x, center.y, center.z);
            }
        }
    }

    protected abstract void explode();

    @Override
    protected void addAdditionalSaveData(CompoundTag nbt) {
        nbt.putShort("Fuse", (short)this.getFuse());
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag nbt) {
        this.setFuse(nbt.getShort("Fuse"));
    }

    @Nullable
    public LivingEntity getOwner() {
        return this.causingEntity;
    }

    private void activateSiren(BlockPos pos) {
        if (level().getBlockEntity(pos) instanceof NuclearSirenBlockEntity nuclearSirenBlock)
            nuclearSirenBlock.setNearestNuclearBomb(this);
    }

    private Stream<BlockPos> getNearbySirens(ServerLevel world, int range) {
        PoiManager pointofinterestmanager = world.getPoiManager();
        return pointofinterestmanager.findAll((poiTypeHolder) -> poiTypeHolder.is(ACPOIRegistry.NUCLEAR_SIREN.getKey()), Predicates.alwaysTrue(), this.blockPosition(), range, PoiManager.Occupancy.ANY);
    }

    @Override
    protected float getEyeHeight(Pose pose, EntityDimensions dimensions) {
        return 0.15F;
    }

    public int getFuse() {
        return this.entityData.get(FUSE);
    }

    public void setFuse(int fuse) {
        this.entityData.set(FUSE, fuse);
    }

    @Override
    public ItemStack getPickResult() {
        if (dropItem == null) return null;
        return new ItemStack(dropItem);
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);
        if (itemStack.is(Tags.Items.SHEARS)) {
            player.swing(hand);
            this.playSound(ACSoundRegistry.NUCLEAR_BOMB_DEFUSE.get());
            this.remove(RemovalReason.KILLED);
            if (dropItem != null)
                this.spawnAtLocation(new ItemStack(dropItem));
            if (!player.getAbilities().instabuild) {
                itemStack.hurtAndBreak(1, player, (e) -> {
                    e.broadcastBreakEvent(hand);
                });
            }

            return InteractionResult.SUCCESS;
        } else if (player.isSecondaryUseActive()) {
            return InteractionResult.PASS;
        } else {
            return InteractionResult.SUCCESS;
        }
    }
}
