package nl.enjarai.rites.type.ritual_effect.flow.logic

import com.mojang.serialization.Codec
import net.minecraft.util.math.BlockPos
import nl.enjarai.rites.type.Ritual
import nl.enjarai.rites.type.RitualContext
import nl.enjarai.rites.type.ritual_effect.RitualEffect
import nl.enjarai.rites.type.ritual_effect.entity.SummonEntityEffect

class FalseEffect : RitualEffect() {
    companion object {
        val CODEC: Codec<FalseEffect> = Codec.unit(FalseEffect())
    }

    override fun activate(pos: BlockPos, ritual: Ritual, ctx: RitualContext): Boolean {
        return false
    }

    override fun getCodec(): Codec<out RitualEffect> {
        return CODEC
    }
}