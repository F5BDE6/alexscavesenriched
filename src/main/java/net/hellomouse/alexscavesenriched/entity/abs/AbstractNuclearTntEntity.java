package net.hellomouse.alexscavesenriched.entity.abs;

import com.github.alexmodguy.alexscaves.client.particle.ACParticleRegistry;
import com.github.alexmodguy.alexscaves.server.block.blockentity.NuclearSirenBlockEntity;
import com.github.alexmodguy.alexscaves.server.block.poi.ACPOIRegistry;
import com.github.alexmodguy.alexscaves.server.misc.ACSoundRegistry;
import com.google.common.base.Predicates;
import net.minecraft.entity.*;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.poi.PointOfInterestStorage;
import net.minecraftforge.common.Tags;

import javax.annotation.Nullable;
import java.util.stream.Stream;

public abstract class AbstractNuclearTntEntity extends Entity implements Ownable {
    protected static final TrackedData<Integer> FUSE = DataTracker.registerData(AbstractNuclearTntEntity.class, TrackedDataHandlerRegistry.INTEGER);
    public static final int DEFAULT_FUSE = 30;
    @Nullable
    protected LivingEntity causingEntity;

    protected ItemConvertible dropItem = null;
    protected boolean triggerSiren = true;

    public AbstractNuclearTntEntity(EntityType<?> type, World world) {
        super(type, world);
    }

    @Override
    protected void initDataTracker() {
        this.dataTracker.startTracking(FUSE, DEFAULT_FUSE);
    }

    @Override
    protected MoveEffect getMoveEffect() {
        return MoveEffect.NONE;
    }

    @Override
    public boolean canHit() {
        return !this.isRemoved();
    }

    @Override
    public void tick() {
        if (!this.hasNoGravity())
            this.setVelocity(this.getVelocity().add(0.0, -0.04, 0.0));

        if (this.triggerSiren && (age + this.getId()) % 10 == 0 && getWorld() instanceof ServerWorld serverLevel)
            getNearbySirens(serverLevel, 256).forEach(this::activateSiren);

        this.move(MovementType.SELF, this.getVelocity());
        this.setVelocity(this.getVelocity().multiply(0.98));
        if (this.isOnGround())
            this.setVelocity(this.getVelocity().multiply(0.7, -0.5, 0.7));

        int i = this.getFuse() - 1;
        this.setFuse(i);
        if (i <= 0) {
            this.discard();
            if (!this.getWorld().isClient)
                this.explode();
        } else {
            this.updateWaterState();
            if (this.getWorld().isClient && DEFAULT_FUSE - i > 10 && random.nextFloat() < 0.3F && this.isOnGround()) {
                Vec3d center = this.getEyePos();
                this.getWorld().addParticle(ACParticleRegistry.PROTON.get(), center.x, center.y, center.z, center.x, center.y, center.z);
            }
        }
    }

    protected abstract void explode();

    @Override
    protected void writeCustomDataToNbt(NbtCompound nbt) {
        nbt.putShort("Fuse", (short)this.getFuse());
    }

    @Override
    protected void readCustomDataFromNbt(NbtCompound nbt) {
        this.setFuse(nbt.getShort("Fuse"));
    }

    @Nullable
    public LivingEntity getOwner() {
        return this.causingEntity;
    }

    private void activateSiren(BlockPos pos) {
        if (getWorld().getBlockEntity(pos) instanceof NuclearSirenBlockEntity nuclearSirenBlock)
            nuclearSirenBlock.setNearestNuclearBomb(this);
    }

    private Stream<BlockPos> getNearbySirens(ServerWorld world, int range) {
        PointOfInterestStorage pointofinterestmanager = world.getPointOfInterestStorage();
        return pointofinterestmanager.getPositions((poiTypeHolder) -> poiTypeHolder.matchesKey(ACPOIRegistry.NUCLEAR_SIREN.getKey()), Predicates.alwaysTrue(), this.getBlockPos(), range, PointOfInterestStorage.OccupationStatus.ANY);
    }

    @Override
    protected float getEyeHeight(EntityPose pose, EntityDimensions dimensions) {
        return 0.15F;
    }

    public void setFuse(int fuse) {
        this.dataTracker.set(FUSE, fuse);
    }

    public int getFuse() {
        return this.dataTracker.get(FUSE);
    }

    @Override
    public ItemStack getPickBlockStack() {
        if (dropItem == null) return null;
        return new ItemStack(dropItem);
    }

    @Override
    public ActionResult interact(PlayerEntity player, Hand hand) {
        ItemStack itemStack = player.getStackInHand(hand);
        if (itemStack.isIn(Tags.Items.SHEARS)) {
            player.swingHand(hand);
            this.playSoundIfNotSilent(ACSoundRegistry.NUCLEAR_BOMB_DEFUSE.get());
            this.remove(RemovalReason.KILLED);
            if (dropItem != null)
                this.dropStack(new ItemStack(dropItem));
            if (!player.getAbilities().creativeMode) {
                itemStack.damage(1, player, (e) -> {
                    e.sendToolBreakStatus(hand);
                });
            }

            return ActionResult.SUCCESS;
        } else if (player.shouldCancelInteraction()) {
            return ActionResult.PASS;
        } else {
            return ActionResult.SUCCESS;
        }
    }
}
