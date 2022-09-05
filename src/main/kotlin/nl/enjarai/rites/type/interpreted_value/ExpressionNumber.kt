package nl.enjarai.rites.type.interpreted_value

import nl.enjarai.rites.type.RitualContext
import nl.enjarai.rites.util.Expression

class ExpressionNumber(string: String) : InterpretedNumber {
    val expression = Expression(string).build()

    override fun interpret(ctx: RitualContext): Double {
        return expression(ctx.variables)
    }
}