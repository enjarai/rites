package nl.enjarai.rites.type.interpreted_value

import nl.enjarai.rites.type.RitualContext
import nl.enjarai.rites.util.StringInterpolator

class InterpolatedString(val inputString: String) : InterpretedString {
    val interpolator = StringInterpolator(inputString).build()

    override fun interpret(ctx: RitualContext): String {
        return interpolator(ctx.variables)
    }
}