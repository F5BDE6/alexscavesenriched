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
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import javax.annotation.Nullable;
import java.util.Optional;

abstract class BaseRotatedPillarEntityBlock extends BaseEntityBlock {
    public static final EnumProperty<Direction.Axis> AXIS = BlockStateProperties.AXIS;

    public BaseRotatedPillarEntityBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.defaultBlockState().setValue(AXIS, Direction.Axis.Y));
    }

    public static BlockState rotatePillar(BlockState state, Rotation rotation) {
        switch (rotation) {
            case COUNTERCLOCKWISE_90, CLOCKWISE_90 -> {
                return switch (state.getValue(AXIS)) {
                    case X -> state.setValue(AXIS, Direction.Axis.Z);
                    case Z -> state.setValue(AXIS, Direction.Axis.X);
                    default -> state;
                };
            }
            default -> {
                return state;
            }
        }
    }

    public BlockState rotate(BlockState state, Rotation rotation) {
        return rotatePillar(state, rotation);
    }

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> state) {
        state.add(AXIS);
    }

    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(AXIS, context.getClickedFace().getAxis());
    }
}

public class EnrichedUraniumRodBlock extends BaseRotatedPillarEntityBlock implements SimpleWaterloggedBlock {
    public static final IntegerProperty LIQUID_LOGGED = IntegerProperty.create("liquid_logged", 0, 2);
    private static final VoxelShape SHAPE_X = ACMath.buildShape(Block.box(2.0, 6.0, 6.0, 14.0, 10.0, 10.0), Block.box(14.0, 5.0, 5.0, 16.0, 11.0, 11.0), Block.box(0.0, 5.0, 5.0, 2.0, 11.0, 11.0));
    private static final VoxelShape SHAPE_Y = ACMath.buildShape(Block.box(6.0, 2.0, 6.0, 10.0, 14.0, 10.0), Block.box(5.0, 0.0, 5.0, 11.0, 2.0, 11.0), Block.box(5.0, 14.0, 5.0, 11.0, 16.0, 11.0));
    private static final VoxelShape SHAPE_Z = ACMath.buildShape(Block.box(6.0, 6.0, 2.0, 10.0, 10.0, 14.0), Block.box(5.0, 5.0, 14.0, 11.0, 11.0, 16.0), Block.box(5.0, 5.0, 0.0, 11.0, 11.0, 2.0));

    public EnrichedUraniumRodBlock() {
        super(Properties.of()
                .mapColor(MapColor.COLOR_LIGHT_GREEN)
                .strength(1.5F)
                .lightLevel((state) -> 15)
                .emissiveRendering((state, level, pos) -> true)
                .sound(ACSoundTypes.URANIUM));
        this.registerDefaultState((this.defaultBlockState()
                .setValue(LIQUID_LOGGED, 0))
                .setValue(AXIS, Direction.Axis.Y));
    }

    public static int getLiquidType(FluidState fluidState) {
        if (fluidState.getType() == Fluids.WATER) {
            return 1;
        } else {
            return fluidState.getFluidType() == ACFluidRegistry.ACID_FLUID_TYPE.get() && fluidState.isSource() ? 2 : 0;
        }
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new EnrichedUraniumRodBlockEntity(pos, state);
    }

