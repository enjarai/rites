package nl.enjarai.rites.type.ritual_effect

import net.minecraft.entity.LivingEntity
import net.minecraft.entity.effect.StatusEffectInstance
import net.minecraft.util.math.BlockPos
import net.minecraft.util.registry.Registry
import net.minecraft.world.World
import nl.enjarai.rites.type.Ritual

class GivePotionContinuousEffect(values: HashMap<String, Any>) : RitualEffect(values) {
    val effect = Registry.STATUS_EFFECT.get(getIdNullSafe(values["effect"] as? String)) ?:
        throw IllegalArgumentException("Invalid effect/no effect given")
    val amplifier = values["amplifier"] as? Int ?: 0
    val showParticles = values["show_particles"] as? Boolean ?: true
    val verticalRange = values["vertical_range"] as? Int ?: 0

    override fun isContinuous(): Boolean {
        return true
    }

    override fun activate(world: World, pos: BlockPos, ritual: Ritual): Boolean {
        return true
    }

    override fun tick(world: World, pos: BlockPos, ritual: Ritual): Boolean {
        ritual.getEntitiesInRangeByClass(world, pos, LivingEntity::class.java, verticalRange).forEach {
            it.addStatusEffect(StatusEffectInstance(effect, 2, amplifier, !showParticles, showParticles, showParticles))
        }
        return true
    }
}