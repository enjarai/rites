package nl.enjarai.rites.type.ritual_effect

import net.minecraft.entity.LivingEntity
import net.minecraft.entity.effect.StatusEffectInstance
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.util.registry.Registry
import nl.enjarai.rites.type.Ritual
import nl.enjarai.rites.type.RitualContext

class GivePotionEffect(values: Map<String, Any>) : RitualEffect(values) {
    private val effect: String = getValue(values, "effect")
    private val amplifier: Int = getValue(values, "amplifier", .0).toInt()
    private val duration: Int = (getValue(values, "duration", .0) * 20.0).toInt()
    private val showParticles: Boolean = getValue(values, "show_particles", false)
    private val verticalRange: Double = getValue(values, "vertical_range", .0)

    override fun activate(pos: BlockPos, ritual: Ritual, ctx: RitualContext): Boolean {
        val effect = Registry.STATUS_EFFECT.get(Identifier.tryParse(ctx.parseVariables(effect))) ?: return false
        RitualContext.getEntitiesInRangeByClass(ctx.world, pos, ritual.circleTypes, LivingEntity::class.java, verticalRange).forEach {
            it.addStatusEffect(StatusEffectInstance(effect, duration, amplifier, !showParticles, showParticles, showParticles))
        }
        return true
    }
}