package nl.enjarai.rites.type.ritual_effect.item

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.util.math.BlockPos
import nl.enjarai.rites.type.Ritual
import nl.enjarai.rites.type.RitualContext
import nl.enjarai.rites.type.interpreted_value.InterpretedString
import nl.enjarai.rites.type.ritual_effect.RitualEffect

class MergeItemNbtEffect(
    val ref: InterpretedString,
    val nbt: InterpretedString
) : RitualEffect(CODEC) {
    companion object {
        val CODEC: Codec<MergeItemNbtEffect> = RecordCodecBuilder.create { instance ->
            instance.group(
                InterpretedString.CODEC.fieldOf("ref").forGetter { it.ref },
                InterpretedString.CODEC.fieldOf("nbt").forGetter { it.nbt }
            ).apply(instance, ::MergeItemNbtEffect)
        }
    }

    override fun activate(pos: BlockPos, ritual: Ritual, ctx: RitualContext): Boolean {
        val ref = ref.interpret(ctx)
        val item = ctx.addressableItems[ref] ?: return false
        val nbt = nbt.interpretAsNbt(ctx) ?: return false
        if (!nbt.isEmpty) {
            item.orCreateNbt.copyFrom(nbt)
        }
        return true
    }
}