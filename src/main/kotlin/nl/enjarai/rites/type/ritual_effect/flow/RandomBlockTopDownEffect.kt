package nl.enjarai.rites.type.ritual_effect.flow

import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Box
import nl.enjarai.rites.type.Ritual
import nl.enjarai.rites.type.RitualContext
import nl.enjarai.rites.type.ritual_effect.RitualEffect

class RandomBlockTopDownEffect(values: Map<String, Any>) : RitualEffect(values) {
    private val range: Int = getValue(values, "range", 0)
    private val verticalRange: Int = getValue(values, "vertical_range", range)
    private val posOffset: List<Int> = getValue(values, "pos_offset", listOf(0, 0, 0))
    private val effects: List<RitualEffect> = getValue<List<Map<String, Any>>>(values, "effects").map {
        fromMap(it)
    }

    override fun activate(pos: BlockPos, ritual: Ritual, ctx: RitualContext): Boolean {
        // TODO if range 0, range = ctx.range
        val center = pos.add(posOffset[0], posOffset[1], posOffset[2])
        val box = Box(
            center.add(-range, -verticalRange, -range),
            center.add(range, verticalRange, range)
        )

        box.
    }
}