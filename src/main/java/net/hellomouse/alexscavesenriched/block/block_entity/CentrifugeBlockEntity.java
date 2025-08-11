package net.hellomouse.alexscavesenriched.block.block_entity;

import com.github.alexmodguy.alexscaves.AlexsCaves;
import net.hellomouse.alexscavesenriched.ACEBlockEntityRegistry;
import net.hellomouse.alexscavesenriched.ACERecipeRegistry;
import net.hellomouse.alexscavesenriched.ACESounds;
import net.hellomouse.alexscavesenriched.AlexsCavesEnriched;
import net.hellomouse.alexscavesenriched.block.centrifuge.CentrifugeMultiBlockBaseBlock;
import net.hellomouse.alexscavesenriched.block.centrifuge.CentrifugeUtil;
import net.hellomouse.alexscavesenriched.client.sound.CentrifugeSound;
import net.hellomouse.alexscavesenriched.inventory.CentrifugeBlockMenu;
import net.hellomouse.alexscavesenriched.recipe.CentrifugeRecipe;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.LockableContainerBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.SidedInvWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Objects;

public class CentrifugeBlockEntity extends LockableContainerBlockEntity implements SidedInventory {
    public static final int N_INPUT_SLOTS = 9;
    public static final int N_OUTPUT_SLOTS = 9;
    public static final int N_SLOTS = N_INPUT_SLOTS + N_OUTPUT_SLOTS;
    public static final int MAX_SPIN_SPEED = AlexsCavesEnriched.CONFIG.centrifuge.maxSpeed;
    public static final float ANGLE_PER_TICK = 50F;

    protected DefaultedList<ItemStack> items;
    protected final PropertyDelegate dataAccess;

    public int age;
    private int spinSpeed = 0;
    private float rotation = 0;
    private boolean spawnedSound = false;

    private static final int[] SLOTS_FOR_TOP = new int[]{0,1,2,3,4,5,6,7,8};
    private static final int[] SLOTS_FOR_DOWN = new int[]{9,10,11,12,13,14,15,16,17};
    private final LazyOptional<? extends IItemHandler>[] handlers;

    public CentrifugeBlockEntity(BlockPos pos, BlockState state) {
        super(ACEBlockEntityRegistry.CENTRIFUGE.get(), pos, state);
        this.items = DefaultedList.ofSize(N_SLOTS, ItemStack.EMPTY);

        this.dataAccess = new PropertyDelegate() {
            public int get(int type) {
                return switch (type) {
                    case 0 -> CentrifugeBlockEntity.this.spinSpeed;
                    default -> 0;
                };
            }

            public void set(int type, int value) {
                switch (type) {
                    case 0 -> CentrifugeBlockEntity.this.spinSpeed = value;
                    default -> {}
                }
            }

            public int size() {
                return 1;
            }
        };
        this.handlers = SidedInvWrapper.create(this, Direction.DOWN, Direction.UP, Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST);
    }

    public static void tick(World level, BlockPos blockPos, BlockState state, CentrifugeBlockEntity entity) {
        entity.age++;
        entity.rotation += ((float)entity.spinSpeed / MAX_SPIN_SPEED) * ANGLE_PER_TICK;
        while (entity.rotation > 360)
            entity.rotation -= 360;

        if (level.isClient) {
            entity.clientTick();
            return;
        }

        boolean requireResync = false;
        boolean invChanged = false;
        var prevSpinSpeed = entity.spinSpeed;
        entity.spinSpeed += entity.getCachedState().get(CentrifugeMultiBlockBaseBlock.POWERED) ? 1 : -1;
        entity.spinSpeed = Math.max(0, Math.min(MAX_SPIN_SPEED, entity.spinSpeed));
        if (prevSpinSpeed != entity.spinSpeed)
            requireResync = true;

        if (entity.spinSpeed > 0) {
            if (AlexsCavesEnriched.CONFIG.centrifuge.cantInteractWithActive && entity.age % 10 == 0)
                entity.closeAllOpenScreens();

            for (int i = 0; i < N_INPUT_SLOTS; i++) {
                ItemStack inStack = entity.items.get(i);
                if (inStack.isEmpty()) continue;

                var recipes = level.getRecipeManager().listAllOfType(ACERecipeRegistry.CENTRIFUGE_TYPE.get());
                for (CentrifugeRecipe recipe : recipes) {
                    float chance = recipe.getChance() * ((float)entity.spinSpeed) / MAX_SPIN_SPEED;
                    if (!recipe.matches(inStack) || (level.getRandom().nextFloat() > chance))
                        continue;

                    inStack.decrement(1);
                    requireResync = true;
                    invChanged = true;

                    for (var outputItem : recipe.getOutputs()) {
                        boolean canFitInside = false;
                        ItemStack outStack = new ItemStack(outputItem, 1);

                        for (int j = N_INPUT_SLOTS; j < N_SLOTS; j++) {
                            if (!entity.canFitInResultSlot(outStack, j))
                                continue;
                            entity.addOutStack(outStack, j);
                            canFitInside = true;
                            break;
                        }
                        if (!canFitInside) {
                            Vec3d spawnPos = Vec3d.ofCenter(entity.getPos());
                            ItemEntity itemEntity = new ItemEntity(level, spawnPos.x, spawnPos.y, spawnPos.z, outStack);
                            itemEntity.setVelocity(
                                (level.random.nextDouble() - 0.5) * 0.25,
                                (level.random.nextDouble() - 0.5) * 0.25,
                                (level.random.nextDouble() - 0.5) * 0.25
                            );
                            level.spawnEntity(itemEntity);
                        }
                    }
                    break; // End recipe
                }
            }
        }
        if (invChanged) {
            for (int dy = 0; dy < CentrifugeUtil.CENTRIFUGE_HEIGHT; dy++) {
                var pos = entity.getPos().offset(Direction.UP, dy);
                level.updateComparators(pos, level.getBlockState(pos).getBlock());
            }
        }
        if (requireResync)
            entity.syncWithClient();
    }

