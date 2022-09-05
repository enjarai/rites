package nl.enjarai.rites.type.interpreted_value

import nl.enjarai.rites.type.RitualContext

class ExpressionNumber(val expression: String) : InterpretedNumber {
    override fun interpret(ctx: RitualContext): Double {
        TODO("Not yet implemented")
    }
}