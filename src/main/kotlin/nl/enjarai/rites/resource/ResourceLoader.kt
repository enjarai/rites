package nl.enjarai.rites.resource

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import net.fabricmc.fabric.api.resource.ResourceManagerHelper
import net.minecraft.resource.ResourceType
import net.minecraft.util.Identifier
import nl.enjarai.rites.resource.serialization.IdentifierDeserializer
import nl.enjarai.rites.resource.serialization.InterpretedNumberDeserializer
import nl.enjarai.rites.resource.serialization.RitualEffectDeserializer
import nl.enjarai.rites.type.interpreted_value.InterpretedNumber
import nl.enjarai.rites.type.ritual_effect.RitualEffect

object ResourceLoader {
    val GSON: Gson = GsonBuilder()
        .setPrettyPrinting() // Makes the json use new lines instead of being a "one-liner"
        .serializeNulls() // Makes fields with `null` value to be written as well.
        .disableHtmlEscaping() // We'll be able to use custom chars without them being saved differently
        .registerTypeAdapter(Identifier::class.java, IdentifierDeserializer)
        .registerTypeAdapter(InterpretedNumber::class.java, InterpretedNumberDeserializer)
        .registerTypeAdapter(RitualEffect::class.java, RitualEffectDeserializer)
        .create()

    fun register() {
        val manager = ResourceManagerHelper.get(ResourceType.SERVER_DATA)
        manager.registerReloadListener(CircleTypes)
        manager.registerReloadListener(Rituals)
    }

    interface TypeFile<T> {
        fun convert(): T
    }
}