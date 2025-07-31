package net.hellomouse.alexscavesenriched.block;

import com.github.alexmodguy.alexscaves.server.block.ACSoundTypes;
import net.hellomouse.alexscavesenriched.block.abs.AbstractTntBlock;
import net.hellomouse.alexscavesenriched.entity.MiniNukeEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.MapColor;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.entity.LivingEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;

public class MiniNukeBlock extends AbstractTntBlock {
    protected static final VoxelShape SHAPE = Block.createCuboidShape(3.0, 0.0, 3.0, 10.0, 10.0, 10.0);

    public MiniNukeBlock() {
        super(Settings.create()
                .mapColor(MapColor.IRON_GRAY)
                .requiresTool()
                .nonOpaque()
                .strength(8, 1001)
                .sounds(ACSoundTypes.NUCLEAR_BOMB));
        this.setDefaultState(this.getDefaultState().with(UNSTABLE, false));
    }

    @Override
    public void detonate(World world, BlockPos pos, @org.jetbrains.annotations.Nullable LivingEntity igniter) {
        if (!world.isClient) {
            MiniNukeEntity primedtnt = new MiniNukeEntity(world, pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, igniter);
            world.spawnEntity(primedtnt);
            world.playSound(null, primedtnt.getX(), primedtnt.getY(), primedtnt.getZ(), SoundEvents.ENTITY_TNT_PRIMED, SoundCategory.BLOCKS, 1.0F, 1.0F);
            world.emitGameEvent(igniter, GameEvent.PRIME_FUSE, pos);
        }
    }

    @Override
    public PistonBehavior getPistonPushReaction(BlockState blockState) {
        return PistonBehavior.DESTROY;
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) { return SHAPE; }

    @Override
    public float getAmbientOcclusionLightLevel(BlockState state, BlockView world, BlockPos pos) {
        return 1.0F;
    }

    @Override
    public boolean isTransparent(BlockState state, BlockView world, BlockPos pos) {
        return true;
    }
}
