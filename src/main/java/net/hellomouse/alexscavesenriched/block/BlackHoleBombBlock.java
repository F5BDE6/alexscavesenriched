package net.hellomouse.alexscavesenriched.block;

import com.github.alexmodguy.alexscaves.server.block.ACSoundTypes;
import net.hellomouse.alexscavesenriched.block.abs.AbstractTntBlock;
import net.hellomouse.alexscavesenriched.entity.BlackHoleBombEntity;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.MapColor;
import net.minecraft.entity.LivingEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;

public class BlackHoleBombBlock extends AbstractTntBlock {
    public BlackHoleBombBlock() {
        super(AbstractBlock.Settings.create()
                .mapColor(MapColor.IRON_GRAY)
                .requiresTool()
                .strength(8, 1001)
                .sounds(ACSoundTypes.NUCLEAR_BOMB));
        this.setDefaultState(this.getDefaultState().with(UNSTABLE, false));
    }

    @Override
    public void detonate(World world, BlockPos pos, @org.jetbrains.annotations.Nullable LivingEntity igniter) {
        if (!world.isClient) {
            BlackHoleBombEntity primedtnt = new BlackHoleBombEntity(world, pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, igniter);
            world.spawnEntity(primedtnt);
            world.playSound(null, primedtnt.getX(), primedtnt.getY(), primedtnt.getZ(), SoundEvents.ENTITY_TNT_PRIMED, SoundCategory.BLOCKS, 1.0F, 1.0F);
            world.emitGameEvent(igniter, GameEvent.PRIME_FUSE, pos);
        }
    }
}
