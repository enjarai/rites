package nl.enjarai.rites.resource.serialization

import com.mojang.serialization.Codec
import com.mojang.serialization.DataResult
import eu.pb4.placeholders.api.TextParserUtils
import net.minecraft.block.BlockState
import net.minecraft.command.argument.BlockArgumentParser
import net.minecraft.registry.Registries
import net.minecraft.text.Text

object Codecs {
    val BLOCK_STATE: Codec<BlockState> = Codec.STRING.comapFlatMap({ blockStateString ->
        val blockResult = BlockArgumentParser.block(Registries.BLOCK.readOnlyWrapper, blockStateString, false)

        if (blockResult.blockState != null) {
            DataResult.success(blockResult.blockState)
        } else {
            DataResult.error { "Invalid block state: $blockStateString" }
        }
    }, { blockState ->
        blockState.toString()
    })

    // TODO: make serialization work losslessly
    val TEXT_CODEC: Codec<Text> = Codec.STRING.xmap(TextParserUtils::formatText, Text::getString)
}