package nl.enjarai.rites.type

import net.minecraft.block.BlockState
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import java.util.function.Predicate

class CircleType(private val layout: List<List<Predicate<BlockState>?>>) {
    val size get() = layout.size / 2

    fun isValid(world: World, pos: BlockPos): Boolean {
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
}