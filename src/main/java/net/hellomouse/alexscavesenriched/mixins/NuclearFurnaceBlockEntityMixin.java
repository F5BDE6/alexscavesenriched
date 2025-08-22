package net.hellomouse.alexscavesenriched.mixins;

import com.github.alexmodguy.alexscaves.client.particle.ACParticleRegistry;
import com.github.alexmodguy.alexscaves.server.block.blockentity.ACBlockEntityRegistry;
import com.github.alexmodguy.alexscaves.server.block.blockentity.NuclearFurnaceBlockEntity;
import com.github.alexmodguy.alexscaves.server.potion.ACEffectRegistry;
import net.hellomouse.alexscavesenriched.ACEBlockRegistry;
import net.hellomouse.alexscavesenriched.ACEItemRegistry;
import net.hellomouse.alexscavesenriched.ACERecipeRegistry;
import net.hellomouse.alexscavesenriched.AlexsCavesEnriched;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Optional;

@Mixin(NuclearFurnaceBlockEntity.class)
public abstract class NuclearFurnaceBlockEntityMixin extends BaseContainerBlockEntity implements WorldlyContainer {
    @Shadow
    private int fissionTime;
    @Shadow
    private AbstractCookingRecipe currentRecipe;
    @Final
    @Unique
    public final RecipeManager.CachedCheck<Container, ? extends AbstractCookingRecipe> quickCheckAdditional
            = RecipeManager.createCheck(ACERecipeRegistry.NUCLEAR_FURNACE_TYPE.get());
    @Final
    private RecipeManager.CachedCheck<Container, ? extends AbstractCookingRecipe> quickCheck;

    public NuclearFurnaceBlockEntityMixin(BlockPos pos, BlockState state) {
        super(ACBlockEntityRegistry.NUCLEAR_FURNACE.get(), pos, state);
    }

    @Inject(
            method = {"tick"},
            at = @At(
                    value = "INVOKE_ASSIGN",
                    target = "Lcom/github/alexmodguy/alexscaves/server/block/blockentity/NuclearFurnaceBlockEntity;getMaxFissionTime()I",
                    shift = At.Shift.AFTER,
                    remap = false
            ),
            locals = LocalCapture.CAPTURE_FAILSOFT,
            remap = false
    )
    private static void makeEnrichedRodsBurnLonger(Level level, BlockPos blockPos, BlockState state, NuclearFurnaceBlockEntity entity, CallbackInfo ci, boolean flag, ItemStack cookStack, ItemStack rodStack, ItemStack barrelStack, ItemStack cookResult) {
        if (rodStack.is(Item.BY_BLOCK.get(ACEBlockRegistry.ENRICHED_URANIUM_ROD.get())))
            ((NuclearFurnaceBlockEntityMixin) (Object) entity).fissionTime *= (int) AlexsCavesEnriched.CONFIG.enrichedRodFuelMultiplier;
    }

    @Inject(
            method = {"tick"},
            at = @At(
                    value = "HEAD",
                    target = "Lnet/minecraft/recipe/AbstractCookingRecipe;getOutput(Lnet/minecraft/registry/DynamicRegistryManager;)Lnet/minecraft/item/ItemStack;",
                    shift = At.Shift.AFTER
            ),
            locals = LocalCapture.CAPTURE_FAILSOFT,
            remap = false
    )
    private static void createRadiationExtra(Level level, BlockPos blockPos, BlockState state, NuclearFurnaceBlockEntity entity, CallbackInfo ci) {
        var entity2 = ((NuclearFurnaceBlockEntityMixin) (Object) entity);
        // Hardcoded: uranium recipe makes radiation
        final int DURATION = 100;

        if (AlexsCavesEnriched.CONFIG.nuclearFurnaceLeakRadius > 0.1 &&
                entity2.currentRecipe != null &&
                entity2.currentRecipe.getResultItem(level.registryAccess()).is(ACEItemRegistry.ENRICHED_URANIUM.get()) &&
                level.getGameTime() % DURATION == 0
        ) {
            AreaEffectCloud areaEffectCloudEntity = new AreaEffectCloud(level,
                    blockPos.getX() + 1, blockPos.getY() + 1, blockPos.getZ() + 1);
            areaEffectCloudEntity.setParticle(ACParticleRegistry.GAMMAROACH.get());
            areaEffectCloudEntity.setFixedColor(0);
            areaEffectCloudEntity.addEffect(new MobEffectInstance(ACEffectRegistry.IRRADIATED.get(), DURATION * 10, 0));
            areaEffectCloudEntity.setRadius(AlexsCavesEnriched.CONFIG.nuclearFurnaceLeakRadius);
            areaEffectCloudEntity.setDuration(DURATION);
            areaEffectCloudEntity.setRadiusPerTick(-areaEffectCloudEntity.getRadius() / (float) areaEffectCloudEntity.getDuration());
            level.addFreshEntity(areaEffectCloudEntity);
        }
    }

    @Inject(at = @At(value = "HEAD"), method = {"getRecipeFor"}, cancellable = true, remap = false)
    private void getRecipeFor(ItemStack itemStack, CallbackInfoReturnable<Optional<? extends AbstractCookingRecipe>> cir) {
        var container = new SimpleContainer(itemStack);
        var out = this.quickCheck.getRecipeFor(container, this.level);
        if (out.isPresent()) {
            cir.setReturnValue(Optional.of(out.get()));
        } else {
            cir.setReturnValue(this.quickCheckAdditional.getRecipeFor(container, this.level));
        }
    }
}
