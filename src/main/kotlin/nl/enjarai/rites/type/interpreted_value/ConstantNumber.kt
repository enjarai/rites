package nl.enjarai.rites.type.interpreted_value

import nl.enjarai.rites.type.RitualContext

class ConstantNumber(val value: Double) : InterpretedNumber {
    constructor(value: Int) : this(value.toDouble())

    override fun interpret(ctx: RitualContext): Double {
        return value
    }
}