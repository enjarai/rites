package nl.enjarai.rites.type.ritual_effect.flow.loop

import net.minecraft.util.math.BlockPos
import nl.enjarai.rites.type.Ritual
import nl.enjarai.rites.type.RitualContext
import nl.enjarai.rites.type.interpreted_value.InterpretedNumber
import nl.enjarai.rites.type.ritual_effect.RitualEffect
import kotlin.math.max
import kotlin.math.min

class ForAreaEffect : RitualEffect() {
    @FromJson
    private lateinit var effects: List<RitualEffect>
    @FromJson
    private val counter_variable: String = "i"
    @FromJson
    private val offset_variables: List<String> = listOf("x", "y", "z")
    @FromJson
    private lateinit var first_corner_offset: List<InterpretedNumber>
    @FromJson
    private lateinit var second_corner_offset: List<InterpretedNumber>

    override fun activate(pos: BlockPos, ritual: Ritual, ctx: RitualContext): Boolean {
        first_corner_offset.size == 3 || return false
        second_corner_offset.size == 3 || return false
        offset_variables.size == 3 || return false

        val firstCorner = pos.add(
            first_corner_offset[0].interpretAsInt(ctx),
            first_corner_offset[1].interpretAsInt(ctx),
            first_corner_offset[2].interpretAsInt(ctx)
        )
        val secondCorner = pos.add(
            second_corner_offset[0].interpretAsInt(ctx),
            second_corner_offset[1].interpretAsInt(ctx),
            second_corner_offset[2].interpretAsInt(ctx)
        )

        val minX = min(firstCorner.x, secondCorner.x)
        val minY = min(firstCorner.y, secondCorner.y)
        val minZ = min(firstCorner.z, secondCorner.z)
        val maxX = max(firstCorner.x, secondCorner.x)
        val maxY = max(firstCorner.y, secondCorner.y)
        val maxZ = max(firstCorner.z, secondCorner.z)

        var i = 0
        for (x in minX..maxX) {
            for (y in minY..maxY) {
                for (z in minZ..maxZ) {
                    ctx.variables[offset_variables[0]] = x.toDouble()
                    ctx.variables[offset_variables[1]] = y.toDouble()
                    ctx.variables[offset_variables[2]] = z.toDouble()
                    ctx.variables[counter_variable] = i.toDouble()
                    val success = effects.all {
                        it.activate(BlockPos(x, y, z), ritual, ctx)
                    }
                    if (!success) return false
                    i++
                }
            }
        }
        return true
    }
}