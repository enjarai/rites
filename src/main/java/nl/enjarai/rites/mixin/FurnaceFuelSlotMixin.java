package nl.enjarai.rites.mixin;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.FurnaceFuelSlot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FurnaceFuelSlot.class)
public abstract class FurnaceFuelSlotMixin {
    @Inject(
            method = "canInsert",
            at = @At("HEAD"),
            cancellable = true
    )
    private void allowBottleInFuelSlot(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (stack.isOf(Items.GLASS_BOTTLE)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(
            method = "getMaxItemCount",
            at = @At("HEAD"),
            cancellable = true
    )
    private void setMaxBottleCount(ItemStack stack, CallbackInfoReturnable<Integer> cir) {
        if (stack.isOf(Items.GLASS_BOTTLE)) {
            cir.setReturnValue(1);
        }
    }
}
