package net.hellomouse.alexscavesenriched.block;

import com.github.alexmodguy.alexscaves.server.block.ACSoundTypes;
import net.hellomouse.alexscavesenriched.block.abs.AbstractTntBlock;
import net.hellomouse.alexscavesenriched.entity.NeutronBombEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.MapColor;

public class NeutronBombBlock extends AbstractTntBlock {
    public NeutronBombBlock() {
        super(Properties.of()
                .mapColor(MapColor.METAL)
                .requiresCorrectToolForDrops()
                .strength(8, 1001)
                .sound(ACSoundTypes.NUCLEAR_BOMB));
        this.registerDefaultState(this.defaultBlockState().setValue(UNSTABLE, false));
    }

    public static void detonateStatic(Level world, BlockPos pos, @org.jetbrains.annotations.Nullable LivingEntity igniter) {
        if (!world.isClientSide) {
            NeutronBombEntity primedtnt = new NeutronBombEntity(world, pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, igniter);
            world.addFreshEntity(primedtnt);
            world.playSound(null, primedtnt.getX(), primedtnt.getY(), primedtnt.getZ(), SoundEvents.TNT_PRIMED, SoundSource.BLOCKS, 1.0F, 1.0F);
            world.gameEvent(igniter, GameEvent.PRIME_FUSE, pos);
        }
    }

    @Override
    public void detonate(Level world, BlockPos pos, @org.jetbrains.annotations.Nullable LivingEntity igniter) {
        detonateStatic(world, pos, igniter);
    }
}
