package net.hellomouse.alexscavesenriched.item;

import com.github.alexmodguy.alexscaves.server.item.UpdatesStackTags;
import net.hellomouse.alexscavesenriched.ACEItemRegistry;
import net.hellomouse.alexscavesenriched.AlexsCavesEnriched;
import net.hellomouse.alexscavesenriched.advancements.ACECriterionTriggers;
import net.hellomouse.alexscavesenriched.client.render.item.ACEClientItemExtension;
import net.hellomouse.alexscavesenriched.entity.IRocketEntity;
import net.hellomouse.alexscavesenriched.entity.RocketEntity;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.BowItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.Rarity;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;
import java.util.function.Predicate;

public class RocketLauncherItem extends BowItem implements UpdatesStackTags {

    private static final float MAX_LOAD_TIME = 40.0f;

    public RocketLauncherItem() {
        super(new Item.Settings()
                .rarity(Rarity.UNCOMMON)
                .fireproof()
                .maxCount(1)
        );
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(ACEClientItemExtension.INSTANCE);
    }

    public @NotNull TypedActionResult<ItemStack> use(World level, PlayerEntity player, Hand interactionHand) {
        ItemStack itemstack = player.getStackInHand(interactionHand);
        ItemStack ammo = player.getProjectileType(itemstack);
        boolean flag = player.isCreative();
        if(flag || !ammo.isEmpty()) {
            player.setCurrentHand(interactionHand);
            return TypedActionResult.consume(itemstack);
        } else {
            return TypedActionResult.fail(itemstack);
        }
    }

    public int getMaxUseTime(@NotNull ItemStack stack) {
        return 72000;
    }

    @Override
    public @NotNull UseAction getUseAction(@NotNull ItemStack stack) { return UseAction.BOW; }

