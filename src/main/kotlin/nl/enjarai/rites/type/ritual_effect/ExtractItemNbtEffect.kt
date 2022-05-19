package nl.enjarai.rites.type.ritual_effect

import nl.enjarai.rites.type.Ritual
import nl.enjarai.rites.type.RitualContext

class ExtractItemNbtEffect(values: Map<String, Any>) : RitualEffect(values) {
    private val sourceIngredient: Int = getValue(values, "source_ingredient", 1.0).toInt()
    private val nbtPath: String = getValue(values, "nbt_path")
    private val targetVariable: String = getValue(values, "target_variable")

    override fun activate(ritual: Ritual, ctx: RitualContext): Boolean {
        TODO("Not yet implemented")
    }
}