    @javax.annotation.Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> entityType) {
        return createTickerHelper(entityType, ACEBlockEntityRegistry.ENRICHED_URANIUM_ROD.get(), RadiationEmitterBlockEntity::tick);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    public VoxelShape getShape(BlockState state, BlockGetter getter, BlockPos pos, CollisionContext context) {
        return switch (state.getValue(AXIS)) {
            case X -> SHAPE_X;
            case Y -> SHAPE_Y;
            case Z -> SHAPE_Z;
        };
    }

    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource randomSource) {
        if (randomSource.nextInt(80) == 0) {
            level.playLocalSound((double) pos.getX() + 0.5, (double) pos.getY() + 0.5, (double) pos.getZ() + 0.5, (SoundEvent) ACSoundRegistry.URANIUM_HUM.get(), SoundSource.BLOCKS, 0.5F, randomSource.nextFloat() * 0.4F + 0.8F, false);
        }

        if (randomSource.nextInt(10) == 0) {
            Vec3 center = Vec3.upFromBottomCenterOf(pos, 0.5);
            level.addParticle(ACParticleRegistry.PROTON.get(), center.x, center.y, center.z, center.x, center.y, center.z);
        }

    }

    public BlockState updateShape(BlockState state, Direction direction, BlockState state1, LevelAccessor levelAccessor, BlockPos blockPos, BlockPos blockPos1) {
        int liquidType = (Integer) state.getValue(LIQUID_LOGGED);
        if (liquidType == 1) {
            levelAccessor.scheduleTick(blockPos, Fluids.WATER, Fluids.WATER.getTickDelay(levelAccessor));
        } else if (liquidType == 2) {
            levelAccessor.scheduleTick(blockPos, ACFluidRegistry.ACID_FLUID_SOURCE.get(), ((FlowingFluid) ACFluidRegistry.ACID_FLUID_SOURCE.get()).getTickDelay(levelAccessor));
        }

        if (!levelAccessor.isClientSide()) {
            levelAccessor.scheduleTick(blockPos, this, 1);
        }

        return super.updateShape(state, direction, state1, levelAccessor, blockPos, blockPos1);
    }

    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        LevelAccessor levelaccessor = context.getLevel();
        BlockPos blockpos = context.getClickedPos();
        return super.getStateForPlacement(context).setValue(LIQUID_LOGGED, getLiquidType(levelaccessor.getFluidState(blockpos)));
    }

    public boolean canPlaceLiquid(BlockGetter getter, BlockPos blockPos, BlockState blockState, Fluid fluid) {
        return fluid == Fluids.WATER || fluid.getFluidType() == ACFluidRegistry.ACID_FLUID_TYPE.get();
    }

    public boolean placeLiquid(LevelAccessor levelAccessor, BlockPos pos, BlockState blockState, FluidState fluidState) {
        int liquidType = blockState.getValue(LIQUID_LOGGED);
        if (liquidType == 0) {
            if (!levelAccessor.isClientSide()) {
                if (fluidState.getType() == Fluids.WATER) {
                    levelAccessor.setBlock(pos, blockState.setValue(LIQUID_LOGGED, 1), 3);
                } else if (fluidState.getFluidType() == ACFluidRegistry.ACID_FLUID_TYPE.get()) {
                    levelAccessor.setBlock(pos, blockState.setValue(LIQUID_LOGGED, 2), 3);
                }

                levelAccessor.scheduleTick(pos, fluidState.getType(), fluidState.getType().getTickDelay(levelAccessor));
            }

            return true;
        } else {
            return false;
        }
    }

    public ItemStack pickupBlock(LevelAccessor levelAccessor, BlockPos blockPos, BlockState state) {
        int liquidType = state.getValue(LIQUID_LOGGED);
        if (liquidType > 0) {
            levelAccessor.setBlock(blockPos, (BlockState) state.setValue(LIQUID_LOGGED, 0), 3);
            if (!state.canSurvive(levelAccessor, blockPos)) {
                levelAccessor.destroyBlock(blockPos, true);
            }

            return new ItemStack((ItemLike) (liquidType == 1 ? Items.WATER_BUCKET : (ItemLike) ACItemRegistry.ACID_BUCKET.get()));
        } else {
            return ItemStack.EMPTY;
        }
    }

    public Optional<SoundEvent> getPickupSound() {
        return Fluids.WATER.getPickupSound();
    }

    public FluidState getFluidState(BlockState state) {
        int liquidType = state.getValue(LIQUID_LOGGED);
        return liquidType == 1 ? Fluids.WATER.getSource(false) : (liquidType == 2 ? (ACFluidRegistry.ACID_FLUID_SOURCE.get()).getSource(false) : super.getFluidState(state));
    }

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> blockStateBuilder) {
        blockStateBuilder.add(LIQUID_LOGGED, AXIS);
    }
}
