package net.hellomouse.alexscavesenriched.item;

import com.github.alexmodguy.alexscaves.server.item.ACItemRegistry;
import com.github.alexmodguy.alexscaves.server.potion.ACEffectRegistry;
import net.hellomouse.alexscavesenriched.AlexsCavesEnriched;
import net.hellomouse.alexscavesenriched.entity.NeutronExplosionEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class GammaFlashlightItem extends Item {
    public GammaFlashlightItem() {
        super(new Properties()
                .stacksTo(1)
                .rarity(ACItemRegistry.RARITY_NUCLEAR));
    }
    public static boolean isOn(ItemStack itemStack) {
        return itemStack.getTag() != null && itemStack.getTag().contains("on");
    }
    @Override
    public void inventoryTick(ItemStack itemStack, Level level, Entity entity, int i, boolean b) {
        if (!level.isClientSide && isOn(itemStack)) {
            boolean irradiate = true;
            if (entity instanceof Player player)
                irradiate = player.getInventory().getSelected() == itemStack || player.getInventory().offhand.get(0) == itemStack;
            if (irradiate) {
                Vec3 launchPos = entity.getEyePosition();
                Vec3 launchDir = entity.getViewVector(1.0F);

                // Mutate blocks
                var hit = level.clip(new ClipContext(
                        launchPos,
                        launchPos.add(launchDir.add(new Vec3(
                                level.random.nextFloat() - 0.5,
                                level.random.nextFloat() - 0.5,
                                level.random.nextFloat() - 0.5
                                ).scale(AlexsCavesEnriched.CONFIG.gammaFlashlightConfig.spread))
                                .scale(AlexsCavesEnriched.CONFIG.gammaFlashlightConfig.range)),
                        ClipContext.Block.OUTLINE,
                        ClipContext.Fluid.NONE,
                        entity));
                NeutronExplosionEntity.tryTransmuteBlock(entity.getCommandSenderWorld(), hit.getBlockPos());

                // Irradiate mobs
                AABB bashBox = new AABB(new BlockPos((int) launchPos.x(), (int) launchPos.y(), (int) launchPos.z()))
                        .expandTowards(launchDir.scale(AlexsCavesEnriched.CONFIG.gammaFlashlightConfig.range));
                for (LivingEntity otherEntity : level.getEntitiesOfClass(LivingEntity.class, bashBox)) {
                    if (otherEntity == entity || !otherEntity.hasLineOfSight(entity))
                        continue;
                    otherEntity.addEffect(new MobEffectInstance(ACEffectRegistry.IRRADIATED.get(),
                    200, 0,
                    false, false, true));
                }
            }
        }
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);
        CompoundTag tag = itemStack.getOrCreateTag();

        if (isOn(itemStack)) {
            level.playSound(null, player.blockPosition(), SoundEvents.WOODEN_BUTTON_CLICK_ON, SoundSource.PLAYERS, 1.0F, 1.0F);
            tag.remove("on");
            itemStack.setTag(tag);
        } else {
            level.playSound(null, player.blockPosition(), SoundEvents.WOODEN_BUTTON_CLICK_OFF, SoundSource.PLAYERS, 1.0F, 1.0F);
            tag.putBoolean("on", true);
            itemStack.setTag(tag);
        }
        return InteractionResultHolder.success(itemStack);
    }
}
