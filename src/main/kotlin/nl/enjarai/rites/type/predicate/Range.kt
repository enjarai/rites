package nl.enjarai.rites.type.predicate

import com.mojang.datafixers.util.Either
import com.mojang.serialization.Codec
import com.mojang.serialization.DataResult
import java.util.function.IntPredicate

class Range(val min: Int, val max: Int) : IntPredicate {
    companion object {
        val CODEC: Codec<Range> = Codec.either(Codec.INT, Codec.STRING).comapFlatMap({ single ->
            if (single.left().isPresent) {
                DataResult.success(Range(single.left().get()))
            } else {
                val range = single.right().get().split("..")
                if (range.size == 2) {
                    val min = range[0].toInt()
                    val max = range[1].toInt()
                    DataResult.success(Range(min, max))
                } else {
                    DataResult.error { "Invalid int range: $single" }
                }
            }
        }, { range ->
            if (range.min == range.max) {
                Either.left(range.min)
            } else {
                Either.right(range.toString())
            }
        })
    }

    constructor(single: Int) : this(single, single)

    override fun test(value: Int): Boolean {
        return value in min..max
    }

    override fun toString(): String {
        return "$min..$max"
    }
}