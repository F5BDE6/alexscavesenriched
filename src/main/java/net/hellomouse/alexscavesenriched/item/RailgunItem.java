package net.hellomouse.alexscavesenriched.item;

import F;
import com.github.alexmodguy.alexscaves.server.enchantment.ACEnchantmentRegistry;
import com.github.alexmodguy.alexscaves.server.item.UpdatesStackTags;
import net.hellomouse.alexscavesenriched.*;
import net.hellomouse.alexscavesenriched.advancements.ACECriterionTriggers;
import net.hellomouse.alexscavesenriched.client.render.item.ACEClientItemExtension;
import net.hellomouse.alexscavesenriched.entity.BlackHoleEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
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

public class RailgunItem extends ProjectileWeaponItem implements UpdatesStackTags {
    public static int MAX_CHARGE = 1000;
    public static int FIRE_TICK_TIME = 10;

    private boolean wasChargingInitiallyOnUse = false;
    private int fireTick = 0; // For shooting glow

    public RailgunItem() {
        super(new Properties()
            .rarity(Rarity.UNCOMMON)
                .fireResistant()
                .stacksTo(1)
        );
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(ACEClientItemExtension.INSTANCE);
    }

    // NBT
    public static int getCharge(ItemStack stack) {
        CompoundTag compoundTag = stack.getTag();
        return compoundTag != null ? compoundTag.getInt("Charge") : 0;
    }

    public static void setCharge(ItemStack stack, int charge) {
        stack.getOrCreateTag().putInt("Charge", charge);
    }

    public static boolean isLoaded(ItemStack stack) {
        CompoundTag compoundTag = stack.getTag();
        return compoundTag != null && compoundTag.getBoolean("Loaded");
    }

    public static void setLoaded(ItemStack stack, boolean loaded) {
        stack.getOrCreateTag().putBoolean("Loaded", loaded);
    }

    public void shoot(Level world, LivingEntity user, float angle) {
        Vec3 start = user.getEyePosition();
        Vec3 look = user.getLookAngle();

        if (angle != 0) {
            Vec3 rotationAxis = Vec3.directionFromRotation(user.getXRot() + 90, user.getYRot());
            Quaterniond angleQuat = new Quaterniond(new AxisAngle4d(angle / 180F * 3.1415926535F, rotationAxis.x, rotationAxis.y, rotationAxis.z));
            Vector3d look2 = angleQuat.transform(new Vector3d(look.x, look.y, look.z));
            look = new Vec3(look2.x, look2.y, look2.z);
            look = look.normalize();
        }

        // Vec3d vel = look.multiply(15);
        // world.addParticle(ParticleTypes.FIREWORK, true, start.x, start.y, start.z,  vel.x, vel.y, vel.z);
        // No supersonic particle because mojang clamps velocity in netcode :(
        // world.addParticle(ACEParticleRegistry.RAILGUN_SHOCKWAVE.get(), true, shockwavePos.x, shockwavePos.y, shockwavePos.z, 0, 0, 0);

        final float STEP = 1.0F;
        for (double d = 0.0; d < AlexsCavesEnriched.CONFIG.railgun.range; d += STEP) {
            Vec3 point = start.add(look.scale(d));

            if (d < 120) {
                int id = ((int)d);
                world.addParticle(ParticleTypes.CRIT, false, point.x, point.y, point.z, 0, 0, 0);
                if (id % 5 == 1)
                    world.addParticle(ACEParticleRegistry.RAILGUN_SHOCKWAVE.get(), true, point.x, point.y, point.z, 0, 0, 0);
            }

            BlockPos blockPos = new BlockPos((int) Math.floor(point.x()), (int) Math.floor(point.y()), (int) Math.floor(point.z()));
            var currentBlockBlastRes = world.getBlockState(blockPos).getBlock().getExplosionResistance();
            if (currentBlockBlastRes > 500F)
                break;

            if (!world.isClientSide) {
                if (d < AlexsCavesEnriched.CONFIG.railgun.blockBreakRange && currentBlockBlastRes < 1F)
                    world.destroyBlock(blockPos, true);

                AABB box = new AABB(point, point).inflate(STEP);
                for (Entity entity : world.getEntities(user, box, e -> e.getBoundingBox().clip(start, point).isPresent())) {
                    if (entity instanceof BlackHoleEntity) {
                        d = AlexsCavesEnriched.CONFIG.railgun.range; // End outer loop
                        break;
                    }
                    if (entity instanceof LivingEntity living) {
                        living.hurt(ACEDamageSources.causeRailgunDamage(world.registryAccess(), user),
                                AlexsCavesEnriched.CONFIG.railgun.damage);

                        if (living.getBoundingBox().getSize() < 5)
                            living.push(look.x, look.y, look.z);
                        else
                            living.push(look.x * 0.1, look.y * 0.1, look.z * 0.1);
                        if (living instanceof Skeleton && living.getHealth() <= 0.0)
                            ACECriterionTriggers.KILL_SKELETON_WITH_RAILGUN.triggerForEntity(user);
                    }
                }
            }
        }
    }

