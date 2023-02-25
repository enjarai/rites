package nl.enjarai.rites.type.ritual_effect.flow.loop

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.util.math.BlockPos
import nl.enjarai.rites.type.Ritual
import nl.enjarai.rites.type.RitualContext
import nl.enjarai.rites.type.interpreted_value.InterpretedNumber
import nl.enjarai.rites.type.ritual_effect.RitualEffect

class ForIEffect(
    val effects: List<RitualEffect>,
    val iterations: InterpretedNumber,
    val counterVariable: String
) : RitualEffect(CODEC) {
    companion object {
        val CODEC: Codec<ForIEffect> = RecordCodecBuilder.create { instance ->
            instance.group(
                RitualEffect.CODEC.listOf().fieldOf("effects").forGetter { it.effects },
                InterpretedNumber.CODEC.fieldOf("iterations").forGetter { it.iterations },
                Codec.STRING.optionalFieldOf("counter_variable", "i").forGetter { it.counterVariable }
            ).apply(instance, ::ForIEffect)
        }
    }

    override fun activate(pos: BlockPos, ritual: Ritual, ctx: RitualContext): Boolean {
        val count = iterations.interpretAsInt(ctx)
        for (i in 0 until count) {
            ctx.variables[counterVariable] = i.toDouble()
            val success = effects.all {
                it.activate(pos, ritual, ctx)
            }

            if (!success) return false
        }
        return true
    }
}