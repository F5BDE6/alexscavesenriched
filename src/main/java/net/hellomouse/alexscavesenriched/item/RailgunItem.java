package net.hellomouse.alexscavesenriched.item;

import com.github.alexmodguy.alexscaves.server.enchantment.ACEnchantmentRegistry;
import com.github.alexmodguy.alexscaves.server.item.UpdatesStackTags;
import net.hellomouse.alexscavesenriched.*;
import net.hellomouse.alexscavesenriched.advancements.ACECriterionTriggers;
import net.hellomouse.alexscavesenriched.client.render.item.ACEClientItemExtension;
import net.hellomouse.alexscavesenriched.entity.BlackHoleEntity;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.SkeletonEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.RangedWeaponItem;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.checkerframework.checker.units.qual.A;
import org.jetbrains.annotations.NotNull;
import org.joml.AxisAngle4d;
import org.joml.Quaterniond;
import org.joml.Vector3d;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class RailgunItem extends RangedWeaponItem implements UpdatesStackTags {
    public static int MAX_CHARGE = 1000;
    public static int FIRE_TICK_TIME = 10;

    private boolean wasChargingInitiallyOnUse = false;
    private int fireTick = 0; // For shooting glow

    public RailgunItem() {
        super(new Settings()
            .rarity(Rarity.UNCOMMON)
            .fireproof()
            .maxCount(1)
        );
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(ACEClientItemExtension.INSTANCE);
    }

    public void shoot(World world, LivingEntity user, float angle) {
        Vec3d start = user.getEyePos();
        Vec3d look = user.getRotationVector();

        if (angle != 0) {
            Vec3d rotationAxis = Vec3d.fromPolar(user.getPitch() + 90, user.getYaw());
            Quaterniond angleQuat = new Quaterniond(new AxisAngle4d(angle / 180F * 3.1415926535F, rotationAxis.x, rotationAxis.y, rotationAxis.z));
            Vector3d look2 = angleQuat.transform(new Vector3d(look.x, look.y, look.z));
            look = new Vec3d(look2.x, look2.y, look2.z);
            look = look.normalize();
        }

        // Vec3d vel = look.multiply(15);
        // world.addParticle(ParticleTypes.FIREWORK, true, start.x, start.y, start.z,  vel.x, vel.y, vel.z);
        // No supersonic particle because mojang clamps velocity in netcode :(
        // world.addParticle(ACEParticleRegistry.RAILGUN_SHOCKWAVE.get(), true, shockwavePos.x, shockwavePos.y, shockwavePos.z, 0, 0, 0);

        final float STEP = 1.0F;
        for (double d = 0.0; d < AlexsCavesEnriched.CONFIG.railgun.range; d += STEP) {
            Vec3d point = start.add(look.multiply(d));

            if (d < 120) {
                int id = ((int)d);
                world.addParticle(ParticleTypes.CRIT, false, point.x, point.y, point.z, 0, 0, 0);
                if (id % 5 == 1)
                    world.addParticle(ACEParticleRegistry.RAILGUN_SHOCKWAVE.get(), true, point.x, point.y, point.z, 0, 0, 0);
            }

            BlockPos blockPos = new BlockPos((int) Math.floor(point.getX()), (int) Math.floor(point.getY()), (int) Math.floor(point.getZ()));
            var currentBlockBlastRes = world.getBlockState(blockPos).getBlock().getBlastResistance();
            if (currentBlockBlastRes > 500F)
                break;

            if (!world.isClient) {
                if (d < AlexsCavesEnriched.CONFIG.railgun.blockBreakRange && currentBlockBlastRes < 1F)
                    world.breakBlock(blockPos, true);

                Box box = new Box(point, point).expand(STEP);
                for (Entity entity : world.getOtherEntities(user, box, e -> e.getBoundingBox().raycast(start, point).isPresent())) {
                    if (entity instanceof BlackHoleEntity) {
                        d = AlexsCavesEnriched.CONFIG.railgun.range; // End outer loop
                        break;
                    }
                    if (entity instanceof LivingEntity living) {
                        living.damage(ACEDamageSources.causeRailgunDamage(world.getRegistryManager(), user),
                                AlexsCavesEnriched.CONFIG.railgun.damage);

                        if (living.getBoundingBox().getAverageSideLength() < 5)
                            living.addVelocity(look.x, look.y, look.z);
                        else
                            living.addVelocity(look.x * 0.1, look.y * 0.1, look.z * 0.1);
                        if (living instanceof SkeletonEntity && living.getHealth() <= 0.0)
                            ACECriterionTriggers.KILL_SKELETON_WITH_RAILGUN.triggerForEntity(user);
                    }
                }
            }
        }
    }

    // Usage
    @Override
    public void onStoppedUsing(ItemStack stack, World world, LivingEntity user, int remainingUseTicks) {
        if (!wasChargingInitiallyOnUse && getCharge(stack) == MAX_CHARGE && isLoaded(stack)) {
            world.playSound(user.getX(), user.getY(), user.getZ(),
                    ACESounds.RAILGUN_FIRE, SoundCategory.MASTER, 1.0F, 1.0F, true);

            boolean multishot = stack.getEnchantmentLevel(Enchantments.MULTISHOT) > 0 && AlexsCavesEnriched.CONFIG.railgun.multishot;
            if (multishot) {
                final float ANGLE = 20;
                this.shoot(world, user, -ANGLE);
                this.shoot(world, user, ANGLE);
            }

            this.shoot(world, user, 0);

            this.fireTick = FIRE_TICK_TIME;
            if (!world.isClient) {
                setLoaded(stack, false);
                setCharge(stack, 0);
            }
        }
    }

    @Override
    public void usageTick(World world, LivingEntity user, ItemStack stack, int remainingUseTicks) {
        if (isLoaded(stack) && getCharge(stack) < MAX_CHARGE) {
            if (world.getTime() % 25 == 0)
                world.playSound(user.getX(), user.getY(), user.getZ(),
                        ACESounds.RAILGUN_CHARGE, SoundCategory.MASTER, 1.0F, 1.0F, true);
            if (!world.isClient) {
                int chargeRate = AlexsCavesEnriched.CONFIG.railgun.chargeRate;
                int quickCharge = stack.getEnchantmentLevel(Enchantments.QUICK_CHARGE);
                if (!AlexsCavesEnriched.CONFIG.railgun.quickCharge)
                    quickCharge = 0;
                chargeRate = chargeRate + (int)(chargeRate * 0.22 * quickCharge);
                setCharge(stack, Math.min(MAX_CHARGE, getCharge(stack) + chargeRate));
            }
        }
    }

    // Shoot or load projectile
    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack itemStack = user.getStackInHand(hand);
        if (getCharge(itemStack) == MAX_CHARGE && isLoaded(itemStack)) {
            user.setCurrentHand(hand);
            wasChargingInitiallyOnUse = false;
            return TypedActionResult.consume(itemStack);
        } else if (!isLoaded(itemStack) && !user.getProjectileType(itemStack).isEmpty()) {
            ItemStack stack = user.getStackInHand(hand);
            ItemStack ammo = user.getProjectileType(stack);
            boolean flag = user.isCreative();
            wasChargingInitiallyOnUse = true;

            if (flag || !ammo.isEmpty()) { // Load ammo and recharge
                user.setCurrentHand(hand);
                if (!ammo.isEmpty())
                    world.playSound(user.getX(), user.getY(), user.getZ(),
                            ACESounds.RAILGUN_RELOAD, SoundCategory.MASTER, 1.0F, 1.0F, true);
                if (!world.isClient) {
                    setLoaded(itemStack,true);
                    boolean infinity = stack.getEnchantmentLevel(Enchantments.INFINITY) > 0;
                    if (!AlexsCavesEnriched.CONFIG.railgun.infinity)
                        infinity = false;
                    if (!user.isCreative() && !infinity)
                        ammo.decrement(1);
                }
            }
            return TypedActionResult.consume(itemStack);
        } else if (isLoaded(itemStack)) { // Recharge
            user.setCurrentHand(hand);
            wasChargingInitiallyOnUse = true;
            return TypedActionResult.consume(itemStack);
        }

        world.playSound(user.getX(), user.getY(), user.getZ(),
                ACESounds.RAILGUN_EMPTY, SoundCategory.MASTER, 1.0F, 1.0F, true);
        return TypedActionResult.fail(itemStack);
    }

    // Railgun charge tooltip
    @Override
    public int getItemBarStep(ItemStack stack) {
        return Math.round((float)getCharge(stack) * 13.0F / MAX_CHARGE);
    }

    @Override
    public boolean isItemBarVisible(ItemStack stack) { return true; }

    @Override
    public int getItemBarColor(ItemStack stack) {
        return MathHelper.hsvToRgb(0.51F, 0.4F, 1.0F);
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World worldIn, List<Text> tooltip, TooltipContext flagIn) {
        String chargeLeft = "" + (getCharge(stack));
        tooltip.add(Text.translatable("item.alexscaves.raygun.charge", chargeLeft, MAX_CHARGE).formatted(Formatting.BLUE));
    }

    // NBT
    public static int getCharge(ItemStack stack) {
        NbtCompound compoundTag = stack.getNbt();
        return compoundTag != null ? compoundTag.getInt("Charge") : 0;
    }

    public static void setCharge(ItemStack stack, int charge) {
        stack.getOrCreateNbt().putInt("Charge", charge);
    }

    public static boolean isLoaded(ItemStack stack) {
        NbtCompound compoundTag = stack.getNbt();
        return compoundTag != null && compoundTag.getBoolean("Loaded");
    }

    public static void setLoaded(ItemStack stack, boolean loaded) {
        stack.getOrCreateNbt().putBoolean("Loaded", loaded);
    }

    // Use actions
    @Override
    public int getMaxUseTime(@NotNull ItemStack stack) {
        return 72000;
    }

    @Override
    public @NotNull UseAction getUseAction(@NotNull ItemStack stack) { return UseAction.BOW; }

    @Override
    public int getEnchantability() { return 0; }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return false;
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return !oldStack.isOf(ACEItemRegistry.RAILGUN.get()) || !newStack.isOf(ACEItemRegistry.RAILGUN.get());
    }

    @Override
    public @NotNull Predicate<ItemStack> getProjectiles() {
        return e -> e.getItem() instanceof RailgunAmmoItem;
    }

    @Override
    public int getRange() { return 128; }

    @Override
    public boolean isUsedOnRelease(ItemStack stack) {
        return stack.isOf(this);
    }

    @Override
    public void inventoryTick(ItemStack itemStack, World level, Entity entity, int i, boolean b) {
        if (this.fireTick > 0)
            this.fireTick--;
    }

    public int getFireTick() { return this.fireTick; }
}
