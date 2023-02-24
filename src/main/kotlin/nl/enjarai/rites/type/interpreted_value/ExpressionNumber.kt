package nl.enjarai.rites.type.interpreted_value

import nl.enjarai.rites.type.RitualContext
import nl.enjarai.rites.util.Expression

class ExpressionNumber(val inputString: String) : InterpretedNumber {
    val expression = Expression(inputString).build()

    override fun interpret(ctx: RitualContext): Double {
        return expression(ctx.variables)
    }
}