    @OnlyIn(Dist.CLIENT)
    public void clientTick() {
        if (!spawnedSound) {
            spawnedSound = true;
            var sound = new CentrifugeSound(this, ACESounds.CENTRIFUGE, SoundCategory.AMBIENT);
            MinecraftClient.getInstance().getSoundManager().playNextTick(sound);
        }
    }

    public float getRotation() { return rotation; }

    public int getSpinSpeed() { return spinSpeed; }

    private int getComparatorPower(int startSlot, int endSlot) { // End slot not inclusive
        if (items.size() == 0)
            return 0;
        int filledSlots = 0;
        float fullness = 0F;

        for (int i = startSlot; i < endSlot; i++) { // Only output slots
            var stack = items.get(i);
            if (!stack.isEmpty()) {
                filledSlots++;
                fullness += (float)stack.getCount() / stack.getMaxCount();
            }
        }
        float scale = fullness / (endSlot - startSlot);
        return (int)Math.ceil(scale * 14.0F) + (filledSlots > 0 ? 1 : 0);
    }

    public int getComparatorPowerInput() {
        return getComparatorPower(0, N_INPUT_SLOTS);
    }

    public int getComparatorPowerOutput() {
        return getComparatorPower(N_INPUT_SLOTS, N_SLOTS);
    }

    public void closeAllOpenScreens() {
        World level = getWorld();
        assert level != null;
        if (level.isClient)
            return;
        ServerWorld serverLevel = (ServerWorld)level;

        for (ServerPlayerEntity player : serverLevel.getPlayers(playerEntity -> {
            ScreenHandler menu = playerEntity.currentScreenHandler;
            if (menu instanceof CentrifugeBlockMenu cMenu)
                return ((CentrifugeBlockEntity)cMenu.getInventory()).getPos().equals(getPos());
            return false;
        })) {
            player.closeHandledScreen();
        }
    }

    @Override
    public void markRemoved() {
        AlexsCaves.PROXY.clearSoundCacheFor(this);
        super.markRemoved();
    }

    private void syncWithClient() {
        assert this.world != null;
        this.world.updateListeners(this.getPos(), this.getCachedState(), this.getCachedState(), 2);
    }

    private void addOutStack(ItemStack outStack, int out) {
        if (ItemStack.areItemsEqual(this.items.get(out), outStack))
            this.items.get(out).increment(outStack.getCount());
        else
            this.setStack(out, outStack.copy());
    }

    private boolean canFitInResultSlot(ItemStack putIn, int resultSlot) {
        ItemStack currentlyInThere = this.items.get(resultSlot);
        if (currentlyInThere.isEmpty()) {
            return true;
        } else if (!ItemStack.areItemsEqual(currentlyInThere, putIn)) {
            return false;
        } else if (currentlyInThere.getCount() + putIn.getCount() <= currentlyInThere.getMaxCount() && currentlyInThere.getCount() + putIn.getCount() <= currentlyInThere.getMaxCount()) {
            return true;
        } else {
            return currentlyInThere.getCount() + putIn.getCount() <= putIn.getMaxCount();
        }
    }

    @Override
    public int[] getAvailableSlots(Direction direction) {
        if (direction == Direction.DOWN)
            return SLOTS_FOR_DOWN;
        else if (direction == Direction.UP)
            return SLOTS_FOR_TOP;
        return new int[0];
    }

    @Override
    public boolean canInsert(int slot, ItemStack stack, @Nullable Direction direction) {
        if (spinSpeed > 0 && AlexsCavesEnriched.CONFIG.centrifuge.cantInteractWithActive) return false;
        if (direction != Direction.UP) return false;
        return this.isValid(slot, stack);
    }

