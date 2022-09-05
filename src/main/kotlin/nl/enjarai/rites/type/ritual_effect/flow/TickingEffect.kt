package nl.enjarai.rites.type.ritual_effect.flow

import net.minecraft.util.math.BlockPos
import nl.enjarai.rites.type.Ritual
import nl.enjarai.rites.type.RitualContext
import nl.enjarai.rites.type.interpreted_value.ConstantNumber
import nl.enjarai.rites.type.interpreted_value.InterpretedNumber
import nl.enjarai.rites.type.ritual_effect.RitualEffect

class TickingEffect : RitualEffect() {
    @FromJson
    private lateinit var effects: List<RitualEffect>
    @FromJson
    private val cooldown: InterpretedNumber = ConstantNumber(1)

    override fun isTicking(): Boolean {
        return true
    }

    override fun getTickCooldown(ctx: RitualContext): Int {
        return cooldown.interpretAsInt(ctx)
    }

    override fun activate(pos: BlockPos, ritual: Ritual, ctx: RitualContext): Boolean {
        if (ctx.checkCooldown(this)) {
            return effects.all {
                it.activate(pos, ritual, ctx)
            }
        }
        return true
    }
}