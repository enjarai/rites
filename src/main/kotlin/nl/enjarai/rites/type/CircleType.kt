package nl.enjarai.rites.type

import net.minecraft.block.BlockState
import net.minecraft.particle.ParticleType
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import nl.enjarai.rites.resource.CircleTypes
import nl.enjarai.rites.resource.Rituals
import nl.enjarai.rites.util.Visuals
import java.util.function.Predicate

class CircleType(
    private val layout: List<List<Predicate<BlockState>?>>,
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

    fun isValid(world: World, pos: BlockPos): CircleType? {
        if (isSelfValid(world, pos)) return this

        for (circle in alternatives) {
            val result = circle.isValid(world, pos)
            if (result != null) return result
        }

        return null
    }

    fun isSelfValid(world: World, pos: BlockPos): Boolean {
        if (layout.isEmpty()) return false

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