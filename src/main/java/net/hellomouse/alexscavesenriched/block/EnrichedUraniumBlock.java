package net.hellomouse.alexscavesenriched.block;


import com.github.alexmodguy.alexscaves.client.particle.ACParticleRegistry;
import com.github.alexmodguy.alexscaves.server.block.ACSoundTypes;
import net.hellomouse.alexscavesenriched.ACEBlockEntityRegistry;
import net.hellomouse.alexscavesenriched.AlexsCavesEnriched;
import net.hellomouse.alexscavesenriched.block.block_entity.EnrichedUraniumBlockEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.MapColor;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;


import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Objects;
import java.util.Stack;

public class EnrichedUraniumBlock extends BlockWithEntity {
    public EnrichedUraniumBlock() {
        super(Settings.create()
                .emissiveLighting((state, level, pos) -> true)
                .mapColor(MapColor.GREEN)
                .resistance(30.0f)
                .requiresTool()
                .strength(6.0F)
                .luminance(_blockState -> 3)
                .sounds(ACSoundTypes.URANIUM));
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new EnrichedUraniumBlockEntity(pos, state);
    }

    @javax.annotation.Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World level, BlockState state, BlockEntityType<T> entityType) {
        return checkType(entityType, ACEBlockEntityRegistry.ENRICHED_URANIUM.get(), EnrichedUraniumBlockEntity::tick);
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    public PistonBehavior getPistonPushReaction(BlockState blockState) {
        return PistonBehavior.BLOCK;
    }

    private static final Pair<Vec3d, Double> BAD_RET = new Pair<>(new Vec3d(0.0, 0.0, 0.0), 0.0);

    public Pair<Boolean, Pair<Vec3d, Double>> collectSurroundedByReflector(WorldView level, BlockPos currentPos, HashSet<BlockPos> visited) {
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
                return new Pair<>(false, BAD_RET);

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
                var newPos = top.add(offset[0], offset[1], offset[2]);
                var state = level.getBlockState(newPos);
                if (state.isIn(AlexsCavesEnriched.NEUTRONREFLECTOR_TAG)) {
                    // no-op
                }
                else if (state.getBlock() instanceof EnrichedUraniumBlock)
                    toVisit.add(newPos);
                else
                    return new Pair<>(false, BAD_RET);
            }
        }

        // Not enough blocks filled
        var vol = (corner1.getX() - corner2.getX() + 1) * (corner1.getY() - corner2.getY() + 1) *(corner1.getZ() - corner2.getZ() + 1);
        if (vol * AlexsCavesEnriched.CONFIG.demonCore.boundingBoxFillProportion > visited.size())
            return new Pair<>(false, BAD_RET);

        double radius = Math.sqrt(corner1.getSquaredDistance(corner2)) / 2.0F;
        return new Pair<>(true, new Pair<>(new Vec3d(
                corner1.getX() - (corner1.getX() - corner2.getX()) / 2.0F + 0.5F,
                corner1.getY() - (corner1.getY() - corner2.getY()) / 2.0F + 0.5F,
                corner1.getZ() - (corner1.getZ() - corner2.getZ()) / 2.0F + 0.5F),
                radius)
        );
    }

    @Override
    public void neighborUpdate(BlockState p_60509_, World level, BlockPos p_60511_, Block p_60512_, BlockPos p_60513_, boolean p_60514_) {
        super.neighborUpdate(p_60509_, level, p_60511_, p_60512_, p_60513_, p_60514_);
        if (!((EnrichedUraniumBlockEntity)(Objects.requireNonNull(level.getBlockEntity(p_60511_)))).isChecked()) {
            var visited = new HashSet<BlockPos>();
            var pair = collectSurroundedByReflector(level, p_60511_, visited);
            boolean valid = pair.getLeft();
            var right = pair.getRight();
            Vec3d center = right.getLeft();
            double radius = right.getRight();
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
                blockEntity.markDirty();
                level.updateListeners(pos, blockEntity.getCachedState(), blockEntity.getCachedState(), 2);
            }
        }
    }

    public void randomDisplayTick(BlockState blockState, World level, BlockPos blockPos, Random randomSource) {
        if (randomSource.nextInt(2) == 0) {
            Direction direction = Direction.random(randomSource);
            BlockPos blockpos = blockPos.offset(direction);
            BlockState blockstate = level.getBlockState(blockpos);
            if (!blockState.isOpaque() || !blockstate.isSideSolidFullSquare(level, blockpos, direction.getOpposite())) {
                double d0 = direction.getOffsetX() == 0 ? randomSource.nextDouble() : 0.5D + (double) direction.getOffsetX() * 0.6D;
                double d1 = direction.getOffsetY() == 0 ? randomSource.nextDouble() : 0.5D + (double) direction.getOffsetY() * 0.6D;
                double d2 = direction.getOffsetZ() == 0 ? randomSource.nextDouble() : 0.5D + (double) direction.getOffsetZ() * 0.6D;
                level.addParticle(randomSource.nextBoolean() ? ACParticleRegistry.GAMMAROACH.get() : ACParticleRegistry.HAZMAT_BREATHE.get(), (double) blockPos.getX() + d0, (double) blockPos.getY() + d1, (double) blockPos.getZ() + d2, 0.0D, 0.1D + level.random.nextFloat() * 0.1F, 0.0D);
            }
        }
    }
}
