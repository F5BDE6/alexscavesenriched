package net.hellomouse.alexscavesenriched.block.block_entity;

import D;
import I;
import com.github.alexmodguy.alexscaves.server.misc.ACDamageTypes;
import com.github.alexmodguy.alexscaves.server.misc.ACTagRegistry;
import com.github.alexmodguy.alexscaves.server.potion.ACEffectRegistry;
import net.hellomouse.alexscavesenriched.ACEBlockEntityRegistry;
import net.hellomouse.alexscavesenriched.ACEParticleRegistry;
import net.hellomouse.alexscavesenriched.AlexsCavesEnriched;
import net.hellomouse.alexscavesenriched.client.particle.DemonCoreGlowParticle;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


public class EnrichedUraniumBlockEntity extends RadiationEmitterBlockEntity {
    final private static AttributeModifier inDemonCore = new AttributeModifier("In demon core", 1, AttributeModifier.Operation.ADDITION);
    private boolean checked = false;
    private boolean glowing = false;
    private int numBlocks = 0;
    private DemonCoreGlowParticle demonParticle = null;
    private boolean isFirst = false;
    private Vec3 center = new Vec3(0.0, 0.0, 0.0);
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

    @OnlyIn(Dist.CLIENT)
    public static void clientTick(Level level, BlockPos blockPos, BlockState p_155016_, EnrichedUraniumBlockEntity enrichedUraniumBlockEntity) {
        if (enrichedUraniumBlockEntity.glowing &&
                (enrichedUraniumBlockEntity.demonParticle == null || !enrichedUraniumBlockEntity.demonParticle.isAlive())) {
            Minecraft mc = Minecraft.getInstance();
            ParticleEngine particleManager = mc.particleEngine;

            Particle spawned = particleManager.createParticle(
                    ACEParticleRegistry.DEMONCORE_GLOW.get(),
                    enrichedUraniumBlockEntity.center.x(), enrichedUraniumBlockEntity.center.y(), enrichedUraniumBlockEntity.center.z(),
                    0.0, 0.0, 0.0
            );
            var dc = (DemonCoreGlowParticle) spawned;
            assert dc != null;
            dc.expandSize(enrichedUraniumBlockEntity.sizeBonus);
            enrichedUraniumBlockEntity.demonParticle = dc;
            particleManager.add(dc);
        } else if (!enrichedUraniumBlockEntity.glowing) {
            if (enrichedUraniumBlockEntity.demonParticle != null && enrichedUraniumBlockEntity.demonParticle.isAlive())
                enrichedUraniumBlockEntity.demonParticle.remove();
            enrichedUraniumBlockEntity.demonParticle = null;
        }
    }

    public int getNumBlocks() {
        return numBlocks;
    }

    public void setNumBlocks(int num) {
        numBlocks = num;
    }

    public static void tick(Level level, BlockPos blockPos, BlockState p_155016_, EnrichedUraniumBlockEntity enrichedUraniumBlockEntity) {
        enrichedUraniumBlockEntity.setChecked(false);
        if (enrichedUraniumBlockEntity.isGlowing()) {
            AABB bashBox = getDamageBox(blockPos);
            for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, bashBox)) {
                if (!entity.getType().is(ACTagRegistry.RESISTS_RADIATION)) {
                    var entityBlockPos = entity.blockPosition();
                    var inverseSquare = Math.min(1.0F, 1 / entityBlockPos.distSqr(blockPos));
                    entity.hurt(ACDamageTypes.causeRadiationDamage(entity.level().registryAccess()),
                            (float) (AlexsCavesEnriched.CONFIG.demonCore.damage * inverseSquare *
                                    enrichedUraniumBlockEntity.numBlocks));
                    entity.addEffect(new MobEffectInstance(ACEffectRegistry.IRRADIATED.get(),
                            4800, Math.min(enrichedUraniumBlockEntity.numBlocks, 9),
                            false, false, true));
                }
            }
        }
        RadiationEmitterBlockEntity.tick(level, blockPos, p_155016_, enrichedUraniumBlockEntity);

        // Spawn particle for glow
        if (level.isClientSide())
            clientTick(level, blockPos, p_155016_, enrichedUraniumBlockEntity);
    }

    private static @NotNull AABB getDamageBox(BlockPos blockPos) {
        var radius = AlexsCavesEnriched.CONFIG.demonCore.diameter / 2;
        var boxTopCorner = new BlockPos.MutableBlockPos(blockPos.getX(), blockPos.getY(), blockPos.getZ());
        boxTopCorner.move(-1, -1, -1);
        boxTopCorner.move(-radius, -radius, -radius);
        var boxBottomCorner = new BlockPos.MutableBlockPos(blockPos.getX(), blockPos.getY(), blockPos.getZ());
        boxBottomCorner.move(1, 1, 1);
        boxBottomCorner.move(radius, radius, radius);
        return new AABB(boxBottomCorner, boxTopCorner);
    }

    public void setFirst(boolean f) {
        isFirst = f;
    }

    public void setCenter(Vec3 c, float radiusBonus) {
        center = c;
        sizeBonus = radiusBonus;
    }

    private void loadNbt(CompoundTag tag) {
        checked = tag.getBoolean("checked");
        glowing = tag.getBoolean("glowing");
        isFirst = tag.getBoolean("isFirst");
        center = new Vec3(
                tag.getFloat("centerX"),
                tag.getFloat("centerY"),
                tag.getFloat("centerZ")
        );
        sizeBonus = tag.getFloat("sizeBonus");
        numBlocks = tag.getInt("numBlocks");
    }

    @Override
    protected void saveAdditional(CompoundTag p_187471_) {
        super.saveAdditional(p_187471_);
        writeMyNbt(p_187471_);
    }

    @Override
    public void load(CompoundTag p_155245_) {
        super.load(p_155245_);
        loadNbt(p_155245_);
    }

    @Override
    public @NotNull CompoundTag getUpdateTag() {
        var tag = super.getUpdateTag();
        saveAdditional(tag);
        return tag;
    }

    @Override
    public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() {
        var nbt = new CompoundTag();
        saveAdditional(nbt);
        return ClientboundBlockEntityDataPacket.create(this);
    }

    public EnrichedUraniumBlockEntity(BlockPos pos, BlockState state) {
        super(ACEBlockEntityRegistry.ENRICHED_URANIUM.get(), pos, state);
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        super.onDataPacket(net, pkt);
        assert pkt.getTag() != null;
        loadNbt(pkt.getTag());
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        super.handleUpdateTag(tag);
        loadNbt(tag);
    }

    private void writeMyNbt(CompoundTag nbt) {
        nbt.putBoolean("checked", checked);
        nbt.putBoolean("glowing", glowing);
        nbt.putBoolean("first", isFirst);
        nbt.putFloat("centerX", (float) center.x());
        nbt.putFloat("centerY", (float) center.y());
        nbt.putFloat("centerZ", (float) center.z());
        nbt.putFloat("sizeBonus", sizeBonus);
        nbt.putInt("numBlocks", numBlocks);
    }

    @Override
    public void setRemoved() {
        if (demonParticle != null)
            demonParticle.remove();
        demonParticle = null;
        super.setRemoved();
    }
}
