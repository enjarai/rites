package nl.enjarai.rites.resource.serialization

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import nl.enjarai.rites.type.interpreted_value.ConstantNumber
import nl.enjarai.rites.type.interpreted_value.ExpressionNumber
import nl.enjarai.rites.type.interpreted_value.InterpretedNumber
import java.lang.reflect.Type

object InterpretedNumberDeserializer : JsonDeserializer<InterpretedNumber> {

    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): InterpretedNumber {
        try {
            return ConstantNumber(json.asDouble)
        } catch (_: ClassCastException) { }

        return ExpressionNumber(json.asString)
    }
}