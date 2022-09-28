package nl.enjarai.rites.type.predicate

import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.tag.TagKey

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
}