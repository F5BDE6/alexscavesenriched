package net.hellomouse.alexscavesenriched.item;

import com.github.alexmodguy.alexscaves.AlexsCaves;
import com.github.alexmodguy.alexscaves.server.message.UpdateItemTagMessage;
import com.github.alexmodguy.alexscaves.server.misc.ACSoundRegistry;
import net.hellomouse.alexscavesenriched.ACEItemRegistry;
import net.hellomouse.alexscavesenriched.ACESounds;
import net.hellomouse.alexscavesenriched.AlexsCavesEnriched;
import net.hellomouse.alexscavesenriched.client.render.item.ACEClientItemExtension;
import net.hellomouse.alexscavesenriched.entity.FlamethrowerProjectileEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class FlamethrowerItem extends Item {
    private static final int MAX_CHARGE = 250;

    public static final float FIRING_SPEED = 2.4f;
    public static final float FIRING_RANDOMNESS = 0.1f;
    public static final float SPEED_RANDOMNESS = 0.01f;
    public static final Predicate<ItemStack> AMMO = (stack) -> stack.is(AlexsCavesEnriched.FLAMETHROWER_FUEL_TAG);

    public FlamethrowerItem() {
        super(new Properties().stacksTo(1));
    }

    public static boolean hasCharge(ItemStack stack) {
        return getCharge(stack) < MAX_CHARGE;
    }

    public static void setUseTime(ItemStack stack, int useTime) {
        CompoundTag tag = stack.getOrCreateTag();
        tag.putInt("PrevUseTime", getUseTime(stack));
        tag.putInt("UseTime", useTime);
    }

    public static int getUseTime(ItemStack stack) {
        CompoundTag compoundtag = stack.getTag();
        return compoundtag != null ? compoundtag.getInt("UseTime") : 0;
    }

    public static int getCharge(ItemStack stack) {
        CompoundTag compoundtag = stack.getTag();
        return compoundtag != null ? compoundtag.getInt("ChargeUsed") : 0;
    }

    public static void setCharge(ItemStack stack, int charge) {
        CompoundTag compoundtag = stack.getOrCreateTag();
        compoundtag.putInt("ChargeUsed", charge);
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return getCharge(stack) != 0;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return Math.round(13.0F - (float)getCharge(stack) * 13.0F / MAX_CHARGE);
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BOW;
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(ACEClientItemExtension.INSTANCE);
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 72000;
    }

    private ItemStack findAmmo(Player entity) {
        if (!entity.isCreative()) {
            for (int i = 0; i < entity.getInventory().getContainerSize(); ++i) {
                ItemStack itemstack1 = entity.getInventory().getItem(i);
                if (AMMO.test(itemstack1))
                    return itemstack1;
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
       if (getCharge(stack) != 0) {
            String chargeLeft = "" + (MAX_CHARGE - getCharge(stack));
           tooltip.add(Component.translatable("item.alexscaves.raygun.charge", chargeLeft, MAX_CHARGE).withStyle(ChatFormatting.YELLOW));
       }
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return !oldStack.is(ACEItemRegistry.FLAMETHROWER.get()) || !newStack.is(ACEItemRegistry.FLAMETHROWER.get());
    }

    @Override
    public void inventoryTick(ItemStack stack, Level world, Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, world, entity, slot, selected);
        boolean using = (entity instanceof LivingEntity living) && living.getUseItem().equals(stack);
        boolean holding = (entity instanceof LivingEntity living) && (living.getMainHandItem().equals(stack) || living.getOffhandItem().equals(stack));

        if (holding && world.isClientSide && world.random.nextInt() % 10 == 0) {
            Vec3 lookDir = entity.getViewVector(1.0F);
            world.addParticle(ParticleTypes.SMALL_FLAME,
                    entity.getX() + lookDir.x, entity.getY() + lookDir.y + 1, entity.getZ() + lookDir.z,
                    (world.random.nextFloat() - 0.5) * 0.1,
                    0.2,
                    (world.random.nextFloat() - 0.5) * 0.1);
        }

        int useTime = getUseTime(stack);
        if (world.isClientSide) {
            CompoundTag tag = stack.getOrCreateTag();
            if (tag.getInt("PrevUseTime") != tag.getInt("UseTime"))
                tag.putInt("PrevUseTime", getUseTime(stack));
            if (using && (float)useTime < 5.0F)
                setUseTime(stack, useTime + 1);
            if (!using && (float)useTime > 0.0F)
                setUseTime(stack, useTime - 1);
        }
    }

    @Override
    public void onUseTick(Level world, LivingEntity living, ItemStack stack, int timeUsing) {
        if (!world.isClientSide) {
            if ((!(living instanceof Player) || !((Player) living).isCreative()))
                setCharge(stack, Math.min(getCharge(stack) + 1, MAX_CHARGE));

            if (hasCharge(stack)) {
                if (world.getGameTime() % 10 == 0)
                    world.playSound(null, living.getX(), living.getY(), living.getZ(), ACESounds.FLAMETHROWER, living.getSoundSource(), 1.0F, 1.0F);
                for (int i = 0; i < 2; i++) {
                    Projectile fireProjectile = new FlamethrowerProjectileEntity(living, 0, 1, 0, world);
                    fireProjectile.setSecondsOnFire(100);

                    float offset1 = (world.getRandom().nextFloat() - 0.5f) * FIRING_RANDOMNESS;
                    float offset2 = (world.getRandom().nextFloat() - 0.5f) * FIRING_RANDOMNESS;
                    float speedOffset = (world.getRandom().nextFloat() - 0.5f) * SPEED_RANDOMNESS;

                    Vec3 launchDir = living.getViewVector(1.0F);
                    fireProjectile.setDeltaMovement(launchDir.scale(FIRING_SPEED).add(
                            new Vec3(offset1, offset2, speedOffset)));
                    fireProjectile.setPos(fireProjectile.position()
                            .add(new Vec3(0, 0.2, 0))
                            .add(fireProjectile.getDeltaMovement().scale(i + 0.5)));
                    world.addFreshEntity(fireProjectile);
                }
            } else {
                living.stopUsingItem();
                world.playSound(null, living.getX(), living.getY(), living.getZ(), ACESounds.FLAMETHROWER_EMPTY, living.getSoundSource(), 1.0F, 1.0F);
            }
        }
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity player, int useTimeLeft) {
        super.releaseUsing(stack, level, player, useTimeLeft);
        if (level.isClientSide)
            AlexsCaves.sendMSGToServer(new UpdateItemTagMessage(player.getId(), stack));
        AlexsCaves.PROXY.clearSoundCacheFor(player);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);
        if (hasCharge(itemstack)) {
            player.startUsingItem(hand);
            return InteractionResultHolder.consume(itemstack);
        } else {
            ItemStack ammo = this.findAmmo(player);
            boolean doReload = player.isCreative();
            if (!ammo.isEmpty()) {
                ammo.shrink(1);
                doReload = true;
            }

            if (doReload) {
                setCharge(itemstack, 0);
                player.level().playSound(null, player.getX(), player.getY(), player.getZ(), ACESounds.FLAMETHROWER_RELOAD, player.getSoundSource(), 1.0F, 1.0F);
            } else {
                player.level().playSound(null, player.getX(), player.getY(), player.getZ(), ACESounds.FLAMETHROWER_EMPTY, player.getSoundSource(), 1.0F, 1.0F);
            }
            return InteractionResultHolder.fail(itemstack);
        }
    }
}
