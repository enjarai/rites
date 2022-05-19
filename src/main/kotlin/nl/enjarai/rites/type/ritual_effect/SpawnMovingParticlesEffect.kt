package nl.enjarai.rites.type.ritual_effect

import net.minecraft.particle.ParticleEffect
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.Identifier
import net.minecraft.util.math.Vec3d
import net.minecraft.util.registry.Registry
import nl.enjarai.rites.type.Ritual
import nl.enjarai.rites.util.RitualContext
import nl.enjarai.rites.util.Visuals

class SpawnMovingParticlesEffect(values: Map<String, Any>) : RitualEffect(values) {
    private val particle: String = getValue(values, "particle")
    private val posOffset: List<Double> = getValue(values, "pos_offset", listOf(.0, .0, .0))
    private val delta: List<Double> = getValue(values, "delta", listOf(.0, .0, .0))
    private val directionVector: List<Double> = getValue(values, "direction_vector", listOf(.0, .0, .0))
    private val count: Int = getValue(values, "count", 1.0).toInt()

    override fun activate(ritual: Ritual, ctx: RitualContext): Boolean {
        if (posOffset.size != 3) return false
        if (delta.size != 3) return false
        if (directionVector.size != 3) return false
        val dPos = Vec3d.ofBottomCenter(ctx.pos).add(posOffset[0], posOffset[1], posOffset[2])

        Visuals.movingParticleCloud(
            ctx.world as ServerWorld,
            (Registry.PARTICLE_TYPE.get(
                Identifier.tryParse(ctx.parseVariables(particle))) as? ParticleEffect) ?: return false,
            dPos,
            Vec3d(delta[0], delta[1], delta[2]),
            Vec3d(directionVector[0], directionVector[1], directionVector[2]),
            count
        )

        return true
    }
}