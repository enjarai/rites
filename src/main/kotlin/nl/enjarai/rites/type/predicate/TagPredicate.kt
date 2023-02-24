package nl.enjarai.rites.type.predicate

import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.nbt.NbtElement
import net.minecraft.nbt.NbtOps
import net.minecraft.registry.Registries
import net.minecraft.registry.tag.TagKey

class TagPredicate(private val tag: TagKey<Block>, private val properties: Map<String, String>) : BlockStatePredicate {
    override fun test(blockState: BlockState): Boolean {
        if (!blockState.isIn(tag)) {
            return false
        }
        for ((key, value) in properties) {
            val property = blockState.block.stateManager.getProperty(key) ?: return false
            val comparable = property.parse(value).orElse(null) ?: return false
            if (blockState[property] === comparable) continue
            return false
        }
        return true
    }

    override fun toString(): String {
        return TagKey.codec(Registries.BLOCK.key)
            .encodeStart(NbtOps.INSTANCE, tag)
            .result().map(NbtElement::asString)
            .orElse("#unknown") +
                "[" + properties
                    .map { entry -> stateEntryToString(entry) }
                    .reduce { a, b -> "$a,$b" } + "]"
    }

    private fun stateEntryToString(entry: Map.Entry<String, String>?): String {
        return if (entry == null) {
            "<NULL>"
        } else {
            val property = entry.key
            property + "=" + entry.value
        }
    }
}