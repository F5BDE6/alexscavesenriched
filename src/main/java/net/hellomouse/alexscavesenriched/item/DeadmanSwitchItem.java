package net.hellomouse.alexscavesenriched.item;

import com.github.alexmodguy.alexscaves.server.misc.ACAdvancementTriggerRegistry;
import com.github.alexmodguy.alexscaves.server.misc.ACTagRegistry;
import net.hellomouse.alexscavesenriched.ACEItemRegistry;
import net.hellomouse.alexscavesenriched.AlexsCavesEnriched;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtOps;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.world.World;
import net.minecraftforge.common.world.ForgeChunkManager;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class DeadmanSwitchItem extends Item {
    public DeadmanSwitchItem() {
        super(new Settings().maxCount(1));
    }

    public static boolean isActive(ItemStack itemStack) {
        NbtCompound compoundtag = itemStack.getNbt();
        return compoundtag != null && (compoundtag.contains("BombDimension") || compoundtag.contains("BombPos"));
    }

    private static Optional<RegistryKey<World>> getBombDimension(NbtCompound tag) {
        return World.CODEC.parse(NbtOps.INSTANCE, tag.get("BombDimension")).result();
    }

    @Nullable
    public static GlobalPos getBombPosition(NbtCompound tag) {
        boolean flag = tag.contains("BombPos");
        boolean flag1 = tag.contains("BombDimension");
        if (flag && flag1) {
            Optional<RegistryKey<World>> optional = getBombDimension(tag);
            if (optional.isPresent()) {
                BlockPos blockpos = NbtHelper.toBlockPos(tag.getCompound("BombPos"));
                return GlobalPos.create(optional.get(), blockpos);
            }
        }
        return null;
    }

    public static void detonate(World level, PlayerEntity player, ItemStack itemstack) {
        if (isActive(itemstack)) {
            NbtCompound tag = itemstack.getOrCreateNbt();
            GlobalPos globalPos = getBombPosition(tag);
            if (globalPos != null && globalPos.getDimension() != null && !level.isClient && level instanceof ServerWorld serverLevel) {
                ServerWorld dimensionLevel = serverLevel.getServer().getWorld(globalPos.getDimension());
                if (dimensionLevel != null) {
                    loadChunksAround(dimensionLevel, player.getUuid(), globalPos.getPos(), true);
                    BlockState blockState = dimensionLevel.getBlockState(globalPos.getPos());
                    if (blockState.isIn(ACTagRegistry.REMOTE_DETONATOR_ACTIVATES)) {
                        blockState.onCaughtFire(dimensionLevel, globalPos.getPos(), Direction.UP, player);
                        if (player.squaredDistanceTo(globalPos.getPos().toCenterPos()) > 5000.0)
                            ACAdvancementTriggerRegistry.REMOTE_DETONATION.triggerForEntity(player);

                        tag.remove("BombDimension");
                        tag.remove("BombPos");
                        itemstack.setNbt(tag);
                        level.setBlockState(globalPos.getPos(), Blocks.AIR.getDefaultState());
                    }
                }
            }
        }
    }

    // Use to disarm
    @Override
    public TypedActionResult<ItemStack> use(World level, PlayerEntity player, Hand hand) {
        ItemStack itemstack = player.getStackInHand(hand);
        if (isActive(itemstack)) {
            level.playSound(null, player.getBlockPos(), SoundEvents.BLOCK_LEVER_CLICK, SoundCategory.PLAYERS, 1.0F, 1.0F);
            NbtCompound tag = itemstack.getOrCreateNbt();
            tag.remove("BombDimension");
            tag.remove("BombPos");
            itemstack.setNbt(tag);
            return TypedActionResult.success(itemstack);
        } else {
            return TypedActionResult.pass(itemstack);
        }
    }

    private static void loadChunksAround(ServerWorld serverLevel, UUID ticket, BlockPos center, boolean load) {
        ChunkPos chunkPos = new ChunkPos(center);
        for (int i = -1; i <= 1; ++i)
            for (int j = -1; j <= 1; ++j)
                ForgeChunkManager.forceChunk(serverLevel, AlexsCavesEnriched.MODID, ticket, chunkPos.x + i, chunkPos.z + j, load, true);
    }

    @Override
    public void inventoryTick(ItemStack itemStack, World level, Entity entity, int i, boolean b) {
        if (!level.isClient && isActive(itemStack)) {
            NbtCompound compoundtag = itemStack.getOrCreateNbt();
            if (compoundtag.contains("BombTracked") && !compoundtag.getBoolean("BombTracked")) {
                return;
            }

            Optional<RegistryKey<World>> optional = getBombDimension(compoundtag);
            if (optional.isPresent() && optional.get() == level.getRegistryKey() && compoundtag.contains("BombPos")) {
                BlockPos blockpos = NbtHelper.toBlockPos(compoundtag.getCompound("BombPos"));
                boolean flag = (entity.age + entity.getId()) % 20 == 0 && level.canSetBlock(blockpos) && !level.getBlockState(blockpos).isIn(ACTagRegistry.REMOTE_DETONATOR_ACTIVATES);

                if (!level.isInBuildLimit(blockpos) || flag) {
                    compoundtag.remove("BombPos");
                    compoundtag.remove("BombDimension");
                }
            }
        }
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        BlockPos blockpos = context.getBlockPos();
        World level = context.getWorld();
        if (!level.getBlockState(blockpos).isIn(ACTagRegistry.REMOTE_DETONATOR_ACTIVATES)) {
            return super.useOnBlock(context);
        } else {
            level.playSound(null, blockpos, SoundEvents.ITEM_LODESTONE_COMPASS_LOCK, SoundCategory.PLAYERS, 1.0F, 1.0F);
            PlayerEntity player = context.getPlayer();
            ItemStack itemstack = context.getStack();
            boolean flag = !player.getAbilities().creativeMode && itemstack.getCount() == 1;
            if (flag) {
                this.addBombTags(level.getRegistryKey(), blockpos, itemstack.getOrCreateNbt());
            } else {
                ItemStack itemstack1 = new ItemStack(ACEItemRegistry.DEADMAN_SWITCH.get(), 1);
                NbtCompound compoundtag = itemstack.hasNbt() ? itemstack.getNbt().copy() : new NbtCompound();
                itemstack1.setNbt(compoundtag);
                itemstack.decrement(1);
                this.addBombTags(level.getRegistryKey(), blockpos, compoundtag);
                if (!player.getInventory().insertStack(itemstack1))
                    player.dropItem(itemstack1, false);
            }
            return ActionResult.success(level.isClient);
        }
    }

    private void addBombTags(RegistryKey<World> levelResourceKey, BlockPos blockPos, NbtCompound tag) {
        tag.put("BombPos", NbtHelper.fromBlockPos(blockPos));
        World.CODEC.encodeStart(NbtOps.INSTANCE, levelResourceKey).result().ifPresent((p_40731_) -> {
            tag.put("BombDimension", p_40731_);
        });
        tag.putBoolean("BombTracked", true);
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World worldIn, List<Text> tooltip, TooltipContext flagIn) {
        if (stack.getNbt() != null && stack.getNbt().contains("BombPos")) {
            Optional<RegistryKey<World>> optional = getBombDimension(stack.getNbt());
            BlockPos blockpos = NbtHelper.toBlockPos(stack.getNbt().getCompound("BombPos"));
            if (optional.isPresent()) {
                Text untranslated = Text.translatable("item.alexscaves.remote_detonator.desc", blockpos.getX(), blockpos.getY(), blockpos.getZ()).formatted(Formatting.GRAY);
                tooltip.add(untranslated);
            }
        }
        super.appendTooltip(stack, worldIn, tooltip, flagIn);
    }
}
