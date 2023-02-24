package nl.enjarai.rites.resource.serialization

import com.mojang.serialization.Codec
import com.mojang.serialization.DataResult
import net.minecraft.block.BlockState
import net.minecraft.command.argument.BlockArgumentParser
import net.minecraft.registry.Registries

object Codecs {
    val BLOCK_STATE: Codec<BlockState> = Codec.STRING.comapFlatMap({ blockStateString ->
        val blockResult = BlockArgumentParser.block(Registries.BLOCK.readOnlyWrapper, blockStateString, false)

        if (blockResult.blockState != null) {
            DataResult.success(blockResult.blockState)
        } else {
            DataResult.error("Invalid block state: $blockStateString")
        }
    }, { blockState ->
        blockState.toString()
    })
}