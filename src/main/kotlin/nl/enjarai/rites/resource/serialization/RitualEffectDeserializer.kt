package nl.enjarai.rites.resource.serialization

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import net.minecraft.util.Identifier
import nl.enjarai.rites.type.ritual_effect.RitualEffect
import java.lang.reflect.Type

object RitualEffectDeserializer : JsonDeserializer<RitualEffect> {
    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): RitualEffect {
        val jsonObject = json.asJsonObject
        val type = context.deserialize<Identifier>(jsonObject.get("type"), Identifier::class.java)
        return RitualEffect.deserialize(type, jsonObject, context)
    }
}