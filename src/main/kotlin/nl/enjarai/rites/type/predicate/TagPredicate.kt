package nl.enjarai.rites.type.predicate

import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.nbt.NbtElement
import net.minecraft.nbt.NbtOps
import net.minecraft.registry.Registries
import net.minecraft.registry.tag.TagKey
import net.minecraft.util.Identifier

class TagPredicate(id: Identifier, states: Map<String, String>) : BlockStatePredicate(id, states) {
    private lateinit var tag: TagKey<Block>

    override fun finalize() {
        tag = Registries.BLOCK.streamTags().filter { it.id == id }.findFirst().orElse(null)
            ?: throw IllegalArgumentException("Invalid block tag: $id")
    }

    override fun test(blockState: BlockState): Boolean {
        if (!blockState.isIn(tag)) {
            return false
        }
        for ((key, value) in states) {
            val property = blockState.block.stateManager.getProperty(key) ?: return false
            val comparable = property.parse(value).orElse(null) ?: return false
            if (blockState[property] === comparable) continue
            return false
        }
        return true
    }

    override fun toString(): String {
        return "#" + id +
                "[" + states
                    .map { entry -> "${entry.key}=${entry.value}" }
                    .reduce { a, b -> "$a,$b" } + "]"
    }
}