    @Override
    public int getEnchantability() { return 1; }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return stack.getCount() == 1;
    }

    public static float getPullProgress(int i) {
        float f = (float) i / MAX_LOAD_TIME;
        f = (f * f + f * 2.0F) / 3.0F;
        if (f > 1.0F) { f = 1.0F; }
        return f;
    }

    private PersistentProjectileEntity createRocket(PlayerEntity player, ItemStack ammoIn) {
        IRocketItem rocket = (IRocketItem)(ammoIn.getItem() instanceof IRocketItem ? ammoIn.getItem() : ACEItemRegistry.ROCKET_NORMAL.get());
        return rocket.createRocket(player.getEntityWorld(), ammoIn, player);
    }

    public void onStoppedUsing(ItemStack itemStack, World level, LivingEntity livingEntity, int i1) {
        if (!(livingEntity instanceof PlayerEntity player))
            return;

        int i = this.getMaxUseTime(itemStack) - i1;
        float f = getPullProgress(i);

        if (f > 0.01D) {
            player.playSoundIfNotSilent(SoundEvents.ENTITY_GENERIC_EXPLODE);
            ItemStack ammoStack = player.getProjectileType(itemStack);
            PersistentProjectileEntity rocket = createRocket(player, ammoStack);

            if (rocket != null) {
                // Fire rocket
                int power = itemStack.getEnchantmentLevel(Enchantments.POWER);
                rocket.pickupType = PersistentProjectileEntity.PickupPermission.DISALLOWED;
                Vec3d launchPos = player.getEyePos();
                Vec3d launchDir = player.getRotationVec(1.0F);
                rocket.setPosition(launchPos);
                rocket.setVelocity(launchDir.x, launchDir.y, launchDir.z,
                        (float) (AlexsCavesEnriched.CONFIG.rocketLauncher.baseSpeed + AlexsCavesEnriched.CONFIG.rocketLauncher.powerSpeed * power),
                        0.0F); // 0.0F randomness
                if (rocket instanceof IRocketEntity && itemStack.getEnchantmentLevel(Enchantments.FLAME) > 0)
                    ((IRocketEntity) rocket).setIsFlame(true);
                level.spawnEntity(rocket);

                // Back blast (near)
                boolean backblastAreaNotClear = false;
                Box bashBoxNear = new Box(new BlockPos((int) launchPos.getX(), (int) launchPos.getY(), (int) launchPos.getZ()))
                        .stretch(launchDir.multiply(-1.0)).expand(0.5);
                for (LivingEntity entity : level.getNonSpectatingEntities(LivingEntity.class, bashBoxNear)) {
                    if (entity == player)
                        continue;
                    Vec3d delta = entity.getLerpedPos(1.0F).subtract(launchPos);
                    entity.setOnFireFor(10);
                    entity.addVelocity(delta.normalize().multiply(5.0F));
                    backblastAreaNotClear = true;
                    entity.damage(level.getDamageSources().explosion(player, entity), (float) AlexsCavesEnriched.CONFIG.rocketLauncher.backblastDirectDamage);
                }

                // Far away back blast
                Vec3d rpgBackPos = launchPos.add(launchDir.multiply(-0.5F));
                final double backBlastCosAngle = Math.cos(AlexsCavesEnriched.CONFIG.rocketLauncher.backblastAngle * Math.PI / 180);

                Box bashBox = new Box(new BlockPos((int) launchPos.getX(), (int) launchPos.getY(), (int) launchPos.getZ()))
                        .stretch(launchDir.multiply(-(AlexsCavesEnriched.CONFIG.rocketLauncher.backblastRange + 1.0)));
                for (LivingEntity entity : level.getNonSpectatingEntities(LivingEntity.class, bashBox)) {
                    if (entity == player)
                        continue;
                    Vec3d delta = entity.getLerpedPos(1.0F).subtract(rpgBackPos);

                    // Compare bottom, center and top of bounding box because I'm too lazy
                    // to figure out cone AABB intersection
                    if (-delta.normalize().dotProduct(launchDir) < backBlastCosAngle - 0.01) {
                        Vec3d delta2 = entity.getLerpedPos(0.0F)
                                .add(0.0, entity.getHeight() * 0.5, 0.0)
                                .subtract(rpgBackPos);
                        if (-delta2.normalize().dotProduct(launchDir) < backBlastCosAngle - 0.01) {
                            Vec3d delta3 = entity.getLerpedPos(0.0F)
                                    .add(0.0, entity.getHeight(), 0.0)
                                    .subtract(rpgBackPos);
                            if (-delta3.normalize().dotProduct(launchDir) < backBlastCosAngle - 0.01)
                                continue;
                        }
                    }
                    if (!entity.canSee(player))
                        continue;
                    entity.setOnFireFor(5);
                    entity.addVelocity(delta.normalize().multiply(1.0F));
                    backblastAreaNotClear = true;
                }

                // Backblast smoke
                final double TRAIL_SPEED = 1.0;
                final double BIG_MAGNITUDE = 1000.0F;
                Vec3d axis1 = new Vec3d(
                        Math.abs(launchDir.x) > 1 / BIG_MAGNITUDE ? -1 / launchDir.x : -BIG_MAGNITUDE * Math.signum(launchDir.x),
                        Math.abs(launchDir.y) > 1 / BIG_MAGNITUDE ? -1 / launchDir.y : -BIG_MAGNITUDE * Math.signum(launchDir.y),
                        Math.abs(launchDir.z) > 1 / BIG_MAGNITUDE ? 2 / launchDir.z : 2 * BIG_MAGNITUDE * Math.signum(launchDir.z));
                axis1 = axis1.normalize();
                Vec3d axis2 = launchDir.normalize().crossProduct(axis1).normalize();

                final double TOTAL_TRAILS = 30.0;
                for (int trails = 0; trails < TOTAL_TRAILS; trails++) {
                    double angle = trails / TOTAL_TRAILS * Math.PI * 2;
                    Vec3d circleOffset = axis1.multiply(Math.cos(angle))
                            .add(axis2.multiply(Math.sin(angle)))
                            .normalize()
                            .multiply(TRAIL_SPEED * Math.sqrt(1 / Math.pow(Math.max(backBlastCosAngle, 0.01), 2.0) - 1.0));
                    Vec3d rand1 = new Vec3d(level.random.nextDouble(), level.random.nextDouble(), level.random.nextDouble()).normalize().multiply(0.2F);
                    Vec3d rand2 = new Vec3d(level.random.nextDouble(), level.random.nextDouble(), level.random.nextDouble()).normalize().multiply(0.2F);

                    Vec3d trailVelocity = launchDir.multiply(-TRAIL_SPEED).add(circleOffset).add(rand1);
                    Vec3d trailVelocity2 = launchDir.multiply(-TRAIL_SPEED).add(circleOffset.multiply(0.6F)).add(rand2);

                    level.addParticle(ParticleTypes.CAMPFIRE_SIGNAL_SMOKE, rpgBackPos.x, rpgBackPos.y, rpgBackPos.z,
                            trailVelocity.x, trailVelocity.y, trailVelocity.z);
                    level.addParticle(ParticleTypes.FLAME, rpgBackPos.x, rpgBackPos.y, rpgBackPos.z,
                            trailVelocity2.x, trailVelocity2.y, trailVelocity2.z);
                }

                if (backblastAreaNotClear)
                    ACECriterionTriggers.KILL_MOB_WITH_BACKBLAST.triggerForEntity(player);

                // Cooldown + misc
                if (AlexsCavesEnriched.CONFIG.rocketLauncher.cooldown > 0)
                    player.getItemCooldownManager().set(itemStack.getItem(),  AlexsCavesEnriched.CONFIG.rocketLauncher.cooldown);
                if (!player.isCreative())
                    ammoStack.decrement(1);
            }
        }
    }

    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return !oldStack.isOf(ACEItemRegistry.ROCKET_LAUNCHER.get()) || !newStack.isOf(ACEItemRegistry.ROCKET_LAUNCHER.get());
    }

    @Override
    public @NotNull Predicate<ItemStack> getProjectiles() {
        return e -> e.getItem() instanceof IRocketItem;
    }

    @Override
    public int getRange() { return 128; }

    @Override
    public @NotNull PersistentProjectileEntity customArrow(@NotNull PersistentProjectileEntity arrow) {
        return new RocketEntity(arrow.getEntityWorld(), (LivingEntity) arrow.getOwner());
    }
}