package net.hellomouse.alexscavesenriched.block;

import com.github.alexmodguy.alexscaves.client.particle.ACParticleRegistry;
import com.github.alexmodguy.alexscaves.server.block.ACSoundTypes;
import com.github.alexmodguy.alexscaves.server.block.fluid.ACFluidRegistry;
import com.github.alexmodguy.alexscaves.server.item.ACItemRegistry;
import com.github.alexmodguy.alexscaves.server.misc.ACMath;
import com.github.alexmodguy.alexscaves.server.misc.ACSoundRegistry;
import net.hellomouse.alexscavesenriched.ACEBlockEntityRegistry;
import net.hellomouse.alexscavesenriched.block.block_entity.EnrichedUraniumRodBlockEntity;
import net.hellomouse.alexscavesenriched.block.block_entity.RadiationEmitterBlockEntity;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.fluid.FlowableFluid;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import javax.annotation.Nullable;
import java.util.Optional;

abstract class BaseRotatedPillarEntityBlock extends BlockWithEntity {
    public static final EnumProperty<Direction.Axis> AXIS = Properties.AXIS;

    public BaseRotatedPillarEntityBlock(Settings properties) {
        super(properties);
        this.setDefaultState(this.getDefaultState().with(AXIS, Direction.Axis.Y));
    }

    public BlockState rotate(BlockState state, BlockRotation rotation) {
        return rotatePillar(state, rotation);
    }

    public static BlockState rotatePillar(BlockState state, BlockRotation rotation) {
        switch (rotation) {
            case COUNTERCLOCKWISE_90, CLOCKWISE_90 -> {
                return switch (state.get(AXIS)) {
                    case X -> state.with(AXIS, Direction.Axis.Z);
                    case Z -> state.with(AXIS, Direction.Axis.X);
                    default -> state;
                };
            }
            default -> {
                return state;
            }
        }
    }

    protected void appendProperties(StateManager.Builder<Block, BlockState> state) {
        state.add(AXIS);
    }

    public BlockState getPlacementState(ItemPlacementContext context) {
        return this.getDefaultState().with(AXIS, context.getSide().getAxis());
    }
}

public class EnrichedUraniumRodBlock extends BaseRotatedPillarEntityBlock implements Waterloggable {
    private static final VoxelShape SHAPE_X = ACMath.buildShape(Block.createCuboidShape(2.0, 6.0, 6.0, 14.0, 10.0, 10.0), Block.createCuboidShape(14.0, 5.0, 5.0, 16.0, 11.0, 11.0), Block.createCuboidShape(0.0, 5.0, 5.0, 2.0, 11.0, 11.0));
    private static final VoxelShape SHAPE_Y = ACMath.buildShape(Block.createCuboidShape(6.0, 2.0, 6.0, 10.0, 14.0, 10.0), Block.createCuboidShape(5.0, 0.0, 5.0, 11.0, 2.0, 11.0), Block.createCuboidShape(5.0, 14.0, 5.0, 11.0, 16.0, 11.0));
    private static final VoxelShape SHAPE_Z = ACMath.buildShape(Block.createCuboidShape(6.0, 6.0, 2.0, 10.0, 10.0, 14.0), Block.createCuboidShape(5.0, 5.0, 14.0, 11.0, 11.0, 16.0), Block.createCuboidShape(5.0, 5.0, 0.0, 11.0, 11.0, 2.0));
    public static final IntProperty LIQUID_LOGGED = IntProperty.of("liquid_logged", 0, 2);

    public EnrichedUraniumRodBlock() {
        super(Settings.create()
                .mapColor(MapColor.LIME)
                .strength(1.5F)
                .luminance((state) -> 15)
                .emissiveLighting((state, level, pos) -> true)
                .sounds(ACSoundTypes.URANIUM));
        this.setDefaultState((this.getDefaultState()
                .with(LIQUID_LOGGED, 0))
                .with(AXIS, Direction.Axis.Y));
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new EnrichedUraniumRodBlockEntity(pos, state);
    }

