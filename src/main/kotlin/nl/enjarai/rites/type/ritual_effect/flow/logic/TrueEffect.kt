package nl.enjarai.rites.type.ritual_effect.flow.logic

import com.mojang.serialization.Codec
import net.minecraft.util.math.BlockPos
import nl.enjarai.rites.type.Ritual
import nl.enjarai.rites.type.RitualContext
import nl.enjarai.rites.type.ritual_effect.RitualEffect

class TrueEffect : RitualEffect() {
    companion object {
        val CODEC: Codec<TrueEffect> = Codec.unit(TrueEffect())
    }

    override fun activate(pos: BlockPos, ritual: Ritual, ctx: RitualContext): Boolean {
        return true
    }

    override fun getCodec(): Codec<out RitualEffect> {
        return CODEC
    }
}