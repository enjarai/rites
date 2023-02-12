package nl.enjarai.rites.type.ritual_effect.flow.logic

import net.minecraft.util.math.BlockPos
import nl.enjarai.rites.type.Ritual
import nl.enjarai.rites.type.RitualContext
import nl.enjarai.rites.type.ritual_effect.RitualEffect

class AndEffect : RitualEffect() {
    @FromJson
    private lateinit var effects: List<RitualEffect>

    override fun activate(pos: BlockPos, ritual: Ritual, ctx: RitualContext): Boolean {
        return effects.all { it.activate(pos, ritual, ctx) }
    }
}