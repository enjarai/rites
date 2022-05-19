package nl.enjarai.rites.type.ritual_effect

import nl.enjarai.rites.type.Ritual
import nl.enjarai.rites.util.RitualContext

class FalseEffect(values: Map<String, Any>) : RitualEffect(values) {
    override fun activate(ritual: Ritual, ctx: RitualContext): Boolean {
        return false
    }
}