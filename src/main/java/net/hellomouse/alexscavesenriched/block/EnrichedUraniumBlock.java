package net.hellomouse.alexscavesenriched.block;


import com.github.alexmodguy.alexscaves.client.particle.ACParticleRegistry;
import com.github.alexmodguy.alexscaves.server.block.ACSoundTypes;
import net.hellomouse.alexscavesenriched.ACEBlockEntityRegistry;
import net.hellomouse.alexscavesenriched.AlexsCavesEnriched;
import net.hellomouse.alexscavesenriched.block.block_entity.EnrichedUraniumBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Tuple;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Objects;
import java.util.Stack;

public class EnrichedUraniumBlock extends BaseEntityBlock {
    private static final Tuple<Vec3, Double> BAD_RET = new Tuple<>(new Vec3(0.0, 0.0, 0.0), 0.0);

    public EnrichedUraniumBlock() {
        super(Properties.of()
                .emissiveRendering((state, level, pos) -> true)
                .mapColor(MapColor.COLOR_GREEN)
                .explosionResistance(30.0f)
                .requiresCorrectToolForDrops()
                .strength(6.0F)
                .lightLevel(_blockState -> 3)
                .sound(ACSoundTypes.URANIUM));
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new EnrichedUraniumBlockEntity(pos, state);
    }

    @javax.annotation.Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> entityType) {
        return createTickerHelper(entityType, ACEBlockEntityRegistry.ENRICHED_URANIUM.get(), EnrichedUraniumBlockEntity::tick);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    public PushReaction getPistonPushReaction(BlockState blockState) {
        return PushReaction.BLOCK;
    }

    public Tuple<Boolean, Tuple<Vec3, Double>> collectSurroundedByReflector(LevelReader level, BlockPos currentPos, HashSet<BlockPos> visited) {
        Stack<BlockPos> toVisit = new Stack<>();
        toVisit.add(currentPos);
        int[][] offsets = {
                {-1, 0, 0},
                {1, 0, 0},
                {0, -1, 0},
                {0, 1, 0},
                {0, 0, -1},
                {0, 0, 1}
        };
        BlockPos corner1 = currentPos;
        BlockPos corner2 = currentPos;

        while (!toVisit.isEmpty()) {
            BlockPos top = toVisit.pop();
            if (visited.contains(top))
                continue;
            if (!(level.getBlockState(top).getBlock() instanceof EnrichedUraniumBlock))
                continue;
            visited.add(top);

            if (visited.size() > AlexsCavesEnriched.CONFIG.demonCore.maxSize)
                return new Tuple<>(false, BAD_RET);

            corner2 = new BlockPos(
                    Math.min(top.getX(), corner2.getX()),
                    Math.min(top.getY(), corner2.getY()),
                    Math.min(top.getZ(), corner2.getZ())
            );
            corner1 = new BlockPos(
                    Math.max(top.getX(), corner1.getX()),
                    Math.max(top.getY(), corner1.getY()),
                    Math.max(top.getZ(), corner1.getZ())
            );

            for (int[] offset : offsets) {
                var newPos = top.offset(offset[0], offset[1], offset[2]);
                var state = level.getBlockState(newPos);
                if (state.is(AlexsCavesEnriched.NEUTRONREFLECTOR_TAG)) {
                    // no-op
                }
                else if (state.getBlock() instanceof EnrichedUraniumBlock)
                    toVisit.add(newPos);
                else
                    return new Tuple<>(false, BAD_RET);
            }
        }

        // Not enough blocks filled
        var vol = (corner1.getX() - corner2.getX() + 1) * (corner1.getY() - corner2.getY() + 1) *(corner1.getZ() - corner2.getZ() + 1);
        if (vol * AlexsCavesEnriched.CONFIG.demonCore.boundingBoxFillProportion > visited.size())
            return new Tuple<>(false, BAD_RET);

        double radius = Math.sqrt(corner1.distSqr(corner2)) / 2.0F;
        return new Tuple<>(true, new Tuple<>(new Vec3(
                corner1.getX() - (corner1.getX() - corner2.getX()) / 2.0F + 0.5F,
                corner1.getY() - (corner1.getY() - corner2.getY()) / 2.0F + 0.5F,
                corner1.getZ() - (corner1.getZ() - corner2.getZ()) / 2.0F + 0.5F),
                radius)
        );
    }

    @Override
    public void neighborChanged(BlockState p_60509_, Level level, BlockPos p_60511_, Block p_60512_, BlockPos p_60513_, boolean p_60514_) {
        super.neighborChanged(p_60509_, level, p_60511_, p_60512_, p_60513_, p_60514_);
        if (!((EnrichedUraniumBlockEntity)(Objects.requireNonNull(level.getBlockEntity(p_60511_)))).isChecked()) {
            var visited = new HashSet<BlockPos>();
            var pair = collectSurroundedByReflector(level, p_60511_, visited);
            boolean valid = pair.getA();
            var right = pair.getB();
            Vec3 center = right.getA();
            double radius = right.getB();
            boolean first = false;

            for (var pos : visited) {
                var blockEntity = (EnrichedUraniumBlockEntity) level.getBlockEntity(pos);
                assert blockEntity != null;
                if (!first) {
                    first = true;
                    blockEntity.setFirst(valid);
                    blockEntity.setCenter(center, (float)radius);
                } else
                    blockEntity.setFirst(false);

                blockEntity.setGlowing(valid);
                blockEntity.setNumBlocks(visited.size());
                blockEntity.setChanged();
                level.sendBlockUpdated(pos, blockEntity.getBlockState(), blockEntity.getBlockState(), 2);
            }
        }
    }

    public void animateTick(BlockState blockState, Level level, BlockPos blockPos, RandomSource randomSource) {
        if (randomSource.nextInt(2) == 0) {
            Direction direction = Direction.getRandom(randomSource);
            BlockPos blockpos = blockPos.relative(direction);
            BlockState blockstate = level.getBlockState(blockpos);
            if (!blockState.canOcclude() || !blockstate.isFaceSturdy(level, blockpos, direction.getOpposite())) {
                double d0 = direction.getStepX() == 0 ? randomSource.nextDouble() : 0.5D + (double) direction.getStepX() * 0.6D;
                double d1 = direction.getStepY() == 0 ? randomSource.nextDouble() : 0.5D + (double) direction.getStepY() * 0.6D;
                double d2 = direction.getStepZ() == 0 ? randomSource.nextDouble() : 0.5D + (double) direction.getStepZ() * 0.6D;
                level.addParticle(randomSource.nextBoolean() ? ACParticleRegistry.GAMMAROACH.get() : ACParticleRegistry.HAZMAT_BREATHE.get(), (double) blockPos.getX() + d0, (double) blockPos.getY() + d1, (double) blockPos.getZ() + d2, 0.0D, 0.1D + level.random.nextFloat() * 0.1F, 0.0D);
            }
        }
    }
}
