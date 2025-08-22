package net.hellomouse.alexscavesenriched.block;

import com.github.alexmodguy.alexscaves.server.potion.ACEffectRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AirBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.PushReaction;

public class NukeGlowingAir extends AirBlock {
    public NukeGlowingAir() {
        super(Properties.of()
                .noCollission()
                .air()
                .lightLevel(state -> 15)
                .replaceable()
                .randomTicks()
                .pushReaction(PushReaction.DESTROY));
    }

    @Override
    public void randomTick(BlockState state, ServerLevel world, BlockPos pos, RandomSource random) {
        world.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
    }

    @Override
    public void entityInside(BlockState state, Level world, BlockPos pos, Entity entity) {
        if (!world.isClientSide() && entity instanceof LivingEntity mob) {
            mob.addEffect(new MobEffectInstance(ACEffectRegistry.IRRADIATED.get(),
                    4800, 1, false, false, true));
        }
    }
}
