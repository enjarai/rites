package nl.enjarai.rites.type.ritual_effect.item

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.util.math.BlockPos
import nl.enjarai.rites.type.Ritual
import nl.enjarai.rites.type.RitualContext
import nl.enjarai.rites.type.interpreted_value.InterpretedNumber
import nl.enjarai.rites.type.interpreted_value.InterpretedString
import nl.enjarai.rites.type.ritual_effect.RitualEffect

class SetItemCountEffect(
    val ref: InterpretedString,
    val count: InterpretedNumber
) : RitualEffect(CODEC) {
    companion object {
        val CODEC: Codec<SetItemCountEffect> = RecordCodecBuilder.create { instance ->
            instance.group(
                InterpretedString.CODEC.fieldOf("ref").forGetter { it.ref },
                InterpretedNumber.CODEC.fieldOf("count").forGetter { it.count }
            ).apply(instance, ::SetItemCountEffect)
        }
    }

    override fun activate(pos: BlockPos, ritual: Ritual, ctx: RitualContext): Boolean {
        val ref = ref.interpret(ctx)
        val item = ctx.addressableItems[ref] ?: return false
        item.count = count.interpretAsInt(ctx)
        return true
    }
}
