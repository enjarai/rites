package nl.enjarai.rites.resource.serialization

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import com.mojang.brigadier.StringReader
import net.minecraft.command.argument.BlockArgumentParser
import net.minecraft.registry.Registries
import nl.enjarai.rites.type.predicate.BlockStatePredicate
import nl.enjarai.rites.type.predicate.StatePredicate
import nl.enjarai.rites.type.predicate.TagPredicate
import java.lang.reflect.Type

object BlockStatePredicateDeserializer : JsonDeserializer<BlockStatePredicate> {
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): BlockStatePredicate {
        val result = BlockArgumentParser.blockOrTag(Registries.BLOCK.readOnlyWrapper, json.asString, true)

        val predicate = result.mapRight {
            val tag = it.tag.tagKey
            if (tag.isEmpty) {
                throw JsonParseException("Invalid block: ${json.asString}")
            }
            TagPredicate(tag.get(), it.vagueProperties)
        }.mapLeft {
            if (it.blockState == null) {
                throw JsonParseException("Invalid block: ${json.asString}")
            }
            StatePredicate(it.blockState!!, it.properties.keys)
        }

        return predicate.left().orElse(null) ?: predicate.right().orElse(null)
        ?: throw JsonParseException("Invalid block: ${json.asString}")
    }
}