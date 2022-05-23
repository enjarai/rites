package nl.enjarai.rites.type.ritual_effect

import net.minecraft.util.math.BlockPos
import nl.enjarai.rites.type.Ritual
import nl.enjarai.rites.type.RitualContext

class SetBlockEffect(values: Map<String, Any>) : RitualEffect(values) {
    val block
    val offset

    override fun activate(pos: BlockPos, ritual: Ritual, ctx: RitualContext): Boolean {
        TODO("Not yet implemented")
    }
}