package nl.enjarai.rites.resource.serialization

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import nl.enjarai.rites.type.interpreted_value.*
import java.lang.reflect.Type

object InterpretedStringDeserializer : JsonDeserializer<InterpretedString> {

    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): InterpretedString {
        return InterpolatedString(json.asString)
    }
}