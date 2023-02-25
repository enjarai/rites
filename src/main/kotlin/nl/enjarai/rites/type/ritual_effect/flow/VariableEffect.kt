package nl.enjarai.rites.type.ritual_effect.flow

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.util.math.BlockPos
import nl.enjarai.rites.type.Ritual
import nl.enjarai.rites.type.RitualContext
import nl.enjarai.rites.type.interpreted_value.InterpretedNumber
import nl.enjarai.rites.type.ritual_effect.RitualEffect

class VariableEffect(
    val variable: String,
    val expression: InterpretedNumber
) : RitualEffect(CODEC) {
    companion object {
        val CODEC: Codec<VariableEffect> = RecordCodecBuilder.create { instance ->
            instance.group(
                Codec.STRING.fieldOf("variable").forGetter { it.variable },
                InterpretedNumber.CODEC.fieldOf("expression").forGetter { it.expression }
            ).apply(instance, ::VariableEffect)
        }
    }

    override fun activate(pos: BlockPos, ritual: Ritual, ctx: RitualContext): Boolean {
        ctx.variables[variable] = expression.interpret(ctx)
        return true
    }
}