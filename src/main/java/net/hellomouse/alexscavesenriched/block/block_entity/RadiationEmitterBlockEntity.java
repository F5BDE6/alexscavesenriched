package net.hellomouse.alexscavesenriched.block.block_entity;

import com.github.alexmodguy.alexscaves.server.misc.ACTagRegistry;
import com.github.alexmodguy.alexscaves.server.potion.ACEffectRegistry;
import net.hellomouse.alexscavesenriched.ACEBlockEntityRegistry;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;

public abstract class RadiationEmitterBlockEntity extends BlockEntity {

    public RadiationEmitterBlockEntity(BlockEntityType<?> p_155228_, BlockPos p_155229_, BlockState p_155230_) {
        super(p_155228_, p_155229_, p_155230_);
    }

    public static void tick(World level, BlockPos pos, BlockState state, RadiationEmitterBlockEntity blockEntity) {
        Box bashBox = new Box(pos).expand(3.0D);
        for (LivingEntity entity : level.getNonSpectatingEntities(LivingEntity.class, bashBox)) {
            if (!entity.getType().isIn(ACTagRegistry.RESISTS_RADIATION)) {
                entity.setVelocity(entity.getVelocity().multiply(0.9D, 1.0D, 0.9D));
                entity.addStatusEffect(new StatusEffectInstance(ACEffectRegistry.IRRADIATED.get(), 20 * 15 * 60,1));
            }
        }
    }
}
