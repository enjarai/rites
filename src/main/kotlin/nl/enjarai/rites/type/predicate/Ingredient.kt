package nl.enjarai.rites.type.predicate

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.registry.Registries
import net.minecraft.registry.RegistryKeys
import net.minecraft.registry.tag.TagKey
import java.util.*
import java.util.function.Predicate

class Ingredient(item: Optional<Item>, tag: Optional<TagKey<Item>>, any: Optional<Boolean>, val amount: Range, ref: Optional<String>) : Predicate<ItemStack> {
    val item: Item? = item.orElse(null)
    val tag: TagKey<Item>? = tag.orElse(null)
    val any: Boolean? = any.orElse(null)
    val ref: String? = ref.orElse(null)

    companion object {
        val CODEC: Codec<Ingredient> = RecordCodecBuilder.create { instance ->
            instance.group(
                Registries.ITEM.codec.optionalFieldOf("item").forGetter { Optional.ofNullable(it.item) },
                TagKey.codec(RegistryKeys.ITEM).optionalFieldOf("tag").forGetter { Optional.ofNullable(it.tag) },
                Codec.BOOL.optionalFieldOf("any").forGetter { Optional.ofNullable(it.any) },
                Range.CODEC.optionalFieldOf("count", Range(1)).forGetter { it.amount },
                Codec.STRING.optionalFieldOf("ref").forGetter { Optional.ofNullable(it.ref) }
            ).apply(instance, ::Ingredient)
        }
    }

    val hasRef: Boolean
        get() = ref != null

    override fun test(stack: ItemStack): Boolean {
        if (!(ref == null || stack.count >= amount.min)) return false

        return item?.let { stack.isOf(it) } == true ||
                tag?.let { Registries.ITEM.getEntry(stack.item).isIn(it) } == true ||
                any == true
    }
}