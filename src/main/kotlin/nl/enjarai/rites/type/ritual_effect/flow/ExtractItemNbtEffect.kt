package nl.enjarai.rites.type.ritual_effect.flow

import net.minecraft.util.math.BlockPos
import nl.enjarai.rites.type.Ritual
import nl.enjarai.rites.type.RitualContext
import nl.enjarai.rites.type.ritual_effect.RitualEffect

class ExtractItemNbtEffect(values: Map<String, Any>) : RitualEffect(values) {
    private val sourceIngredient: Int = getValue(values, "source_ingredient", 1.0).toInt()
    private val nbtPath: String = getValue(values, "nbt_path")
    private val targetVariable: String = getValue(values, "target_variable")

    override fun activate(pos: BlockPos, ritual: Ritual, ctx: RitualContext): Boolean {
        TODO("Not yet implemented")
    }
}