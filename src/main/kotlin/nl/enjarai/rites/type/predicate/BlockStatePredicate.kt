package nl.enjarai.rites.type.predicate

import com.mojang.serialization.Codec
import com.mojang.serialization.DataResult
import net.minecraft.block.BlockState
import net.minecraft.command.argument.BlockArgumentParser
import net.minecraft.registry.Registries
import java.util.function.Predicate

interface BlockStatePredicate : Predicate<BlockState> {
    companion object {
        val CODEC: Codec<BlockStatePredicate> = Codec.STRING.comapFlatMap({ blockStateString ->
            val result = BlockArgumentParser.blockOrTag(Registries.BLOCK.readOnlyWrapper, blockStateString, true)

            val predicate = result.mapRight {
                val tag = it.tag.tagKey
                if (tag.isEmpty) {
                    DataResult.error("Invalid block: $blockStateString")
                } else {
                    DataResult.success(TagPredicate(tag.get(), it.vagueProperties))
                }
            }.mapLeft {
                if (it.blockState == null) {
                    DataResult.error("Invalid block: $blockStateString")
                } else {
                    DataResult.success(StatePredicate(it.blockState, it.properties.keys))
                }
            }

            predicate.left().orElse(null) ?: predicate.right().orElse(null)
            ?: DataResult.error("Invalid block: $blockStateString")
        }, { blockStatePredicate ->
            blockStatePredicate.toString()
        })
    }
}