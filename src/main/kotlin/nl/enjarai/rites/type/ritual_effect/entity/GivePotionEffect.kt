package nl.enjarai.rites.type.ritual_effect.entity

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.effect.StatusEffect
import net.minecraft.entity.effect.StatusEffectInstance
import net.minecraft.registry.Registries
import net.minecraft.util.math.BlockPos
import nl.enjarai.rites.type.Ritual
import nl.enjarai.rites.type.RitualContext
import nl.enjarai.rites.type.interpreted_value.ConstantNumber
import nl.enjarai.rites.type.interpreted_value.InterpretedNumber
import nl.enjarai.rites.type.interpreted_value.InterpretedString
import nl.enjarai.rites.type.ritual_effect.RitualEffect

class GivePotionEffect(
    val selector: InterpretedString,
    val effect: StatusEffect,
    val amplifier: InterpretedNumber,
    val duration: InterpretedNumber,
    val showParticles: Boolean
) : RitualEffect(CODEC) {
    companion object {
        val CODEC: Codec<GivePotionEffect> = RecordCodecBuilder.create { instance ->
            instance.group(
                InterpretedString.CODEC.fieldOf("selector").forGetter { it.selector },
                Registries.STATUS_EFFECT.codec.fieldOf("effect").forGetter { it.effect },
                InterpretedNumber.CODEC.optionalFieldOf("amplifier", ConstantNumber(0))
                    .forGetter { it.amplifier },
                InterpretedNumber.CODEC.optionalFieldOf("duration", ConstantNumber(20.0))
                    .forGetter { it.duration },
                Codec.BOOL.optionalFieldOf("show_particles", false).forGetter { it.showParticles }
            ).apply(instance, ::GivePotionEffect)
        }
    }

    override fun activate(pos: BlockPos, ritual: Ritual, ctx: RitualContext): Boolean {
        selectEntities(ctx, selector.interpret(ctx))?.forEach {
            if (it !is LivingEntity) return@forEach
            it.addStatusEffect(
                StatusEffectInstance(
                    effect,
                    (duration.interpret(ctx) * 20).toInt(),
                    amplifier.interpretAsInt(ctx),
                    !showParticles,
                    showParticles,
                    showParticles
                )
            )
        } ?: return false
        return true
    }
}