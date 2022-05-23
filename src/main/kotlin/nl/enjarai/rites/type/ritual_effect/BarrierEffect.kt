package nl.enjarai.rites.type.ritual_effect

import net.minecraft.util.math.BlockPos
import nl.enjarai.rites.type.Ritual
import nl.enjarai.rites.type.RitualContext

class BarrierEffect(values: Map<String, Any>) : RitualEffect(values) {
    override fun isTicking(): Boolean {
        return true
    }

    override fun activate(pos: BlockPos, ritual: Ritual, ctx: RitualContext): Boolean {
        TODO("Not yet implemented")
    }
}