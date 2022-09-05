package nl.enjarai.rites.resource.serialization

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import net.minecraft.util.Identifier
import java.lang.reflect.Type

object IdentifierDeserializer : JsonDeserializer<Identifier> {

    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): Identifier {
        return Identifier(json.asString)
    }
}