package nl.enjarai.rites.type.ritual_effect.flow

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.util.math.BlockPos
import nl.enjarai.rites.type.Ritual
import nl.enjarai.rites.type.RitualContext
import nl.enjarai.rites.type.interpreted_value.ConstantNumber
import nl.enjarai.rites.type.interpreted_value.InterpretedNumber
import nl.enjarai.rites.type.ritual_effect.RitualEffect

class TickingEffect(
    val effects: List<RitualEffect>,
    val cooldown: InterpretedNumber,
    val counterVariable: String
) : RitualEffect(CODEC) {
    companion object {
        val CODEC: Codec<TickingEffect> = RecordCodecBuilder.create { instance ->
            instance.group(
                RitualEffect.CODEC.listOf().fieldOf("effects").forGetter { it.effects },
                InterpretedNumber.CODEC.optionalFieldOf("cooldown", ConstantNumber(1)).forGetter { it.cooldown },
                Codec.STRING.optionalFieldOf("counter_variable", "t").forGetter { it.counterVariable }
            ).apply(instance, ::TickingEffect)
        }
    }

    override fun isTicking(): Boolean {
        return true
    }

    override fun getTickCooldown(ctx: RitualContext): Int {
        return cooldown.interpretAsInt(ctx)
    }

    override fun activate(pos: BlockPos, ritual: Ritual, ctx: RitualContext): Boolean {
        if (ctx.checkCooldown(this)) {
            ctx.variables[counterVariable] = (ctx.variables[counterVariable] ?: 0.0) + 1.0
            return effects.all {
                it.activate(pos, ritual, ctx)
            }
        }
        return true
    }
}