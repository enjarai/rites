package nl.enjarai.rites.type.ritual_effect

import net.minecraft.entity.LivingEntity
import net.minecraft.entity.effect.StatusEffectInstance
import net.minecraft.util.registry.Registry
import nl.enjarai.rites.type.Ritual
import nl.enjarai.rites.util.RitualContext

class GivePotionContinuousEffect(values: Map<String, Any>) : RitualEffect(values) {
    val effect = Registry.STATUS_EFFECT.get(getIdNullSafe(values["effect"] as? String)) ?:
        throw IllegalArgumentException("Invalid effect/no effect given") // TODO
    val amplifier = values["amplifier"] as? Int ?: 0
    val showParticles = values["show_particles"] as? Boolean ?: true
    val verticalRange = values["vertical_range"] as? Double ?: 0.0

    override fun isTicking(): Boolean {
        return true
    }

    override fun activate(ritual: Ritual, ctx: RitualContext): Boolean {
        return true
    }

    override fun tick(ritual: Ritual, ctx: RitualContext): Boolean {
        ritual.getEntitiesInRangeByClass(ctx.world, ctx.pos, LivingEntity::class.java, verticalRange).forEach {
            it.addStatusEffect(StatusEffectInstance(effect, 60, amplifier, !showParticles, showParticles, showParticles))
        }
        return true
    }
}