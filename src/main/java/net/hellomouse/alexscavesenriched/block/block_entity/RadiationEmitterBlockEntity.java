package net.hellomouse.alexscavesenriched.block.block_entity;

import com.github.alexmodguy.alexscaves.server.misc.ACTagRegistry;
import com.github.alexmodguy.alexscaves.server.potion.ACEffectRegistry;
import net.hellomouse.alexscavesenriched.ACEBlockEntityRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

public abstract class RadiationEmitterBlockEntity extends BlockEntity {

    public RadiationEmitterBlockEntity(BlockEntityType<?> p_155228_, BlockPos p_155229_, BlockState p_155230_) {
        super(p_155228_, p_155229_, p_155230_);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, RadiationEmitterBlockEntity blockEntity) {
        AABB bashBox = new AABB(pos).inflate(3.0D);
        for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, bashBox)) {
            if (!entity.getType().is(ACTagRegistry.RESISTS_RADIATION)) {
                entity.setDeltaMovement(entity.getDeltaMovement().multiply(0.9D, 1.0D, 0.9D));
                entity.addEffect(new MobEffectInstance(ACEffectRegistry.IRRADIATED.get(), 20 * 15 * 60, 1));
            }
        }
    }
}
