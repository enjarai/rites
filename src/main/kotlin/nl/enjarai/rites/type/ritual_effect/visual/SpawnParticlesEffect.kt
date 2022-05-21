package nl.enjarai.rites.type.ritual_effect.visual

import net.minecraft.particle.ParticleEffect
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.Identifier
import net.minecraft.util.math.Vec3d
import net.minecraft.util.registry.Registry
import nl.enjarai.rites.type.Ritual
import nl.enjarai.rites.type.RitualContext
import nl.enjarai.rites.type.ritual_effect.RitualEffect

class SpawnParticlesEffect(values: Map<String, Any>) : RitualEffect(values) {
    private val particle: String = getValue(values, "particle")
    private val posOffset: List<Double> = getValue(values, "pos_offset", listOf(.0, .0, .0))
    private val delta: List<Double> = getValue(values, "delta", listOf(.0, .0, .0))
    private val count: Int = getValue(values, "count", 1.0).toInt()
    private val speed: Double = getValue(values, "speed", .0)

    override fun activate(ritual: Ritual, ctx: RitualContext): Boolean {
        if (posOffset.size != 3) return false
        if (delta.size != 3) return false
        val dPos = Vec3d.ofBottomCenter(ctx.pos)

        (ctx.world as ServerWorld).spawnParticles(
            (Registry.PARTICLE_TYPE.get(
                Identifier.tryParse(ctx.parseVariables(particle))) as? ParticleEffect) ?: return false,
            dPos.getX() + posOffset[0], dPos.getY() + posOffset[1], dPos.getZ() + posOffset[2],
            count, delta[0], delta[1], delta[2], speed
        )

        return true
    }
}