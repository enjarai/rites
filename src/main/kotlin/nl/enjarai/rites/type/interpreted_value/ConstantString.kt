package nl.enjarai.rites.type.interpreted_value

import nl.enjarai.rites.type.RitualContext

class ConstantString(val value: String) : InterpretedString {
    override fun interpret(ctx: RitualContext): String {
        return value
    }
}