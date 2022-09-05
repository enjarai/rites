package nl.enjarai.rites.resource.serialization

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.mojang.brigadier.StringReader
import net.minecraft.block.BlockState
import net.minecraft.command.argument.BlockArgumentParser
import java.lang.reflect.Type

object BlockStateDeserializer : JsonDeserializer<BlockState> {
    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): BlockState {
        val stringReader = StringReader(json.asString)
        val parser = BlockArgumentParser(stringReader, false).parse(false)

        return parser.blockState
    }
}