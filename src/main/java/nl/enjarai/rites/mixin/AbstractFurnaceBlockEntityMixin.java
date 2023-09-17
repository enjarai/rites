package nl.enjarai.rites.mixin;

import net.minecraft.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.recipe.Recipe;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.util.collection.DefaultedList;
import nl.enjarai.rites.item.ModItems;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Map;

@Mixin(AbstractFurnaceBlockEntity.class)
public abstract class AbstractFurnaceBlockEntityMixin {
    private static final Map<Item, Item> INGREDIENT_RECIPES_MAP = Map.of(
            Items.OAK_SAPLING, ModItems.INSTANCE.getEVER_CHANGING_EXTRACT(),
            Items.BIRCH_SAPLING, ModItems.INSTANCE.getEMULSION_OF_CONJOINMENT(),
            Items.SPRUCE_SAPLING, ModItems.INSTANCE.getDISRUPTIVE_OINTMENT(),
            Items.JUNGLE_SAPLING, ModItems.INSTANCE.getSUPPLE_DEW(),
            Items.ACACIA_SAPLING, ModItems.INSTANCE.getMOTIVE_CATALYST(),
            Items.DARK_OAK_SAPLING, ModItems.INSTANCE.getOIL_OF_VITRIOL(),
            Items.CHERRY_SAPLING, ModItems.INSTANCE.getESSENCE_OF_PASSIONS(),

            Items.CRIMSON_FUNGUS, ModItems.INSTANCE.getBLOOD_OF_THE_EARTH(),
            Items.WARPED_FUNGUS, ModItems.INSTANCE.getDEATHLY_PANACEA(),

            Items.CHORUS_FLOWER, ModItems.INSTANCE.getTENEBROUS_MARROW()
    );

    @Inject(
            method = "craftRecipe",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/item/ItemStack;decrement(I)V"
            )
    )
    private static void modifyFuelSlot(DynamicRegistryManager registryManager, @Nullable Recipe<?> recipe, DefaultedList<ItemStack> slots, int count, CallbackInfoReturnable<Boolean> cir) {
        var inputStack = slots.get(0);
        var resultItem = INGREDIENT_RECIPES_MAP.get(inputStack.getItem());
        if (resultItem != null && slots.get(1).isOf(Items.GLASS_BOTTLE) && slots.get(1).getCount() == 1) {
            slots.set(1, resultItem.getDefaultStack());
        }
    }

    @Inject(
            method = "isValid",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/block/entity/AbstractFurnaceBlockEntity;canUseAsFuel(Lnet/minecraft/item/ItemStack;)Z"
            ),
            locals = LocalCapture.CAPTURE_FAILHARD,
            cancellable = true
    )
    private void allowBottlesInFuelSlot(int slot, ItemStack stack, CallbackInfoReturnable<Boolean> cir, ItemStack fuelStack) {
        if (stack.isOf(Items.GLASS_BOTTLE) && !fuelStack.isOf(Items.GLASS_BOTTLE)) {
            cir.setReturnValue(true);
        }
    }
}
