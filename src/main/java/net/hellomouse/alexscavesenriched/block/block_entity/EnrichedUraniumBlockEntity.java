package net.hellomouse.alexscavesenriched.block.block_entity;

import com.github.alexmodguy.alexscaves.server.misc.ACDamageTypes;
import com.github.alexmodguy.alexscaves.server.misc.ACTagRegistry;

import com.github.alexmodguy.alexscaves.server.potion.ACEffectRegistry;
import net.hellomouse.alexscavesenriched.ACEBlockEntityRegistry;
import net.hellomouse.alexscavesenriched.ACEParticleRegistry;
import net.hellomouse.alexscavesenriched.AlexsCavesEnriched;
import net.hellomouse.alexscavesenriched.client.particle.DemonCoreGlowParticle;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeModifier;

import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;

import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


public class EnrichedUraniumBlockEntity extends RadiationEmitterBlockEntity {
    final private static EntityAttributeModifier inDemonCore = new EntityAttributeModifier("In demon core", 1, EntityAttributeModifier.Operation.ADDITION);
    private boolean checked = false;
    private boolean glowing = false;
    private int numBlocks = 0;
    private DemonCoreGlowParticle demonParticle = null;
    private boolean isFirst = false;
    private Vec3d center = new Vec3d(0.0, 0.0, 0.0);
    private float sizeBonus = 0.0F;

    public boolean isChecked() {
        return checked;
    }
    public void setChecked(boolean checked) {
        this.checked = checked;
    }
    public boolean isGlowing() {
        return glowing;
    }
    public void setGlowing(boolean glowing) {
        this.glowing = glowing;
    }
    public int getNumBlocks() { return numBlocks; }
    public void setNumBlocks(int num) { numBlocks = num; }
    public void setFirst(boolean f) { isFirst = f; }
    public void setCenter(Vec3d c, float radiusBonus) {
        center = c;
        sizeBonus = radiusBonus;
    }

    private void loadNbt(NbtCompound tag) {
        checked = tag.getBoolean("checked");
        glowing = tag.getBoolean("glowing");
        isFirst = tag.getBoolean("isFirst");
        center = new Vec3d(
                tag.getFloat("centerX"),
                tag.getFloat("centerY"),
                tag.getFloat("centerZ")
        );
        sizeBonus = tag.getFloat("sizeBonus");
        numBlocks = tag.getInt("numBlocks");
    }

    private void writeMyNbt(NbtCompound nbt) {
        nbt.putBoolean("checked", checked);
        nbt.putBoolean("glowing", glowing);
        nbt.putBoolean("first", isFirst);
        nbt.putFloat("centerX", (float)center.getX());
        nbt.putFloat("centerY", (float)center.getY());
        nbt.putFloat("centerZ", (float)center.getZ());
        nbt.putFloat("sizeBonus", sizeBonus);
        nbt.putInt("numBlocks", numBlocks);
    }

    @Override
    protected void writeNbt(NbtCompound p_187471_) {
        super.writeNbt(p_187471_);
        writeMyNbt(p_187471_);
    }

    @Override
    public void readNbt(NbtCompound p_155245_) {
        super.readNbt(p_155245_);
        loadNbt(p_155245_);
    }

    @Override
    public @NotNull NbtCompound toInitialChunkDataNbt() {
        var tag = super.toInitialChunkDataNbt();
        writeNbt(tag);
        return tag;
    }

    @Override
    public @Nullable Packet<ClientPlayPacketListener> toUpdatePacket() {
        var nbt = new NbtCompound();
        writeNbt(nbt);
        return BlockEntityUpdateS2CPacket.create(this);
    }

    @Override
    public void onDataPacket(ClientConnection net, BlockEntityUpdateS2CPacket pkt) {
        super.onDataPacket(net, pkt);
        assert pkt.getNbt() != null;
        loadNbt(pkt.getNbt());
    }

