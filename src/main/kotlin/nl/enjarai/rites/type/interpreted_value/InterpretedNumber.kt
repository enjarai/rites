package nl.enjarai.rites.type.interpreted_value

import nl.enjarai.rites.type.RitualContext

interface InterpretedNumber : InterpretedValue<Double> {
    fun interpretAsInt(ctx: RitualContext): Int {
        return interpret(ctx).toInt()
    }
}