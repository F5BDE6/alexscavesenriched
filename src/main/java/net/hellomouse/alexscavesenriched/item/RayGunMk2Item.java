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
import net.hellomouse.alexscavesenriched.client.ClientHandler;
import net.minecraft.block.BlockState;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.*;
import net.minecraft.world.World;
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
        consumer.accept(ClientHandler.getItemRenderProperties());
    }

    @Override
    public int getMaxUseTime(ItemStack stack) {
        return 12;
    }

    @Override
    public void onStoppedUsing(ItemStack stack, World level, LivingEntity livingEntity, int i1) {
        super.onStoppedUsing(stack, level, livingEntity, i1);
        if (level.isClient)
            AlexsCaves.sendMSGToServer(new UpdateItemTagMessage(livingEntity.getId(), stack));
        AlexsCaves.PROXY.clearSoundCacheFor(livingEntity);

        if (livingEntity instanceof PlayerEntity)
            ((PlayerEntity) livingEntity).getItemCooldownManager().set(stack.getItem(), COOLDOWN);
    }

    @Override
    public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
        if (user instanceof PlayerEntity)
            ((PlayerEntity) user).getItemCooldownManager().set(stack.getItem(), COOLDOWN);
        return stack;
    }

    @Override
    public void usageTick(World level, LivingEntity living, ItemStack stack, int timeUsing) {
        int i = this.getMaxUseTime(stack) - timeUsing;
        int realStart = 3;
        float time = i < realStart ? (float)i / (float)realStart : 1.0F;
        float maxDist = 125F * time;
        boolean xRay = stack.getEnchantmentLevel(ACEnchantmentRegistry.X_RAY.get()) > 0;
        HitResult realHitResult = ProjectileUtil.getCollision(living, Entity::canBeHitByProjectile, maxDist);
        HitResult blockOnlyHitResult = living.raycast(maxDist, 0.0F, false);
        Vec3d xRayVec = living.getRotationVec(0.0F).multiply(maxDist).add(living.getEyePos());
        Vec3d vec3 = xRay ? xRayVec : blockOnlyHitResult.getPos();
        Vec3d vec31 = xRay ? xRayVec : blockOnlyHitResult.getPos();

        if (!hasCharge(stack)) {
            if (level.isClient)
                AlexsCaves.sendMSGToServer(new UpdateItemTagMessage(living.getId(), stack));
            living.clearActiveItem();
            level.playSound(null, living.getX(), living.getY(), living.getZ(), ACSoundRegistry.RAYGUN_EMPTY.get(), living.getSoundCategory(), 1.0F, 1.0F);
        } else {
            if (level.isClient) {
                setRayPosition(stack, vec3.x, vec3.y, vec3.z);
                AlexsCaves.PROXY.playWorldSound(living, (byte)8);
                int efficency = stack.getEnchantmentLevel(ACEnchantmentRegistry.ENERGY_EFFICIENCY.get());
                int divis = 2 + (int)Math.floor((float)efficency * 1.5F);
                if (time >= 1.0F && i % divis == 0 && (!(living instanceof PlayerEntity) || !((PlayerEntity)living).isCreative())) {
                    int charge = getCharge(stack);
                    setCharge(stack, Math.min(charge + 5, MAX_CHARGE));
                }
            }

            float deltaX = 0.0F;
            float deltaY = 0.0F;
            float deltaZ = 0.0F;
            boolean gamma = stack.getEnchantmentLevel(ACEnchantmentRegistry.GAMMA_RAY.get()) > 0;
            ParticleEffect particleOptions;
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
            Vec3d blastHitPos = null;
            Box hitBox;

            if (xRay) {
                hitBox = living.getBoundingBox().expand(maxDist);
                float fakeRayTraceProgress = 1.0F;

                for (Vec3d startClip = living.getEyePos(); fakeRayTraceProgress < maxDist; ++fakeRayTraceProgress) {
                    startClip = startClip.add(living.getRotationVec(1.0F));
                    Vec3d endClip = startClip.add(living.getRotationVec(1.0F));
                    HitResult attemptedHitResult = ProjectileUtil.getEntityCollision(level, living, startClip, endClip, hitBox, Entity::canBeHitByProjectile);
                    if (attemptedHitResult != null) {
                        realHitResult = attemptedHitResult;
                        break;
                    }
                }
            } else if (realHitResult instanceof BlockHitResult blockHitResult) {
                BlockPos pos = blockHitResult.getBlockPos();
                BlockState state = level.getBlockState(pos);
                blastHitDirection = blockHitResult.getSide();
                if (!state.isAir() && state.isSideSolidFullSquare(level, pos, blastHitDirection)) {
                    blastHitPos = (realHitResult).getPos();
                }
            }

            if (realHitResult instanceof EntityHitResult entityHitResult) {
                blastHitPos = entityHitResult.getEntity().getPos();
                blastHitDirection = Direction.UP;
                vec31 = blastHitPos;
            }

            if (blastHitPos != null && i % 2 == 0) {
                float offset = 0.05F + level.random.nextFloat() * 0.09F;
                Vec3d particleVec = blastHitPos.add((offset * (float)blastHitDirection.getOffsetX()), (offset * (float)blastHitDirection.getOffsetY()), (offset * (float)blastHitDirection.getOffsetZ()));
                level.addParticle(ACParticleRegistry.RAYGUN_BLAST.get(), particleVec.x, particleVec.y, particleVec.z, blastHitDirection.getId(), 0.0, 0.0);
            }

            if (!level.isClient && (i - realStart) % 3 == 0) {
                hitBox = new Box(vec31.add(-1, -1, -1), vec31.add(1, 1, 1));
                int radiationLevel = gamma ? IrradiatedEffect.BLUE_LEVEL : 0;
                for (Entity entity : level.getOtherEntities(living, hitBox, Entity::canBeHitByProjectile)) {
                    if (!entity.isPartOf(living) && !entity.isTeammate(living) && !living.isTeammate(entity) && !living.isConnectedThroughVehicle(entity)) {
                        boolean flag = entity instanceof TremorzillaEntity || entity.damage(ACDamageTypes.causeRaygunDamage(level.getRegistryManager(), living), gamma ? 10F : 6F);
                        if (flag && entity instanceof LivingEntity livingEntity && !livingEntity.getType().isIn(ACTagRegistry.RESISTS_RADIATION)) {
                            if (livingEntity.addStatusEffect(new StatusEffectInstance(ACEffectRegistry.IRRADIATED.get(), 800, radiationLevel)))
                                AlexsCaves.sendMSGToAll(new UpdateEffectVisualityEntityMessage(entity.getId(), living.getId(), gamma ? 4 : 0, 800));
                        }
                    }
                }
            }
        }
    }

    @Override
    public int getItemBarStep(ItemStack stack) {
        return Math.round(13.0F - (float)getCharge(stack) * 13.0F / MAX_CHARGE);
    }

    @Override
    public int getItemBarColor(ItemStack stack) {
        float pulseRate = (float)getCharge(stack) / MAX_CHARGE * 2.0F;
        float f = (float) AlexsCaves.PROXY.getPlayerTime() + AlexsCaves.PROXY.getPartialTicks();
        float f1 = 0.5F * (float)(1.0 + Math.sin(f * pulseRate));
        return MathHelper.hsvToRgb(0.3F, f1 * 0.6F + 0.2F, 1.0F);
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World worldIn, List<Text> tooltip, TooltipContext flagIn) {
        if (getCharge(stack) != 0) {
            String chargeLeft = "" + (MAX_CHARGE - getCharge(stack));
            tooltip.add(Text.translatable("item.alexscaves.raygun.charge", chargeLeft, MAX_CHARGE).formatted(Formatting.GREEN));
        }
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return !oldStack.isOf(ACEItemRegistry.RAYGUN.get()) || !newStack.isOf(ACEItemRegistry.RAYGUN.get());
    }
}