    @Override
    public void handleUpdateTag(NbtCompound tag) {
        super.handleUpdateTag(tag);
        loadNbt(tag);
    }

    public EnrichedUraniumBlockEntity(BlockPos pos, BlockState state) {
        super(ACEBlockEntityRegistry.ENRICHED_URANIUM.get(), pos, state);
    }

    public static void tick(World level, BlockPos blockPos, BlockState p_155016_, EnrichedUraniumBlockEntity enrichedUraniumBlockEntity) {
        enrichedUraniumBlockEntity.setChecked(false);
        if (enrichedUraniumBlockEntity.isGlowing()) {
            Box bashBox = getDamageBox(blockPos);
            for (LivingEntity entity : level.getNonSpectatingEntities(LivingEntity.class, bashBox)) {
                if (!entity.getType().isIn(ACTagRegistry.RESISTS_RADIATION)) {
                    var entityBlockPos = entity.getBlockPos();
                    var inverseSquare = Math.min(1.0F, 1 / entityBlockPos.getSquaredDistance(blockPos));
                    entity.damage(ACDamageTypes.causeRadiationDamage(entity.getWorld().getRegistryManager()),
                            (float) (AlexsCavesEnriched.CONFIG.demonCore.damage * inverseSquare *
                                    enrichedUraniumBlockEntity.numBlocks));
                    entity.addStatusEffect(new StatusEffectInstance(ACEffectRegistry.IRRADIATED.get(),
                            4800, Math.min(enrichedUraniumBlockEntity.numBlocks, 9),
                            false, false, true));
                }
            }
        }
        RadiationEmitterBlockEntity.tick(level, blockPos, p_155016_, enrichedUraniumBlockEntity);

        // Spawn particle for glow
        if (level.isClient()) {
            if (enrichedUraniumBlockEntity.glowing &&
                    (enrichedUraniumBlockEntity.demonParticle == null || !enrichedUraniumBlockEntity.demonParticle.isAlive())) {
                MinecraftClient mc = MinecraftClient.getInstance();
                ParticleManager particleManager = mc.particleManager;

                DemonCoreGlowParticle particle = (DemonCoreGlowParticle)(new DemonCoreGlowParticle.Factory()).createParticle(ACEParticleRegistry.DEMONCORE_GLOW.get(), (ClientWorld) level,
                        enrichedUraniumBlockEntity.center.getX(), enrichedUraniumBlockEntity.center.getY(), enrichedUraniumBlockEntity.center.getZ(),
                        0.0, 0.0, 0.0);

                particle.expandSize(enrichedUraniumBlockEntity.sizeBonus);
                enrichedUraniumBlockEntity.demonParticle = particle;
                particleManager.addParticle(particle);
            } else if (!enrichedUraniumBlockEntity.glowing) {
                if (enrichedUraniumBlockEntity.demonParticle != null && enrichedUraniumBlockEntity.demonParticle.isAlive())
                    enrichedUraniumBlockEntity.demonParticle.markDead();
                enrichedUraniumBlockEntity.demonParticle = null;
            }
        }
    }

    @Override
    public void markRemoved() {
        if (demonParticle != null)
            demonParticle.markDead();
        demonParticle = null;
        super.markRemoved();
    }

    private static @NotNull Box getDamageBox(BlockPos blockPos) {
        var radius = AlexsCavesEnriched.CONFIG.demonCore.diameter / 2;
        var boxTopCorner = new BlockPos.Mutable(blockPos.getX(), blockPos.getY(), blockPos.getZ());
        boxTopCorner.move(-1, -1, -1);
        boxTopCorner.move(-radius, -radius, -radius);
        var boxBottomCorner = new BlockPos.Mutable(blockPos.getX(), blockPos.getY(), blockPos.getZ());
        boxBottomCorner.move(1, 1, 1);
        boxBottomCorner.move(radius, radius, radius);
        return new Box(boxBottomCorner, boxTopCorner);
    }
}