    @Override
    public boolean canExtract(int slot, ItemStack stack, Direction direction) {
        if (spinSpeed > 0 && AlexsCavesEnriched.CONFIG.centrifuge.cantInteractWithActive) return false;
        if (direction == Direction.UP) return false;
        return slot >= N_INPUT_SLOTS;
    }

    @OnlyIn(Dist.CLIENT)
    public Box getRenderBoundingBox() {
        BlockPos pos = this.getPos(); // TODO
        return new Box(pos.add(-1, -1, -1), pos.add(2, 2, 2));
    }

    @Override
    public void readNbt(NbtCompound compoundTag) {
        super.readNbt(compoundTag);
        this.items.clear();
        Inventories.readNbt(compoundTag, this.items);
        this.loadAdditional(compoundTag);
    }

    private void loadAdditional(NbtCompound compoundTag) {
        this.spinSpeed = compoundTag.getInt("SpinSpeed");
    }

    @Override
    protected void writeNbt(NbtCompound compoundTag) {
        super.writeNbt(compoundTag);
        Inventories.writeNbt(compoundTag, this.items, true);
        compoundTag.putInt("SpinSpeed", this.spinSpeed);
    }

    @Override
    public BlockEntityUpdateS2CPacket toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    @Override
    public NbtCompound toInitialChunkDataNbt() {
        return this.createNbt();
    }

    @Override
    public void handleUpdateTag(NbtCompound tag) {
        super.handleUpdateTag(tag);
        this.readNbt(tag);
    }

    @Override
    public void onDataPacket(ClientConnection net, BlockEntityUpdateS2CPacket packet) {
        if (packet != null && packet.getNbt() != null)
            this.loadAdditional(packet.getNbt());
    }

    @Override
    public int size() {
        return this.items.size();
    }

    @Override
    public boolean isEmpty() {
        for (var itemStack : this.items)
            if (!itemStack.isEmpty())
                return false;
        return true;
    }

    @Override
    public ItemStack getStack(int slot) {
        return this.items.get(slot);
    }

    @Override
    public ItemStack removeStack(int slot, int count) {
        return Inventories.splitStack(this.items, slot, count);
    }

    @Override
    public ItemStack removeStack(int slot) {
        return Inventories.removeStack(this.items, slot);
    }

    @Override
    public void setStack(int slot, ItemStack itemStack) {
        ItemStack itemstack = this.items.get(slot);
        boolean flag = !itemStack.isEmpty() && ItemStack.canCombine(itemstack, itemStack);
        this.items.set(slot, itemStack);
        if (itemStack.getCount() > this.getMaxCountPerStack())
            itemStack.setCount(this.getMaxCountPerStack());
        if (slot == 0 && !flag)
            this.markDirty();
    }

    @Override
    public boolean canPlayerUse(PlayerEntity player) {
        return Inventory.canPlayerUse(this, player);
    }

    @Override
    public void markDirty() {
        if (world != null) {
            super.markDirty();
            world.updateComparators(this.getPos(), world.getBlockState(this.getPos()).getBlock());
        }
    }

    @Override
    public boolean isValid(int slot, ItemStack stack) {
        if (spinSpeed > 0 && AlexsCavesEnriched.CONFIG.centrifuge.cantInteractWithActive)
            return false;
        return slot < N_INPUT_SLOTS && inputAllowed(stack.getItem(), Objects.requireNonNull(this.getWorld()));
    }

    // Check if input type is allowed
    public static HashSet<Item> allowedItems = new HashSet<>();

    public static boolean inputAllowed(Item input, World level) {
        var recipes = level.getRecipeManager().listAllOfType(ACERecipeRegistry.CENTRIFUGE_TYPE.get());
        if (allowedItems.isEmpty()) { // Pre-populate with non-tags
            for (CentrifugeRecipe recipe : recipes) {
                if (recipe.getInput() != null)
                    allowedItems.add(recipe.getInput().getItem());
            }
        }
        if (allowedItems.contains(input))
            return true;
        var inputStack = new ItemStack(input, 1);
        for (CentrifugeRecipe recipe : recipes) {
            if (recipe.matches(inputStack)) {
                allowedItems.add(input);
                return true;
            }
        }
        return false;
    }

    @Override
    public void clear() {
        this.items.clear();
    }

    @Override
    protected Text getContainerName() {
        return Text.translatable("block.alexscavesenriched.centrifuge");
    }

    @Override
    protected ScreenHandler createScreenHandler(int syncId, PlayerInventory playerInventory) {
        return new CentrifugeBlockMenu(syncId, playerInventory, this);
    }

    @Override
    @NotNull
    public <T> LazyOptional<T> getCapability(Capability<T> capability, @javax.annotation.Nullable Direction facing) {
        return !this.removed && facing != null && capability == ForgeCapabilities.ITEM_HANDLER ?
                this.handlers[facing.ordinal()].cast() : super.getCapability(capability, facing);
    }
}
