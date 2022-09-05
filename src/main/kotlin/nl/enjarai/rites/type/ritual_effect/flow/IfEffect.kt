package nl.enjarai.rites.type.ritual_effect.flow

import net.minecraft.util.math.BlockPos
import nl.enjarai.rites.type.Ritual
import nl.enjarai.rites.type.RitualContext
import nl.enjarai.rites.type.ritual_effect.RitualEffect

class IfEffect : RitualEffect() {
    @FromJson
    private lateinit var condition: RitualEffect
    @FromJson
    private lateinit var then: List<RitualEffect>
    @FromJson
    private val `else`: List<RitualEffect> = listOf()

    override fun activate(pos: BlockPos, ritual: Ritual, ctx: RitualContext): Boolean {
        return if (condition.activate(pos, ritual, ctx)) {
            then.all { it.activate(pos, ritual, ctx) }
        } else {
            `else`.all { it.activate(pos, ritual, ctx) }
        }
    }
}