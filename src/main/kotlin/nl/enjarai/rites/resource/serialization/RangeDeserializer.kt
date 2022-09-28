package nl.enjarai.rites.resource.serialization

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import nl.enjarai.rites.type.predicate.Range
import java.lang.reflect.Type

object RangeDeserializer : JsonDeserializer<Range> {
    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): Range {
        try {
            return Range(json.asInt)
        } catch (_: NumberFormatException) { }

        // parse a range in the format "min..max"
        val range = json.asString.split("..")
        if (range.size == 2) {
            val min = range[0].toInt()
            val max = range[1].toInt()
            return Range(min, max)
        }

        throw IllegalArgumentException("Invalid int range: $json")
    }
}