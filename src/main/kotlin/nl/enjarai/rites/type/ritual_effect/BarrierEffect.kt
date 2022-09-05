package nl.enjarai.rites.type.ritual_effect

import net.minecraft.util.math.BlockPos
import nl.enjarai.rites.type.Ritual
import nl.enjarai.rites.type.RitualContext

class BarrierEffect : RitualEffect() {
    override fun isTicking(): Boolean {
        return true
    }

    override fun activate(pos: BlockPos, ritual: Ritual, ctx: RitualContext): Boolean {
        TODO("Not yet implemented")
    }
}