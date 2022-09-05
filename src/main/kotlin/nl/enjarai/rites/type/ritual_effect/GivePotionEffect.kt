package nl.enjarai.rites.type.ritual_effect

import net.minecraft.entity.LivingEntity
import net.minecraft.entity.effect.StatusEffectInstance
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.util.registry.Registry
import nl.enjarai.rites.type.Ritual
import nl.enjarai.rites.type.RitualContext
import nl.enjarai.rites.type.interpreted_value.ConstantNumber
import nl.enjarai.rites.type.interpreted_value.InterpretedNumber

class GivePotionEffect : RitualEffect() {
    @FromJson
    private lateinit var effect: Identifier
    @FromJson
    private val amplifier: InterpretedNumber = ConstantNumber(0)
    @FromJson
    private val duration: InterpretedNumber = ConstantNumber(20.0)
    @FromJson
    private val show_particles: Boolean = false
    @FromJson
    private val vertical_range: InterpretedNumber = ConstantNumber(.0)

    override fun activate(pos: BlockPos, ritual: Ritual, ctx: RitualContext): Boolean {
        val effect = Registry.STATUS_EFFECT.get(effect) ?: return false
        RitualContext.getEntitiesInRangeByClass(ctx.world, pos, ritual.circleTypes, LivingEntity::class.java, vertical_range.interpret(ctx)).forEach {
            it.addStatusEffect(StatusEffectInstance(
                effect, (duration.interpret(ctx) * 20).toInt(), amplifier.interpretAsInt(ctx),
                !show_particles, show_particles, show_particles
            ))
        }
        return true
    }
}