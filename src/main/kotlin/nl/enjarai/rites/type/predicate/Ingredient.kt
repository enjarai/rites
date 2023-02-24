package nl.enjarai.rites.type.predicate

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.registry.Registries
import java.util.*
import java.util.function.Predicate

class Ingredient(val item: Item, val amount: Int, val ref: String?) : Predicate<ItemStack> {
    companion object {
        val CODEC: Codec<Ingredient> = RecordCodecBuilder.create { instance ->
            instance.group(
                Registries.ITEM.codec.fieldOf("item").forGetter { it.item },
                Codec.INT.fieldOf("count").forGetter { it.amount },
                Codec.STRING.optionalFieldOf("ref").forGetter { Optional.ofNullable(it.ref) }
            ).apply(instance, ::Ingredient)
        }
    }

    constructor(item: Item, amount: Int, ref: Optional<String>) : this(item, amount, ref.orElse(null))

    val hasRef: Boolean
        get() = ref != null

    override fun test(stack: ItemStack): Boolean {
        return stack.isOf(item) && (ref == null || stack.count >= amount)
    }
}