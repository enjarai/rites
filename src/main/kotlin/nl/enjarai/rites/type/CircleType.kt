package nl.enjarai.rites.type

import net.minecraft.particle.ParticleType
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import nl.enjarai.rites.resource.CircleTypes
import nl.enjarai.rites.util.Visuals

class CircleType(
    private val layout: List<List<CircleTypes.BlockStatePredicate?>>,
    private val particle: ParticleType<*>,
    private val particleSettings: CircleTypes.ParticleSettings
) {
    var alternatives = listOf<CircleType>()
    val size get() = layout.size / 2

    val id: Identifier? get() {
        CircleTypes.values.entries.forEach {
            if (it.value === this) {
                return it.key
            }
        }
        return null
    }

    fun isValid(world: World, pos: BlockPos, ctx: RitualContext?): CircleType? {
        if (isSelfValid(world, pos, ctx)) return this

        for (circle in alternatives) {
            val result = circle.isValid(world, pos, ctx)
            if (result != null) return result
        }

        return null
    }

    /**
     * Checks the validity of this circle only.
     * If the circle might be added, ctx should be set, **otherwise it should always be null**.
     */
    fun isSelfValid(world: World, pos: BlockPos, ctx: RitualContext?): Boolean {
        if (layout.isEmpty() || (ctx != null && !ctx.canAddCircle(size))) return false

        val offset = size
        val offsetPos = pos.add(-offset, 0, -offset)
        for ((x, row) in layout.withIndex()) {
            for ((z, predicate) in row.withIndex()) {
                if (predicate != null && !predicate.test(world.getBlockState(offsetPos.add(x, 0, z)))) {
                    return false
                }
            }
        }

        return true
    }

    fun drawParticleCircle(world: ServerWorld, pos: BlockPos) {
        val cycle = size * 30
        for (i in 1..particleSettings.cycles) {
            Visuals.drawParticleCircleArm(
                world, Vec3d.ofBottomCenter(pos).add(0.0, 0.2, 0.0), cycle,
                ((1.0 / particleSettings.cycles) * i), size.toDouble(), particle,
                particleSettings
            )
        }
    }
}