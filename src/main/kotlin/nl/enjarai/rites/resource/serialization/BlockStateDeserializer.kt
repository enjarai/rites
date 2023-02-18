package nl.enjarai.rites.resource.serialization

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import net.minecraft.block.BlockState
import net.minecraft.command.argument.BlockArgumentParser
import net.minecraft.registry.Registries
import java.lang.reflect.Type

object BlockStateDeserializer : JsonDeserializer<BlockState> {
    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): BlockState {
        val blockResult = BlockArgumentParser.block(Registries.BLOCK.readOnlyWrapper, json.asString, false)

        return blockResult.blockState ?: throw JsonParseException("Invalid block state: ${json.asString}")
    }
}