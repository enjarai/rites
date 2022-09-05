package nl.enjarai.rites.type.interpreted_value

import nl.enjarai.rites.type.RitualContext

interface InterpretedValue<T> {
    fun interpret(ctx: RitualContext): T
}