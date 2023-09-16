package nl.enjarai.rites.type.ritual_effect.item

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.inventory.SingleStackInventory
import net.minecraft.item.ItemStack
import net.minecraft.recipe.RecipeType
import net.minecraft.util.math.BlockPos
import nl.enjarai.rites.type.Ritual
import nl.enjarai.rites.type.RitualContext
import nl.enjarai.rites.type.interpreted_value.InterpretedString
import nl.enjarai.rites.type.ritual_effect.RitualEffect

class SmeltItemEffect(val ref: InterpretedString) : RitualEffect(CODEC) {
    companion object {
        val CODEC: Codec<SmeltItemEffect> = RecordCodecBuilder.create { instance ->
            instance.group(
                InterpretedString.CODEC.fieldOf("ref").forGetter { it.ref }
            ).apply(instance, ::SmeltItemEffect)
        }
    }

    override fun activate(pos: BlockPos, ritual: Ritual, ctx: RitualContext): Boolean {
        val ref = ref.interpret(ctx)
        val item = ctx.addressableItems[ref] ?: return false
        val tempInv = TemporaryInventory(item)
        val recipe = ctx.world.recipeManager.getFirstMatch(RecipeType.SMELTING, tempInv, ctx.world).orElse(null) ?: return false
        ctx.addressableItems[ref] = recipe.getOutput(ctx.world.registryManager).copyWithCount(item.count)
        return true
    }

    class TemporaryInventory(private var itemStack: ItemStack) : SingleStackInventory {
        override fun getStack(slot: Int): ItemStack {
            return itemStack
        }

        override fun removeStack(slot: Int, amount: Int): ItemStack {
            return itemStack.split(amount)
        }

        override fun setStack(slot: Int, stack: ItemStack) {
            itemStack = stack
        }

        override fun markDirty() {
        }

        override fun canPlayerUse(player: PlayerEntity?): Boolean {
            return false
        }
    }
}