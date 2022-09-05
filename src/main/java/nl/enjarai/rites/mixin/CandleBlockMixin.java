package nl.enjarai.rites.mixin;

import net.minecraft.block.AbstractCandleBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.CandleBlock;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static net.minecraft.block.CandleBlock.CANDLES;

@Mixin(CandleBlock.class)
public abstract class CandleBlockMixin extends AbstractCandleBlock {
    protected CandleBlockMixin(Settings settings) {
        super(settings);
    }

    @Inject(
            method = "getPlacementState",
            at = @At(value = "HEAD"),
            cancellable = true
    )
    private void modifyLitState(ItemPlacementContext ctx, CallbackInfoReturnable<BlockState> cir) {
        if (
            ctx.getHand() == Hand.MAIN_HAND &&
            ctx.getPlayer() != null
        ) {
            var offhandStack = ctx.getPlayer().getOffHandStack();
            var currentState = ctx.getWorld().getBlockState(ctx.getBlockPos());
            FluidState currentFluidState = ctx.getWorld().getFluidState(ctx.getBlockPos());
            boolean waterlogged = currentFluidState.getFluid() == Fluids.WATER;
            if (
                    offhandStack.isOf(Items.FLINT_AND_STEEL) &&
                    (!currentState.isOf((CandleBlock) (Object) this) || !currentState.get(LIT)) &&
                    !waterlogged
            ) {
                var pos = ctx.getBlockPos();
                var world = ctx.getWorld();

                world.playSound(
                        ctx.getPlayer(),
                        pos.getX(), pos.getY(), pos.getZ(),
                        SoundEvents.ITEM_FLINTANDSTEEL_USE,
                        SoundCategory.BLOCKS, 1.0f,
                        world.getRandom().nextFloat() * 0.4f + 0.8f
                );
                offhandStack.damage(1, ctx.getPlayer(), player -> player.sendToolBreakStatus(Hand.OFF_HAND));

                if (currentState.isOf((CandleBlock) (Object) this)) {
                    cir.setReturnValue(currentState.cycle(CANDLES).with(LIT, true));
                    return;
                }
                cir.setReturnValue(super.getPlacementState(ctx).with(LIT, true));
            }
        }
    }
}
