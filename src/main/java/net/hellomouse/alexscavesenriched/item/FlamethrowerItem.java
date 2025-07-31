package net.hellomouse.alexscavesenriched.item;

import com.github.alexmodguy.alexscaves.AlexsCaves;
import com.github.alexmodguy.alexscaves.server.message.UpdateItemTagMessage;
import com.github.alexmodguy.alexscaves.server.misc.ACSoundRegistry;
import net.hellomouse.alexscavesenriched.ACEItemRegistry;
import net.hellomouse.alexscavesenriched.ACESounds;
import net.hellomouse.alexscavesenriched.AlexsCavesEnriched;
import net.hellomouse.alexscavesenriched.client.render.item.ACEClientItemExtension;
import net.hellomouse.alexscavesenriched.entity.FlamethrowerProjectileEntity;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
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
    public static final Predicate<ItemStack> AMMO = (stack) -> stack.isIn(AlexsCavesEnriched.FLAMETHROWER_FUEL_TAG);

    public FlamethrowerItem() {
        super(new Settings().maxCount(1));
    }

    public static boolean hasCharge(ItemStack stack) {
        return getCharge(stack) < MAX_CHARGE;
    }

    public static void setUseTime(ItemStack stack, int useTime) {
        NbtCompound tag = stack.getOrCreateNbt();
        tag.putInt("PrevUseTime", getUseTime(stack));
        tag.putInt("UseTime", useTime);
    }

    public static int getUseTime(ItemStack stack) {
        NbtCompound compoundtag = stack.getNbt();
        return compoundtag != null ? compoundtag.getInt("UseTime") : 0;
    }

    public static int getCharge(ItemStack stack) {
        NbtCompound compoundtag = stack.getNbt();
        return compoundtag != null ? compoundtag.getInt("ChargeUsed") : 0;
    }

    public static void setCharge(ItemStack stack, int charge) {
        NbtCompound compoundtag = stack.getOrCreateNbt();
        compoundtag.putInt("ChargeUsed", charge);
    }

    @Override
    public boolean isItemBarVisible(ItemStack stack) {
        return getCharge(stack) != 0;
    }

    @Override
    public int getItemBarStep(ItemStack stack) {
        return Math.round(13.0F - (float)getCharge(stack) * 13.0F / MAX_CHARGE);
    }

    @Override
    public UseAction getUseAction(ItemStack stack) {
        return UseAction.BOW;
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(ACEClientItemExtension.INSTANCE);
    }

    @Override
    public int getMaxUseTime(ItemStack stack) {
        return 72000;
    }

    private ItemStack findAmmo(PlayerEntity entity) {
        if (!entity.isCreative()) {
            for (int i = 0; i < entity.getInventory().size(); ++i) {
                ItemStack itemstack1 = entity.getInventory().getStack(i);
                if (AMMO.test(itemstack1))
                    return itemstack1;
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World worldIn, List<Text> tooltip, TooltipContext flagIn) {
       if (getCharge(stack) != 0) {
            String chargeLeft = "" + (MAX_CHARGE - getCharge(stack));
            tooltip.add(Text.translatable("item.alexscaves.raygun.charge", chargeLeft, MAX_CHARGE).formatted(Formatting.YELLOW));
       }
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return !oldStack.isOf(ACEItemRegistry.FLAMETHROWER.get()) || !newStack.isOf(ACEItemRegistry.FLAMETHROWER.get());
    }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, world, entity, slot, selected);
        boolean using = (entity instanceof LivingEntity living) && living.getActiveItem().equals(stack);
        boolean holding = (entity instanceof LivingEntity living) && (living.getMainHandStack().equals(stack) || living.getOffHandStack().equals(stack));

        if (holding && world.isClient && world.random.nextInt() % 10 == 0) {
            Vec3d lookDir = entity.getRotationVec(1.0F);
            world.addParticle(ParticleTypes.SMALL_FLAME,
                    entity.getX() + lookDir.x, entity.getY() + lookDir.y + 1, entity.getZ() + lookDir.z,
                    (world.random.nextFloat() - 0.5) * 0.1,
                    0.2,
                    (world.random.nextFloat() - 0.5) * 0.1);
        }

        int useTime = getUseTime(stack);
        if (world.isClient) {
            NbtCompound tag = stack.getOrCreateNbt();
            if (tag.getInt("PrevUseTime") != tag.getInt("UseTime"))
                tag.putInt("PrevUseTime", getUseTime(stack));
            if (using && (float)useTime < 5.0F)
                setUseTime(stack, useTime + 1);
            if (!using && (float)useTime > 0.0F)
                setUseTime(stack, useTime - 1);
        }
    }

    @Override
    public void usageTick(World world, LivingEntity living, ItemStack stack, int timeUsing) {
        if (!world.isClient) {
            if ((!(living instanceof PlayerEntity) || !((PlayerEntity)living).isCreative()))
                setCharge(stack, Math.min(getCharge(stack) + 1, MAX_CHARGE));

            if (hasCharge(stack)) {
                if (world.getTime() % 10 == 0)
                    world.playSound(null, living.getX(), living.getY(), living.getZ(), ACESounds.FLAMETHROWER, living.getSoundCategory(), 1.0F, 1.0F);
                for (int i = 0; i < 2; i++) {
                    ProjectileEntity fireProjectile = new FlamethrowerProjectileEntity(living, 0, 1, 0, world);
                    fireProjectile.setOnFireFor(100);

                    float offset1 = (world.getRandom().nextFloat() - 0.5f) * FIRING_RANDOMNESS;
                    float offset2 = (world.getRandom().nextFloat() - 0.5f) * FIRING_RANDOMNESS;
                    float speedOffset = (world.getRandom().nextFloat() - 0.5f) * SPEED_RANDOMNESS;

                    Vec3d launchDir = living.getRotationVec(1.0F);
                    fireProjectile.setVelocity(launchDir.multiply(FIRING_SPEED).add(
                            new Vec3d(offset1, offset2, speedOffset)));
                    fireProjectile.setPosition(fireProjectile.getPos()
                            .add(new Vec3d(0, 0.2, 0))
                            .add(fireProjectile.getVelocity().multiply(i + 0.5)));
                    world.spawnEntity(fireProjectile);
                }
            } else {
                living.clearActiveItem();
                world.playSound(null, living.getX(), living.getY(), living.getZ(), ACESounds.FLAMETHROWER_EMPTY, living.getSoundCategory(), 1.0F, 1.0F);
            }
        }
    }

    @Override
    public void onStoppedUsing(ItemStack stack, World level, LivingEntity player, int useTimeLeft) {
        super.onStoppedUsing(stack, level, player, useTimeLeft);
        if (level.isClient)
            AlexsCaves.sendMSGToServer(new UpdateItemTagMessage(player.getId(), stack));
        AlexsCaves.PROXY.clearSoundCacheFor(player);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack itemstack = player.getStackInHand(hand);
        if (hasCharge(itemstack)) {
            player.setCurrentHand(hand);
            return TypedActionResult.consume(itemstack);
        } else {
            ItemStack ammo = this.findAmmo(player);
            boolean doReload = player.isCreative();
            if (!ammo.isEmpty()) {
                ammo.decrement(1);
                doReload = true;
            }

            if (doReload) {
                setCharge(itemstack, 0);
                player.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(), ACESounds.FLAMETHROWER_RELOAD, player.getSoundCategory(), 1.0F, 1.0F);
            } else {
                player.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(), ACESounds.FLAMETHROWER_EMPTY, player.getSoundCategory(), 1.0F, 1.0F);
            }
            return TypedActionResult.fail(itemstack);
        }
    }
}
