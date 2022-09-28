package nl.enjarai.rites.type.predicate

import net.minecraft.block.BlockState
import net.minecraft.state.property.Property

class StatePredicate(private val state: BlockState, private val properties: Set<Property<*>>) : BlockStatePredicate {

    override fun test(blockState: BlockState): Boolean {
        if (!blockState.isOf(state.block)) {
            return false
        }
        for (property in properties) {
            if (blockState[property] == state[property]) continue
            return false
        }
        return true
    }
}