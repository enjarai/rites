package nl.enjarai.rites.resource

import com.google.gson.GsonBuilder
import net.fabricmc.fabric.api.resource.ResourceManagerHelper
import net.minecraft.resource.ResourceType

object ResourceLoaders {
    val GSON = GsonBuilder()
        .setPrettyPrinting() // Makes the json use new lines instead of being a "one-liner"
        .serializeNulls() // Makes fields with `null` value to be written as well.
        .disableHtmlEscaping() // We'll be able to use custom chars without them being saved differently
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