package net.hellomouse.alexscavesenriched.item.abs;

import net.hellomouse.alexscavesenriched.ACEItemRegistry;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

public abstract class AbstractNukaColaItem extends BlockItem {
    public AbstractNukaColaItem(Block block, Properties settings) {
        super(block, settings);
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.DRINK;
    }

    public abstract void affectUser(ItemStack stack, Level world, LivingEntity user);

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level world, LivingEntity user) {
        super.finishUsingItem(stack, world, user);
        if (user instanceof ServerPlayer serverPlayerEntity) {
            CriteriaTriggers.CONSUME_ITEM.trigger(serverPlayerEntity, stack);
            serverPlayerEntity.awardStat(Stats.ITEM_USED.get(this));
        }

        if (!world.isClientSide)
            this.affectUser(stack, world, user);

        if (stack.isEmpty()) {
            return new ItemStack(ACEItemRegistry.NUKA_COLA_EMPTY.get());
        } else {
            if (user instanceof Player playerEntity && !((Player) user).getAbilities().instabuild) {
                ItemStack itemStack =new ItemStack(ACEItemRegistry.NUKA_COLA_EMPTY.get());
                if (!playerEntity.getInventory().add(itemStack))
                    playerEntity.drop(itemStack, false);
            }
            return stack;
        }
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 40;
    }

    @Override
    public SoundEvent getDrinkingSound() {
        return SoundEvents.HONEY_DRINK;
    }

    @Override
    public SoundEvent getEatingSound() {
        return SoundEvents.HONEY_DRINK;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player user, InteractionHand hand) {
        return ItemUtils.startUsingInstantly(world, user, hand);
    }
}

