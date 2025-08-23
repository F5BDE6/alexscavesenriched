package net.hellomouse.alexscavesenriched.item;

import com.github.alexmodguy.alexscaves.AlexsCaves;
import com.github.alexmodguy.alexscaves.client.particle.ACParticleRegistry;
import com.github.alexmodguy.alexscaves.server.enchantment.ACEnchantmentRegistry;
import com.github.alexmodguy.alexscaves.server.entity.living.TremorzillaEntity;
import com.github.alexmodguy.alexscaves.server.item.RaygunItem;
import com.github.alexmodguy.alexscaves.server.message.UpdateEffectVisualityEntityMessage;
import com.github.alexmodguy.alexscaves.server.message.UpdateItemTagMessage;
import com.github.alexmodguy.alexscaves.server.misc.ACDamageTypes;
import com.github.alexmodguy.alexscaves.server.misc.ACSoundRegistry;
import com.github.alexmodguy.alexscaves.server.misc.ACTagRegistry;
import com.github.alexmodguy.alexscaves.server.potion.ACEffectRegistry;
import com.github.alexmodguy.alexscaves.server.potion.IrradiatedEffect;
import net.hellomouse.alexscavesenriched.ACEItemRegistry;
import net.hellomouse.alexscavesenriched.AlexsCavesEnriched;
import net.hellomouse.alexscavesenriched.client.ACEClientHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.*;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Consumer;

public class RayGunMk2Item extends RaygunItem {
    private static final int MAX_CHARGE = 2000;
    public static final int COOLDOWN = AlexsCavesEnriched.CONFIG.raygunCooldownTicks;

    public static boolean hasCharge(ItemStack stack) {
        return getCharge(stack) < MAX_CHARGE;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(ACEClientHandler.getItemRenderProperties());
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 12;
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity livingEntity, int i1) {
        super.releaseUsing(stack, level, livingEntity, i1);
        if (level.isClientSide)
            AlexsCaves.sendMSGToServer(new UpdateItemTagMessage(livingEntity.getId(), stack));
        AlexsCaves.PROXY.clearSoundCacheFor(livingEntity);

        if (livingEntity instanceof Player)
            ((Player) livingEntity).getCooldowns().addCooldown(stack.getItem(), COOLDOWN);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level world, LivingEntity user) {
        if (user instanceof Player)
            ((Player) user).getCooldowns().addCooldown(stack.getItem(), COOLDOWN);
        return stack;
    }

