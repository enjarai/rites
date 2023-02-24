package nl.enjarai.rites.type.ritual_effect.flow.logic

import com.mojang.serialization.Codec
import net.minecraft.util.math.BlockPos
import nl.enjarai.rites.type.Ritual
import nl.enjarai.rites.type.RitualContext
import nl.enjarai.rites.type.ritual_effect.RitualEffect
import nl.enjarai.rites.type.ritual_effect.entity.SummonEntityEffect

class AndEffect(val effects: List<RitualEffect>) : RitualEffect() {
    companion object {
        val CODEC: Codec<AndEffect> = RitualEffect.CODEC.listOf()
            .xmap({ AndEffect(it) }, { it.effects }).fieldOf("effects").codec()
    }

    override fun activate(pos: BlockPos, ritual: Ritual, ctx: RitualContext): Boolean {
        return effects.all { it.activate(pos, ritual, ctx) }
    }

    override fun getCodec(): Codec<out RitualEffect> {
        return CODEC
    }
}