    @javax.annotation.Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World level, BlockState state, BlockEntityType<T> entityType) {
        return checkType(entityType, ACEBlockEntityRegistry.ENRICHED_URANIUM_ROD.get(), RadiationEmitterBlockEntity::tick);
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    public VoxelShape getOutlineShape(BlockState state, BlockView getter, BlockPos pos, ShapeContext context) {
        return switch (state.get(AXIS)) {
            case X -> SHAPE_X;
            case Y -> SHAPE_Y;
            case Z -> SHAPE_Z;
        };
    }

    public void randomDisplayTick(BlockState state, World level, BlockPos pos, Random randomSource) {
        if (randomSource.nextInt(80) == 0) {
            level.playSound((double) pos.getX() + 0.5, (double) pos.getY() + 0.5, (double) pos.getZ() + 0.5, (SoundEvent) ACSoundRegistry.URANIUM_HUM.get(), SoundCategory.BLOCKS, 0.5F, randomSource.nextFloat() * 0.4F + 0.8F, false);
        }

        if (randomSource.nextInt(10) == 0) {
            Vec3d center = Vec3d.ofCenter(pos, 0.5);
            level.addParticle(ACParticleRegistry.PROTON.get(), center.x, center.y, center.z, center.x, center.y, center.z);
        }

    }

    public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState state1, WorldAccess levelAccessor, BlockPos blockPos, BlockPos blockPos1) {
        int liquidType = (Integer) state.get(LIQUID_LOGGED);
        if (liquidType == 1) {
            levelAccessor.scheduleFluidTick(blockPos, Fluids.WATER, Fluids.WATER.getTickRate(levelAccessor));
        } else if (liquidType == 2) {
            levelAccessor.scheduleFluidTick(blockPos, ACFluidRegistry.ACID_FLUID_SOURCE.get(), ((FlowableFluid) ACFluidRegistry.ACID_FLUID_SOURCE.get()).getTickRate(levelAccessor));
        }

        if (!levelAccessor.isClient()) {
            levelAccessor.scheduleBlockTick(blockPos, this, 1);
        }

        return super.getStateForNeighborUpdate(state, direction, state1, levelAccessor, blockPos, blockPos1);
    }

    @Nullable
    public BlockState getPlacementState(ItemPlacementContext context) {
        WorldAccess levelaccessor = context.getWorld();
        BlockPos blockpos = context.getBlockPos();
        return super.getPlacementState(context).with(LIQUID_LOGGED, getLiquidType(levelaccessor.getFluidState(blockpos)));
    }

    public boolean canFillWithFluid(BlockView getter, BlockPos blockPos, BlockState blockState, Fluid fluid) {
        return fluid == Fluids.WATER || fluid.getFluidType() == ACFluidRegistry.ACID_FLUID_TYPE.get();
    }

    public boolean tryFillWithFluid(WorldAccess levelAccessor, BlockPos pos, BlockState blockState, FluidState fluidState) {
        int liquidType = blockState.get(LIQUID_LOGGED);
        if (liquidType == 0) {
            if (!levelAccessor.isClient()) {
                if (fluidState.getFluid() == Fluids.WATER) {
                    levelAccessor.setBlockState(pos, blockState.with(LIQUID_LOGGED, 1), 3);
                } else if (fluidState.getFluidType() == ACFluidRegistry.ACID_FLUID_TYPE.get()) {
                    levelAccessor.setBlockState(pos, blockState.with(LIQUID_LOGGED, 2), 3);
                }

                levelAccessor.scheduleFluidTick(pos, fluidState.getFluid(), fluidState.getFluid().getTickRate(levelAccessor));
            }

            return true;
        } else {
            return false;
        }
    }

    public ItemStack tryDrainFluid(WorldAccess levelAccessor, BlockPos blockPos, BlockState state) {
        int liquidType = state.get(LIQUID_LOGGED);
        if (liquidType > 0) {
            levelAccessor.setBlockState(blockPos, (BlockState) state.with(LIQUID_LOGGED, 0), 3);
            if (!state.canPlaceAt(levelAccessor, blockPos)) {
                levelAccessor.breakBlock(blockPos, true);
            }

            return new ItemStack((ItemConvertible) (liquidType == 1 ? Items.WATER_BUCKET : (ItemConvertible) ACItemRegistry.ACID_BUCKET.get()));
        } else {
            return ItemStack.EMPTY;
        }
    }

    public Optional<SoundEvent> getBucketFillSound() {
        return Fluids.WATER.getBucketFillSound();
    }

    public FluidState getFluidState(BlockState state) {
        int liquidType = state.get(LIQUID_LOGGED);
        return liquidType == 1 ? Fluids.WATER.getStill(false) : (liquidType == 2 ? (ACFluidRegistry.ACID_FLUID_SOURCE.get()).getStill(false) : super.getFluidState(state));
    }

    protected void appendProperties(StateManager.Builder<Block, BlockState> blockStateBuilder) {
        blockStateBuilder.add(LIQUID_LOGGED, AXIS);
    }

    public static int getLiquidType(FluidState fluidState) {
        if (fluidState.getFluid() == Fluids.WATER) {
            return 1;
        } else {
            return fluidState.getFluidType() == ACFluidRegistry.ACID_FLUID_TYPE.get() && fluidState.isStill() ? 2 : 0;
        }
    }
}
