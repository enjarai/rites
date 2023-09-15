package nl.enjarai.rites.type.ritual_effect.flow.logic.comparison

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.util.math.BlockPos
import nl.enjarai.rites.type.Ritual
import nl.enjarai.rites.type.RitualContext
import nl.enjarai.rites.type.interpreted_value.InterpretedNumber
import nl.enjarai.rites.type.ritual_effect.RitualEffect

class LessThanEffect(val left: InterpretedNumber, val right: InterpretedNumber) : RitualEffect(CODEC) {
    companion object {
        val CODEC: Codec<LessThanEffect> = RecordCodecBuilder.create { instance ->
            instance.group(
                InterpretedNumber.CODEC.fieldOf("left").forGetter { it.left },
                InterpretedNumber.CODEC.fieldOf("right").forGetter { it.right }
            ).apply(instance, ::LessThanEffect)
        }
    }

    override fun activate(pos: BlockPos, ritual: Ritual, ctx: RitualContext): Boolean {
        return left.interpret(ctx) < right.interpret(ctx)
    }
}