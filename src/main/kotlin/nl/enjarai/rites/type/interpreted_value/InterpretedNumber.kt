package nl.enjarai.rites.type.interpreted_value

import com.mojang.datafixers.util.Either
import com.mojang.serialization.Codec
import com.mojang.serialization.DataResult
import nl.enjarai.rites.type.RitualContext

interface InterpretedNumber : InterpretedValue<Double> {
    companion object {
        val CODEC: Codec<InterpretedNumber> = Codec.either(Codec.DOUBLE, Codec.STRING).flatXmap({ string ->
            if (string.left().isPresent) {
                try {
                    DataResult.success(ConstantNumber(string.left().get()))
                } catch (e: RuntimeException) {
                    DataResult.error("Invalid expression: '$string', ${e.message}")
                }
            } else {
                DataResult.success(ExpressionNumber(string.right().get()))
            }
        }, { interpretedNumber ->
            when (interpretedNumber) {
                is ConstantNumber -> {
                    DataResult.success(Either.left(interpretedNumber.value))
                }
                is ExpressionNumber -> {
                    DataResult.success(Either.right(interpretedNumber.inputString))
                }
                else -> {
                    DataResult.error("Invalid interpreted number: $interpretedNumber")
                }
            }
        })
    }

    fun interpretAsInt(ctx: RitualContext): Int {
        return interpret(ctx).toInt()
    }
}