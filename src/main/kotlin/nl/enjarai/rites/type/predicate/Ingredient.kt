package nl.enjarai.rites.type.predicate

import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import java.util.function.Predicate

class Ingredient(val item: Item, val amount: Int, val ref: String?) : Predicate<ItemStack> {
    val hasRef: Boolean
        get() = ref != null

    override fun test(stack: ItemStack): Boolean {
        return stack.isOf(item) && (ref == null || stack.count >= amount)
    }
}