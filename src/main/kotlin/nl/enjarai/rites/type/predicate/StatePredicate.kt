package nl.enjarai.rites.type.predicate

import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.registry.Registries
import net.minecraft.state.property.Property
import net.minecraft.util.Identifier

class StatePredicate(id: Identifier, states: Map<String, String>) : BlockStatePredicate(id, states) {
    private lateinit var block: Block
    private lateinit var properties: Map<Property<*>, Comparable<*>>

    override fun finalize() {
        block = Registries.BLOCK[id]
        properties = block.stateManager.properties.map { property ->
            property to property.parse(states[property.name]).orElse(null)
        }.filter { it.second != null }.toMap()
    }

    override fun test(blockState: BlockState): Boolean {
        if (!blockState.isOf(block)) {
            return false
        }
        for (property in properties) {
            if (blockState[property.key] == property.value) continue
            return false
        }
        return true
    }

    override fun toString(): String {
        return id.toString() +
            "[" + states
                .map { entry -> "${entry.key}=${entry.value}" }
                .reduce { a, b -> "$a,$b" } + "]"
    }
}