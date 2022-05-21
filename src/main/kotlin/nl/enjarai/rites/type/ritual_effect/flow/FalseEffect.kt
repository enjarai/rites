package nl.enjarai.rites.type.ritual_effect.flow

import nl.enjarai.rites.type.Ritual
import nl.enjarai.rites.type.RitualContext
import nl.enjarai.rites.type.ritual_effect.RitualEffect

class FalseEffect(values: Map<String, Any>) : RitualEffect(values) {
    override fun activate(ritual: Ritual, ctx: RitualContext): Boolean {
        return false
    }
}