    @Override
    public void onUseTick(Level level, LivingEntity living, ItemStack stack, int timeUsing) {
        int i = this.getUseDuration(stack) - timeUsing;
        int realStart = 3;
        float time = i < realStart ? (float)i / (float)realStart : 1.0F;
        float maxDist = 125F * time;
        boolean xRay = stack.getEnchantmentLevel(ACEnchantmentRegistry.X_RAY.get()) > 0;
        HitResult realHitResult = ProjectileUtil.getHitResultOnViewVector(living, Entity::canBeHitByProjectile, maxDist);
        HitResult blockOnlyHitResult = living.pick(maxDist, 0.0F, false);
        Vec3 xRayVec = living.getViewVector(0.0F).scale(maxDist).add(living.getEyePosition());
        Vec3 vec3 = xRay ? xRayVec : blockOnlyHitResult.getLocation();
        Vec3 vec31 = xRay ? xRayVec : blockOnlyHitResult.getLocation();

        if (!hasCharge(stack)) {
            if (level.isClientSide)
                AlexsCaves.sendMSGToServer(new UpdateItemTagMessage(living.getId(), stack));
            living.stopUsingItem();
            level.playSound(null, living.getX(), living.getY(), living.getZ(), ACSoundRegistry.RAYGUN_EMPTY.get(), living.getSoundSource(), 1.0F, 1.0F);
        } else {
            if (level.isClientSide) {
                setRayPosition(stack, vec3.x, vec3.y, vec3.z);
                AlexsCaves.PROXY.playWorldSound(living, (byte)8);
                int efficency = stack.getEnchantmentLevel(ACEnchantmentRegistry.ENERGY_EFFICIENCY.get());
                int divis = 2 + (int)Math.floor((float)efficency * 1.5F);
                if (time >= 1.0F && i % divis == 0 && (!(living instanceof Player) || !((Player) living).isCreative())) {
                    int charge = getCharge(stack);
                    setCharge(stack, Math.min(charge + 5, MAX_CHARGE));
                }
            }

            float deltaX = 0.0F;
            float deltaY = 0.0F;
            float deltaZ = 0.0F;
            boolean gamma = stack.getEnchantmentLevel(ACEnchantmentRegistry.GAMMA_RAY.get()) > 0;
            ParticleOptions particleOptions;
            if (level.random.nextBoolean() && time >= 1.0F) {
                particleOptions = gamma ? ACParticleRegistry.BLUE_RAYGUN_EXPLOSION.get() : ACParticleRegistry.RAYGUN_EXPLOSION.get();
            }
            else {
                particleOptions = gamma ? ACParticleRegistry.BLUE_HAZMAT_BREATHE.get() : ACParticleRegistry.HAZMAT_BREATHE.get();
                deltaX = (level.random.nextFloat() - 0.5F) * 0.2F;
                deltaY = (level.random.nextFloat() - 0.5F) * 0.2F;
                deltaZ = (level.random.nextFloat() - 0.5F) * 0.2F;
            }

            level.addParticle(particleOptions, vec3.x + (double)((level.random.nextFloat() - 0.5F) * 0.45F), vec3.y + 0.20000000298023224, vec3.z + (double)((level.random.nextFloat() - 0.5F) * 0.45F), deltaX, deltaY, deltaZ);
            Direction blastHitDirection = null;
            Vec3 blastHitPos = null;
            AABB hitBox;

            if (xRay) {
                hitBox = living.getBoundingBox().inflate(maxDist);
                float fakeRayTraceProgress = 1.0F;

                for (Vec3 startClip = living.getEyePosition(); fakeRayTraceProgress < maxDist; ++fakeRayTraceProgress) {
                    startClip = startClip.add(living.getViewVector(1.0F));
                    Vec3 endClip = startClip.add(living.getViewVector(1.0F));
                    HitResult attemptedHitResult = ProjectileUtil.getEntityHitResult(level, living, startClip, endClip, hitBox, Entity::canBeHitByProjectile);
                    if (attemptedHitResult != null) {
                        realHitResult = attemptedHitResult;
                        break;
                    }
                }
            } else if (realHitResult instanceof BlockHitResult blockHitResult) {
                BlockPos pos = blockHitResult.getBlockPos();
                BlockState state = level.getBlockState(pos);
                blastHitDirection = blockHitResult.getDirection();
                if (!state.isAir() && state.isFaceSturdy(level, pos, blastHitDirection)) {
                    blastHitPos = (realHitResult).getLocation();
                }
            }

            if (realHitResult instanceof EntityHitResult entityHitResult) {
                blastHitPos = entityHitResult.getEntity().position();
                blastHitDirection = Direction.UP;
                vec31 = blastHitPos;
            }

            if (blastHitPos != null && i % 2 == 0) {
                float offset = 0.05F + level.random.nextFloat() * 0.09F;
                Vec3 particleVec = blastHitPos.add((offset * (float) blastHitDirection.getStepX()), (offset * (float) blastHitDirection.getStepY()), (offset * (float) blastHitDirection.getStepZ()));
                level.addParticle(ACParticleRegistry.RAYGUN_BLAST.get(), particleVec.x, particleVec.y, particleVec.z, blastHitDirection.get3DDataValue(), 0.0, 0.0);
            }

            if (!level.isClientSide && (i - realStart) % 3 == 0) {
                hitBox = new AABB(vec31.add(-1, -1, -1), vec31.add(1, 1, 1));
                int radiationLevel = gamma ? IrradiatedEffect.BLUE_LEVEL : 0;
                for (Entity entity : level.getEntities(living, hitBox, Entity::canBeHitByProjectile)) {
                    if (!entity.is(living) && !entity.isAlliedTo(living) && !living.isAlliedTo(entity) && !living.isPassengerOfSameVehicle(entity)) {
                        boolean flag = entity instanceof TremorzillaEntity || entity.hurt(ACDamageTypes.causeRaygunDamage(level.registryAccess(), living), gamma ? 10F : 6F);
                        if (flag && entity instanceof LivingEntity livingEntity && !livingEntity.getType().is(ACTagRegistry.RESISTS_RADIATION)) {
                            if (livingEntity.addEffect(new MobEffectInstance(ACEffectRegistry.IRRADIATED.get(), 800, radiationLevel)))
                                AlexsCaves.sendMSGToAll(new UpdateEffectVisualityEntityMessage(entity.getId(), living.getId(), gamma ? 4 : 0, 800));
                        }
                    }
                }
            }
        }
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return Math.round(13.0F - (float)getCharge(stack) * 13.0F / MAX_CHARGE);
    }

    @Override
    public int getBarColor(ItemStack stack) {
        float pulseRate = (float)getCharge(stack) / MAX_CHARGE * 2.0F;
        float f = (float) AlexsCaves.PROXY.getPlayerTime() + AlexsCaves.PROXY.getPartialTicks();
        float f1 = 0.5F * (float)(1.0 + Math.sin(f * pulseRate));
        return Mth.hsvToRgb(0.3F, f1 * 0.6F + 0.2F, 1.0F);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
        if (getCharge(stack) != 0) {
            String chargeLeft = "" + (MAX_CHARGE - getCharge(stack));
            tooltip.add(Component.translatable("item.alexscaves.raygun.charge", chargeLeft, MAX_CHARGE).withStyle(ChatFormatting.GREEN));
        }
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return !oldStack.is(ACEItemRegistry.RAYGUN.get()) || !newStack.is(ACEItemRegistry.RAYGUN.get());
    }
}
