package nl.enjarai.rites.resource.serialization

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import net.minecraft.registry.Registries
import nl.enjarai.rites.type.predicate.Ingredient
import java.lang.reflect.Type

object IngredientDeserializer : JsonDeserializer<Ingredient> {
    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): Ingredient {
        val jsonObject = json.asJsonObject
        val item =
            Registries.ITEM.getOrEmpty(IdentifierDeserializer.deserialize(jsonObject.get("item"), typeOfT, context))
                .orElse(null) ?: throw JsonParseException("Invalid item: ${jsonObject.get("item")}")
        val count = jsonObject.get("count")?.asInt ?: 1
        val ref = jsonObject.get("ref")?.asString

        if (count < 1) throw JsonParseException("Invalid count: $count")
        if (ref != null && count > item.maxCount) throw JsonParseException("Invalid count: $count, as this ingredient has a ref, count can't be higher that the maximum stack size of $item: ${item.maxCount}")

        return Ingredient(item, count, ref)
    }
}