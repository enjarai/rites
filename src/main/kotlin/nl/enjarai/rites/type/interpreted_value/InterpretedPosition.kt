package nl.enjarai.rites.type.interpreted_value

import com.mojang.serialization.Codec
import com.mojang.serialization.DataResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import nl.enjarai.rites.type.RitualContext

interface InterpretedPosition : InterpretedValue<Vec3d> {
    companion object {
        val CODEC: Codec<InterpretedPosition> = InterpretedNumber.CODEC.listOf().flatXmap({ list ->
            if (list.size == 3) {
                DataResult.success(ExpressionPosition(list[0], list[1], list[2]))
            } else {
                DataResult.error("Invalid position: '$list', expected 3 expressions")
            }
        }, { interpretedPosition ->
            when (interpretedPosition) {
                is ExpressionPosition -> {
                    DataResult.success(listOf(interpretedPosition.x, interpretedPosition.y, interpretedPosition.z))
                }
                else -> {
                    DataResult.error("Invalid interpreted position: $interpretedPosition")
                }
            }
        })
    }

    fun interpretAsBlockPos(ctx: RitualContext): BlockPos {
        val pos = interpret(ctx)
        return BlockPos(pos)
    }
}