package net.hellomouse.alexscavesenriched.item;

import com.github.alexmodguy.alexscaves.server.item.UpdatesStackTags;
import net.hellomouse.alexscavesenriched.ACEItemRegistry;
import net.hellomouse.alexscavesenriched.AlexsCavesEnriched;
import net.hellomouse.alexscavesenriched.advancements.ACECriterionTriggers;
import net.hellomouse.alexscavesenriched.client.render.item.ACEClientItemExtension;
import net.hellomouse.alexscavesenriched.entity.IRocketEntity;
import net.hellomouse.alexscavesenriched.entity.RocketEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;
import java.util.function.Predicate;

public class RocketLauncherItem extends BowItem implements UpdatesStackTags {

    private static final float MAX_LOAD_TIME = 40.0f;

    public RocketLauncherItem() {
        super(new Item.Properties()
                .rarity(Rarity.UNCOMMON)
                .fireResistant()
                .stacksTo(1)
        );
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(ACEClientItemExtension.INSTANCE);
    }

    public static float getPowerForTime(int i) {
        float f = (float) i / MAX_LOAD_TIME;
        f = (f * f + f * 2.0F) / 3.0F;
        if (f > 1.0F) { f = 1.0F; }
        return f;
    }

    public @NotNull InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand interactionHand) {
        ItemStack itemstack = player.getItemInHand(interactionHand);
        ItemStack ammo = player.getProjectile(itemstack);
        boolean flag = player.isCreative();
        if(flag || !ammo.isEmpty()) {
            player.startUsingItem(interactionHand);
            return InteractionResultHolder.consume(itemstack);
        } else {
            return InteractionResultHolder.fail(itemstack);
        }
    }

    public int getUseDuration(@NotNull ItemStack stack) {
        return 72000;
    }

    @Override
    public @NotNull UseAnim getUseAnimation(@NotNull ItemStack stack) {
        return UseAnim.BOW;
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return stack.getCount() == 1;
    }

    @Override
    public int getEnchantmentValue() {
        return 1;
    }

    private AbstractArrow createRocket(Player player, ItemStack ammoIn) {
        IRocketItem rocket = (IRocketItem)(ammoIn.getItem() instanceof IRocketItem ? ammoIn.getItem() : ACEItemRegistry.ROCKET_NORMAL.get());
        return rocket.createRocket(player.getCommandSenderWorld(), ammoIn, player);
    }

    public void releaseUsing(ItemStack itemStack, Level level, LivingEntity livingEntity, int i1) {
        if (!(livingEntity instanceof Player player))
            return;

        int i = this.getUseDuration(itemStack) - i1;
        float f = getPowerForTime(i);

        if (f > 0.01D) {
            player.playSound(SoundEvents.GENERIC_EXPLODE);
            ItemStack ammoStack = player.getProjectile(itemStack);
            AbstractArrow rocket = createRocket(player, ammoStack);

            if (rocket != null) {
                // Fire rocket
                int power = itemStack.getEnchantmentLevel(Enchantments.POWER_ARROWS);
                rocket.pickup = AbstractArrow.Pickup.DISALLOWED;
                Vec3 launchPos = player.getEyePosition();
                Vec3 launchDir = player.getViewVector(1.0F);
                rocket.setPos(launchPos);
                rocket.shoot(launchDir.x, launchDir.y, launchDir.z,
                        (float) (AlexsCavesEnriched.CONFIG.rocketLauncher.baseSpeed + AlexsCavesEnriched.CONFIG.rocketLauncher.powerSpeed * power),
                        0.0F); // 0.0F randomness
                if (rocket instanceof IRocketEntity && itemStack.getEnchantmentLevel(Enchantments.FLAMING_ARROWS) > 0)
                    ((IRocketEntity) rocket).setIsFlame(true);
                level.addFreshEntity(rocket);

                // Back blast (near)
                boolean backblastAreaNotClear = false;
                AABB bashBoxNear = new AABB(new BlockPos((int) launchPos.x(), (int) launchPos.y(), (int) launchPos.z()))
                        .expandTowards(launchDir.scale(-1.0)).inflate(0.5);
                for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, bashBoxNear)) {
                    if (entity == player)
                        continue;
                    Vec3 delta = entity.getPosition(1.0F).subtract(launchPos);
                    entity.setSecondsOnFire(10);
                    entity.addDeltaMovement(delta.normalize().scale(5.0F));
                    backblastAreaNotClear = true;
                    entity.hurt(level.damageSources().explosion(player, entity), (float) AlexsCavesEnriched.CONFIG.rocketLauncher.backblastDirectDamage);
                }

                // Far away back blast
                Vec3 rpgBackPos = launchPos.add(launchDir.scale(-0.5F));
                final double backBlastCosAngle = Math.cos(AlexsCavesEnriched.CONFIG.rocketLauncher.backblastAngle * Math.PI / 180);

                AABB bashBox = new AABB(new BlockPos((int) launchPos.x(), (int) launchPos.y(), (int) launchPos.z()))
                        .expandTowards(launchDir.scale(-(AlexsCavesEnriched.CONFIG.rocketLauncher.backblastRange + 1.0)));
                for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, bashBox)) {
                    if (entity == player)
                        continue;
                    Vec3 delta = entity.getPosition(1.0F).subtract(rpgBackPos);

                    // Compare bottom, center and top of bounding box because I'm too lazy
                    // to figure out cone AABB intersection
                    if (-delta.normalize().dot(launchDir) < backBlastCosAngle - 0.01) {
                        Vec3 delta2 = entity.getPosition(0.0F)
                                .add(0.0, entity.getBbHeight() * 0.5, 0.0)
                                .subtract(rpgBackPos);
                        if (-delta2.normalize().dot(launchDir) < backBlastCosAngle - 0.01) {
                            Vec3 delta3 = entity.getPosition(0.0F)
                                    .add(0.0, entity.getBbHeight(), 0.0)
                                    .subtract(rpgBackPos);
                            if (-delta3.normalize().dot(launchDir) < backBlastCosAngle - 0.01)
                                continue;
                        }
                    }
                    if (!entity.hasLineOfSight(player))
                        continue;
                    entity.setSecondsOnFire(5);
                    entity.addDeltaMovement(delta.normalize().scale(1.0F));
                    backblastAreaNotClear = true;
                }

                // Backblast smoke
                final double TRAIL_SPEED = 1.0;
                final double BIG_MAGNITUDE = 1000.0F;
                Vec3 axis1 = new Vec3(
                        Math.abs(launchDir.x) > 1 / BIG_MAGNITUDE ? -1 / launchDir.x : -BIG_MAGNITUDE * Math.signum(launchDir.x),
                        Math.abs(launchDir.y) > 1 / BIG_MAGNITUDE ? -1 / launchDir.y : -BIG_MAGNITUDE * Math.signum(launchDir.y),
                        Math.abs(launchDir.z) > 1 / BIG_MAGNITUDE ? 2 / launchDir.z : 2 * BIG_MAGNITUDE * Math.signum(launchDir.z));
                axis1 = axis1.normalize();
                Vec3 axis2 = launchDir.normalize().cross(axis1).normalize();

                final double TOTAL_TRAILS = 30.0;
                for (int trails = 0; trails < TOTAL_TRAILS; trails++) {
                    double angle = trails / TOTAL_TRAILS * Math.PI * 2;
                    Vec3 circleOffset = axis1.scale(Math.cos(angle))
                            .add(axis2.scale(Math.sin(angle)))
                            .normalize()
                            .scale(TRAIL_SPEED * Math.sqrt(1 / Math.pow(Math.max(backBlastCosAngle, 0.01), 2.0) - 1.0));
                    Vec3 rand1 = new Vec3(level.random.nextDouble(), level.random.nextDouble(), level.random.nextDouble()).normalize().scale(0.2F);
                    Vec3 rand2 = new Vec3(level.random.nextDouble(), level.random.nextDouble(), level.random.nextDouble()).normalize().scale(0.2F);

                    Vec3 trailVelocity = launchDir.scale(-TRAIL_SPEED).add(circleOffset).add(rand1);
                    Vec3 trailVelocity2 = launchDir.scale(-TRAIL_SPEED).add(circleOffset.scale(0.6F)).add(rand2);

                    level.addParticle(ParticleTypes.CAMPFIRE_SIGNAL_SMOKE, rpgBackPos.x, rpgBackPos.y, rpgBackPos.z,
                            trailVelocity.x, trailVelocity.y, trailVelocity.z);
                    level.addParticle(ParticleTypes.FLAME, rpgBackPos.x, rpgBackPos.y, rpgBackPos.z,
                            trailVelocity2.x, trailVelocity2.y, trailVelocity2.z);
                }

                if (backblastAreaNotClear)
                    ACECriterionTriggers.KILL_MOB_WITH_BACKBLAST.triggerForEntity(player);

                // Cooldown + misc
                if (AlexsCavesEnriched.CONFIG.rocketLauncher.cooldown > 0)
                    player.getCooldowns().addCooldown(itemStack.getItem(), AlexsCavesEnriched.CONFIG.rocketLauncher.cooldown);
                if (!player.isCreative())
                    ammoStack.shrink(1);
            }
        }
    }

    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return !oldStack.is(ACEItemRegistry.ROCKET_LAUNCHER.get()) || !newStack.is(ACEItemRegistry.ROCKET_LAUNCHER.get());
    }

    @Override
    public @NotNull Predicate<ItemStack> getAllSupportedProjectiles() {
        return e -> e.getItem() instanceof IRocketItem;
    }

    @Override
    public int getDefaultProjectileRange() {
        return 128;
    }

    @Override
    public @NotNull AbstractArrow customArrow(@NotNull AbstractArrow arrow) {
        return new RocketEntity(arrow.getCommandSenderWorld(), (LivingEntity) arrow.getOwner());
    }
}