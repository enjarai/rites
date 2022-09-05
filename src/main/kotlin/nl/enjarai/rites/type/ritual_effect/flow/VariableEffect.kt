package nl.enjarai.rites.type.ritual_effect.flow

import net.minecraft.util.math.BlockPos
import nl.enjarai.rites.type.Ritual
import nl.enjarai.rites.type.RitualContext
import nl.enjarai.rites.type.interpreted_value.InterpretedNumber
import nl.enjarai.rites.type.ritual_effect.RitualEffect

class VariableEffect : RitualEffect() {
    @FromJson
    private lateinit var variable: String
    @FromJson
    private lateinit var expression: InterpretedNumber

    override fun activate(pos: BlockPos, ritual: Ritual, ctx: RitualContext): Boolean {
        ctx.variables[variable] = expression.interpret(ctx)
        return true
    }
}