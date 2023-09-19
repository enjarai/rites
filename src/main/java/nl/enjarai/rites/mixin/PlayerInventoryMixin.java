package nl.enjarai.rites.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import nl.enjarai.rites.item.ModItems;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(PlayerInventory.class)
public class PlayerInventoryMixin {

    @WrapOperation(
            method = "dropAll",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/item/ItemStack;isEmpty()Z"
            )
    )
    private boolean preventSoulBindingItemsFromDropping(ItemStack stack, Operation<Boolean> original) {
        return original.call(stack) || EnchantmentHelper.getLevel(ModItems.INSTANCE.getSOUL_BINDING(), stack) > 0;
    }

}
