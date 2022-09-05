package nl.enjarai.rites.resource.serialization

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import com.mojang.brigadier.StringReader
import net.minecraft.command.argument.BlockArgumentParser
import nl.enjarai.rites.resource.CircleTypes
import java.lang.reflect.Type

object BlockStatePredicateDeserializer : JsonDeserializer<CircleTypes.BlockStatePredicate> {
    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): CircleTypes.BlockStatePredicate {
        val string = json.asString
        val blockArgumentParser = BlockArgumentParser(StringReader(string), true).parse(false)

        if (blockArgumentParser.blockState == null) {
            if (blockArgumentParser.tagId == null) {
                throw JsonParseException("Invalid block: $string")
            }
            return CircleTypes.TagPredicate(
                blockArgumentParser.tagId!!,
                blockArgumentParser.properties
            )
        }
        return CircleTypes.StatePredicate(
            blockArgumentParser.blockState!!,
            blockArgumentParser.blockProperties.keys
        )
    }
}