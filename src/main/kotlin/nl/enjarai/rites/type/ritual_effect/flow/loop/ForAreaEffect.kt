package nl.enjarai.rites.type.ritual_effect.flow.loop

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.util.math.BlockPos
import nl.enjarai.rites.type.Ritual
import nl.enjarai.rites.type.RitualContext
import nl.enjarai.rites.type.interpreted_value.InterpretedVec3
import nl.enjarai.rites.type.ritual_effect.RitualEffect
import kotlin.math.max
import kotlin.math.min

class ForAreaEffect(
    val effects: List<RitualEffect>,
    val counterVariable: String,
    val offsetVariables: List<String>,
    val firstCornerOffset: InterpretedVec3,
    val secondCornerOffset: InterpretedVec3
) : RitualEffect(CODEC) {
    companion object {
        val CODEC: Codec<ForAreaEffect> = RecordCodecBuilder.create { instance ->
            instance.group(
                RitualEffect.CODEC.listOf().fieldOf("effects").forGetter { it.effects },
                Codec.STRING.optionalFieldOf("counter_variable", "i").forGetter { it.counterVariable },
                Codec.STRING.listOf().optionalFieldOf("offset_variables", listOf("x", "y", "z"))
                    .forGetter { it.offsetVariables },
                InterpretedVec3.CODEC.fieldOf("first_corner_offset").forGetter { it.firstCornerOffset },
                InterpretedVec3.CODEC.fieldOf("second_corner_offset").forGetter { it.secondCornerOffset }
            ).apply(instance, ::ForAreaEffect)
        }
    }

    override fun activate(pos: BlockPos, ritual: Ritual, ctx: RitualContext): Boolean {
        offsetVariables.size == 3 || return false

        val firstCorner = pos.add(firstCornerOffset.interpretAsBlockPos(ctx))
        val secondCorner = pos.add(secondCornerOffset.interpretAsBlockPos(ctx))

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
                    ctx.variables[offsetVariables[0]] = x.toDouble()
                    ctx.variables[offsetVariables[1]] = y.toDouble()
                    ctx.variables[offsetVariables[2]] = z.toDouble()
                    ctx.variables[counterVariable] = i.toDouble()
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