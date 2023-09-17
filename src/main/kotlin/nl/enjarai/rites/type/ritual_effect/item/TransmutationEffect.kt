package nl.enjarai.rites.type.ritual_effect.item

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.item.Item
import net.minecraft.registry.Registries
import net.minecraft.util.math.BlockPos
import nl.enjarai.rites.type.Ritual
import nl.enjarai.rites.type.RitualContext
import nl.enjarai.rites.type.interpreted_value.InterpretedString
import nl.enjarai.rites.type.ritual_effect.RitualEffect

class TransmutationEffect(val ref: InterpretedString, val transmutations: Map<Item, Item>) : RitualEffect(CODEC) {
    companion object {
        val CODEC: Codec<TransmutationEffect> = RecordCodecBuilder.create { instance ->
            instance.group(
                InterpretedString.CODEC.fieldOf("ref").forGetter { it.ref },
                Codec.unboundedMap(Registries.ITEM.codec, Registries.ITEM.codec).fieldOf("transmutations").forGetter { it.transmutations }
            ).apply(instance, ::TransmutationEffect)
        }
    }

    override fun activate(pos: BlockPos, ritual: Ritual, ctx: RitualContext): Boolean {
        val ref = ref.interpret(ctx)
        val item = ctx.addressableItems[ref] ?: return false
        val newItem = transmutations[item.item]?.defaultStack?.copyWithCount(item.count) ?: return false
        if (item.hasNbt()) {
            newItem.nbt = item.nbt
        }
        ctx.addressableItems[ref] = newItem
        return true
    }
}