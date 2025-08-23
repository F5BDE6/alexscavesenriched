package net.hellomouse.alexscavesenriched.block;

import com.github.alexmodguy.alexscaves.server.block.ACSoundTypes;
import net.hellomouse.alexscavesenriched.block.abs.AbstractTntBlock;
import net.hellomouse.alexscavesenriched.entity.MiniNukeEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class MiniNukeBlock extends AbstractTntBlock {
    protected static final VoxelShape SHAPE = Block.box(3.0, 0.0, 3.0, 10.0, 10.0, 10.0);

    public MiniNukeBlock() {
        super(Properties.of()
                .mapColor(MapColor.METAL)
                .requiresCorrectToolForDrops()
                .noOcclusion()
                .strength(8, 1001)
                .sound(ACSoundTypes.NUCLEAR_BOMB));
        this.registerDefaultState(this.defaultBlockState().setValue(UNSTABLE, false));
    }

    public static void detonateStatic(Level world, BlockPos pos, @org.jetbrains.annotations.Nullable LivingEntity igniter) {
        if (!world.isClientSide) {
            MiniNukeEntity primedtnt = new MiniNukeEntity(world, pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, igniter);
            world.addFreshEntity(primedtnt);
            world.playSound(null, primedtnt.getX(), primedtnt.getY(), primedtnt.getZ(), SoundEvents.TNT_PRIMED, SoundSource.BLOCKS, 1.0F, 1.0F);
            world.gameEvent(igniter, GameEvent.PRIME_FUSE, pos);
        }
    }

    @Override
    public void detonate(Level world, BlockPos pos, @org.jetbrains.annotations.Nullable LivingEntity igniter) {
        detonateStatic(world, pos, igniter);
    }

    @Override
    public PushReaction getPistonPushReaction(BlockState blockState) {
        return PushReaction.DESTROY;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public float getShadeBrightness(BlockState state, BlockGetter world, BlockPos pos) {
        return 1.0F;
    }

    @Override
    public boolean propagatesSkylightDown(BlockState state, BlockGetter world, BlockPos pos) {
        return true;
    }
}
