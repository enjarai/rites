package nl.enjarai.rites.type.predicate

import com.mojang.serialization.Codec
import com.mojang.serialization.DataResult
import net.minecraft.block.BlockState
import net.minecraft.util.Identifier
import nl.enjarai.rites.util.BlockStatePredicateParser
import java.util.function.Predicate

abstract class BlockStatePredicate(val id: Identifier, val states: Map<String, String>) : Predicate<BlockState> {
    companion object {
        val CODEC: Codec<BlockStatePredicate> = Codec.STRING.comapFlatMap({ blockStateString ->
            val parser = BlockStatePredicateParser(blockStateString)
            try {
                DataResult.success(parser.parse())
            } catch (e: Exception) {
                DataResult.error { "Invalid block state: $blockStateString" }
            }
        }, { blockStatePredicate ->
            blockStatePredicate.toString()
        })
    }

    abstract fun finalize()
}