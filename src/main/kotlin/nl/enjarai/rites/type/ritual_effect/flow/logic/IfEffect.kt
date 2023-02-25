package nl.enjarai.rites.type.ritual_effect.flow.logic

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.util.math.BlockPos
import nl.enjarai.rites.type.Ritual
import nl.enjarai.rites.type.RitualContext
import nl.enjarai.rites.type.ritual_effect.RitualEffect

class IfEffect(
    val condition: RitualEffect,
    val then: List<RitualEffect>,
    val `else`: List<RitualEffect>
) : RitualEffect(CODEC) {
    companion object {
        val CODEC: Codec<IfEffect> = RecordCodecBuilder.create { instance ->
            instance.group(
                RitualEffect.CODEC.fieldOf("condition").forGetter { it.condition },
                RitualEffect.CODEC.listOf().fieldOf("then").forGetter { it.then },
                RitualEffect.CODEC.listOf().optionalFieldOf("else", listOf()).forGetter { it.`else` }
            ).apply(instance, ::IfEffect)
        }
    }

    override fun activate(pos: BlockPos, ritual: Ritual, ctx: RitualContext): Boolean {
        return if (condition.activate(pos, ritual, ctx)) {
            then.all { it.activate(pos, ritual, ctx) }
        } else {
            `else`.all { it.activate(pos, ritual, ctx) }
        }
    }
}