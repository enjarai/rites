package nl.enjarai.rites.type.ritual_effect

import nl.enjarai.rites.type.Ritual
import nl.enjarai.rites.util.RitualContext

class TickingEffect(values: Map<String, Any>) : RitualEffect(values) {
    val effects: List<RitualEffect> = getValue<List<Map<String, Any>>>(values, "effects").map {
        fromMap(it)
    }
    val cooldown: Int = getValue(values, "cooldown", 1.0).toInt()

    override fun getTickCooldown(): Int {
        return cooldown
    }

    override fun activate(ritual: Ritual, ctx: RitualContext): Boolean {
        return true
    }

    override fun tick(ritual: Ritual, ctx: RitualContext): Boolean {
        effects.forEach {
            if (!it.activate(ritual, ctx)) return false
        }
        return true
    }
}