    // Usage
    @Override
    public void releaseUsing(ItemStack stack, Level world, LivingEntity user, int remainingUseTicks) {
        if (!wasChargingInitiallyOnUse && getCharge(stack) == MAX_CHARGE && isLoaded(stack)) {
            world.playLocalSound(user.getX(), user.getY(), user.getZ(),
                    ACESounds.RAILGUN_FIRE, SoundSource.MASTER, 1.0F, 1.0F, true);

            boolean multishot = stack.getEnchantmentLevel(Enchantments.MULTISHOT) > 0 && AlexsCavesEnriched.CONFIG.railgun.multishot;
            if (multishot) {
                final float ANGLE = 20;
                this.shoot(world, user, -ANGLE);
                this.shoot(world, user, ANGLE);
            }

            this.shoot(world, user, 0);

            this.fireTick = FIRE_TICK_TIME;
            if (!world.isClientSide) {
                setLoaded(stack, false);
                setCharge(stack, 0);
            }
        }
    }

    @Override
    public void onUseTick(Level world, LivingEntity user, ItemStack stack, int remainingUseTicks) {
        if (isLoaded(stack) && getCharge(stack) < MAX_CHARGE) {
            if (world.getGameTime() % 25 == 0)
                world.playLocalSound(user.getX(), user.getY(), user.getZ(),
                        ACESounds.RAILGUN_CHARGE, SoundSource.MASTER, 1.0F, 1.0F, true);
            if (!world.isClientSide) {
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
    public InteractionResultHolder<ItemStack> use(Level world, Player user, InteractionHand hand) {
        ItemStack itemStack = user.getItemInHand(hand);
        if (getCharge(itemStack) == MAX_CHARGE && isLoaded(itemStack)) {
            user.startUsingItem(hand);
            wasChargingInitiallyOnUse = false;
            return InteractionResultHolder.consume(itemStack);
        } else if (!isLoaded(itemStack) && !user.getProjectile(itemStack).isEmpty()) {
            ItemStack stack = user.getItemInHand(hand);
            ItemStack ammo = user.getProjectile(stack);
            boolean flag = user.isCreative();
            wasChargingInitiallyOnUse = true;

            if (flag || !ammo.isEmpty()) { // Load ammo and recharge
                user.startUsingItem(hand);
                if (!ammo.isEmpty())
                    world.playLocalSound(user.getX(), user.getY(), user.getZ(),
                            ACESounds.RAILGUN_RELOAD, SoundSource.MASTER, 1.0F, 1.0F, true);
                if (!world.isClientSide) {
                    setLoaded(itemStack,true);
                    boolean infinity = stack.getEnchantmentLevel(Enchantments.INFINITY_ARROWS) > 0;
                    if (!AlexsCavesEnriched.CONFIG.railgun.infinity)
                        infinity = false;
                    if (!user.isCreative() && !infinity)
                        ammo.shrink(1);
                }
            }
            return InteractionResultHolder.consume(itemStack);
        } else if (isLoaded(itemStack)) { // Recharge
            user.startUsingItem(hand);
            wasChargingInitiallyOnUse = true;
            return InteractionResultHolder.consume(itemStack);
        }

        world.playLocalSound(user.getX(), user.getY(), user.getZ(),
                ACESounds.RAILGUN_EMPTY, SoundSource.MASTER, 1.0F, 1.0F, true);
        return InteractionResultHolder.fail(itemStack);
    }

    // Railgun charge tooltip
    @Override
    public int getBarWidth(ItemStack stack) {
        return Math.round((float)getCharge(stack) * 13.0F / MAX_CHARGE);
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return true;
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return Mth.hsvToRgb(0.51F, 0.4F, 1.0F);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
        String chargeLeft = "" + (getCharge(stack));
        tooltip.add(Component.translatable("item.alexscaves.raygun.charge", chargeLeft, MAX_CHARGE).withStyle(ChatFormatting.BLUE));
    }

    // Use actions
    @Override
    public int getUseDuration(@NotNull ItemStack stack) {
        return 72000;
    }

    @Override
    public @NotNull UseAnim getUseAnimation(@NotNull ItemStack stack) {
        return UseAnim.BOW;
    }

    @Override
    public int getEnchantmentValue() {
        return 0;
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return false;
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return !oldStack.is(ACEItemRegistry.RAILGUN.get()) || !newStack.is(ACEItemRegistry.RAILGUN.get());
    }

    @Override
    public @NotNull Predicate<ItemStack> getAllSupportedProjectiles() {
        return e -> e.getItem() instanceof RailgunAmmoItem;
    }

    @Override
    public int getDefaultProjectileRange() {
        return 128;
    }

    @Override
    public boolean useOnRelease(ItemStack stack) {
        return stack.is(this);
    }

    @Override
    public void inventoryTick(ItemStack itemStack, Level level, Entity entity, int i, boolean b) {
        if (this.fireTick > 0)
            this.fireTick--;
    }

    public int getFireTick() { return this.fireTick; }
}
