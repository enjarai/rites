package nl.enjarai.rites.type.ritual_effect.item

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.util.math.BlockPos
import nl.enjarai.rites.type.Ritual
import nl.enjarai.rites.type.RitualContext
import nl.enjarai.rites.type.interpreted_value.InterpretedString
import nl.enjarai.rites.type.ritual_effect.RitualEffect

class GetItemCountEffect(
    val ref: InterpretedString,
    val variable: String
) : RitualEffect(CODEC) {
    companion object {
        val CODEC: Codec<GetItemCountEffect> = RecordCodecBuilder.create { instance ->
            instance.group(
                InterpretedString.CODEC.fieldOf("ref").forGetter { it.ref },
                Codec.STRING.fieldOf("variable").forGetter { it.variable }
            ).apply(instance, ::GetItemCountEffect)
        }
    }

    override fun activate(pos: BlockPos, ritual: Ritual, ctx: RitualContext): Boolean {
        val ref = ref.interpret(ctx)
        val item = ctx.addressableItems[ref] ?: return false
        ctx.variables[variable] = item.count.toDouble()
        return true
    }
}
