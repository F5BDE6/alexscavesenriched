package net.hellomouse.alexscavesenriched.inventory;

import net.hellomouse.alexscavesenriched.ACEMenuRegistry;
import net.hellomouse.alexscavesenriched.block.block_entity.CentrifugeBlockEntity;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import java.util.Objects;

public class CentrifugeBlockMenu extends AbstractContainerMenu {
    private final Container inventory;

    public CentrifugeBlockMenu(int syncId, Inventory playerInv, Container blockInv) {
        super(ACEMenuRegistry.CENTRIFUGE.get(), syncId);
        this.inventory = blockInv;
        checkContainerSize(blockInv, 18);
        blockInv.startOpen(playerInv.player);

        // Input slots (0–8)
        for (int row = 0; row < 3; ++row)
            for (int col = 0; col < 3; ++col)
                this.addSlot(new Slot(blockInv, col + row * 3, 8 + col * 18, 16 + row * 18) {
                    @Override
                    public boolean mayPlace(ItemStack stack) {
                        return CentrifugeBlockEntity.inputAllowed(stack.getItem(), playerInv.player.level());
                    }
                });

        // Output slots (9–17)
        for (int row = 0; row < 3; ++row)
            for (int col = 0; col < 3; ++col)
                this.addSlot(new Slot(blockInv, 9 + col + row * 3, 97 + col * 18, 16 + row * 18) {
                    @Override
                    public boolean mayPlace(ItemStack stack) {
                        return CentrifugeBlockEntity.inputAllowed(stack.getItem(), playerInv.player.level());
                    }
                });

        // Player inventory
        for (int row = 0; row < 3; ++row)
            for (int col = 0; col < 9; ++col)
                this.addSlot(new Slot(playerInv, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));

        // Hotbar
        for (int col = 0; col < 9; ++col)
            this.addSlot(new Slot(playerInv, col, 8 + col * 18, 142));
    }

    public CentrifugeBlockMenu(int syncId, Inventory playerInv) {
        this(syncId, playerInv, new SimpleContainer(18));
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slotIndex) {
        ItemStack newStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(slotIndex);
        if (slot.hasItem()) {
            ItemStack slotItemStack = slot.getItem();
            newStack = slotItemStack.copy();

            if (slotIndex < CentrifugeBlockEntity.N_SLOTS) { // Item -> player inv
                if (!this.moveItemStackTo(slotItemStack, CentrifugeBlockEntity.N_SLOTS, this.slots.size(), false))
                    return ItemStack.EMPTY;
            } else { // player inv -> input
                if (!CentrifugeBlockEntity.inputAllowed(slotItemStack.getItem(), Objects.requireNonNull(player.level())))
                    return ItemStack.EMPTY;
                if (!this.moveItemStackTo(slotItemStack, 0, CentrifugeBlockEntity.N_INPUT_SLOTS, false))
                    return ItemStack.EMPTY;
            }
            if (slotItemStack.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }
        return newStack;
    }

    @Override
    public boolean stillValid(Player player) {
        return this.inventory.stillValid(player);
    }

    public Container getInventory() {
        return inventory;
    }
}

