package nl.enjarai.rites.type.ritual_effect.flow.logic

import net.minecraft.util.math.BlockPos
import nl.enjarai.rites.type.Ritual
import nl.enjarai.rites.type.RitualContext
import nl.enjarai.rites.type.ritual_effect.RitualEffect

class NotEffect : RitualEffect() {
    private lateinit var effect: RitualEffect

    override fun activate(pos: BlockPos, ritual: Ritual, ctx: RitualContext): Boolean {
        return !effect.activate(pos, ritual, ctx)
    }
}