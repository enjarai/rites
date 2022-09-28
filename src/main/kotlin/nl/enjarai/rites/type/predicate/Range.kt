package nl.enjarai.rites.type.predicate

import java.util.function.IntPredicate

class Range(val min: Int, val max: Int) : IntPredicate {
    constructor(single: Int) : this(single, single)

    override fun test(value: Int): Boolean {
        return value in min..max
    }
}