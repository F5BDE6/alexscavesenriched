package net.hellomouse.alexscavesenriched.block;

import com.github.alexmodguy.alexscaves.server.potion.ACEffectRegistry;
import net.minecraft.block.AirBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

public class NukeGlowingAir extends AirBlock {
    public NukeGlowingAir() {
        super(Settings.create()
                .noCollision()
                .air()
                .luminance(state -> 15)
                .replaceable()
                .ticksRandomly()
                .pistonBehavior(PistonBehavior.DESTROY));
    }

    @Override
    public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        world.setBlockState(pos, Blocks.AIR.getDefaultState());
    }

    @Override
    public void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
        if (!world.isClient() && entity instanceof LivingEntity mob) {
            mob.addStatusEffect(new StatusEffectInstance(ACEffectRegistry.IRRADIATED.get(),
                    4800, 1, false, false, true));
        }
    }
}
