package nl.enjarai.rites.type.ritual_effect.flow.logic

import com.mojang.serialization.Codec
import net.minecraft.util.math.BlockPos
import nl.enjarai.rites.type.Ritual
import nl.enjarai.rites.type.RitualContext
import nl.enjarai.rites.type.ritual_effect.RitualEffect

class NotEffect(val effect: RitualEffect) : RitualEffect(CODEC) {
    companion object {
        val CODEC: Codec<NotEffect> = RitualEffect.CODEC
            .xmap({ NotEffect(it) }, { it.effect }).fieldOf("effect").codec()
    }

    override fun activate(pos: BlockPos, ritual: Ritual, ctx: RitualContext): Boolean {
        return !effect.activate(pos, ritual, ctx)
    }
}