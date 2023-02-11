package nl.enjarai.rites.type.ritual_effect.flow

import net.minecraft.util.math.BlockPos
import nl.enjarai.rites.type.Ritual
import nl.enjarai.rites.type.RitualContext
import nl.enjarai.rites.type.interpreted_value.InterpretedNumber
import nl.enjarai.rites.type.ritual_effect.RitualEffect

class ForIEffect : RitualEffect() {
    @FromJson
    private lateinit var effects: List<RitualEffect>
    @FromJson
    private lateinit var iterations: InterpretedNumber
    @FromJson
    private val counter_variable: String = "i"

    override fun activate(pos: BlockPos, ritual: Ritual, ctx: RitualContext): Boolean {
        val count = iterations.interpretAsInt(ctx)
        for (i in 0 until count) {
            ctx.variables[counter_variable] = i.toDouble()
            val success = effects.all {
                it.activate(pos, ritual, ctx)
            }

            if (!success) return false
        }
        return true
    }
}