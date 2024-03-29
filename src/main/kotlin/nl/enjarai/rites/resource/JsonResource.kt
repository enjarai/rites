package nl.enjarai.rites.resource

import com.mojang.serialization.Codec
import com.mojang.serialization.DataResult
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener
import net.minecraft.resource.ResourceManager
import net.minecraft.util.Identifier
import nl.enjarai.rites.RitesMod
import java.io.InputStream

abstract class JsonResource<T>(private val resource: String) : SimpleSynchronousResourceReloadListener {
    private val fileSuffix = ".json"
    private val fileSuffixLength = ".json".length

    val values = hashMapOf<Identifier, T>()
    private val resourceLocation = "rites/$resource"

    fun get(id: Identifier): T? {
        return values[id]
    }

    fun getId(value: T): Identifier? {
        return values.entries.firstOrNull { it.value == value }?.key
    }

    override fun getFabricId(): Identifier {
        return RitesMod.id(resource)
    }

    override fun reload(manager: ResourceManager) {
        val startTime = System.currentTimeMillis()
        values.clear()
        for (entry in manager.findResources(resourceLocation) { path -> path.path.endsWith(fileSuffix) }) {
            try {
                entry.value.inputStream.use { stream ->
                    val i: Int = resourceLocation.length + 1
                    val idPath: String = entry.key.path
                    val shortId = Identifier(
                        entry.key.namespace,
                        idPath.substring(i, idPath.length - fileSuffixLength)
                    )

                    processStream(shortId, stream)
                }
            } catch (e: Exception) {
                RitesMod.LOGGER.error("Error occurred while loading resource json $entry", e)
            }
        }
        try {
            after()
        } catch (e: Exception) {
            RitesMod.LOGGER.error("Error occurred while finalizing resource jsons", e)
        }
        RitesMod.LOGGER.info("Loaded ${values.size} $resource, took ${System.currentTimeMillis() - startTime}ms")
    }

    abstract fun processStream(identifier: Identifier, stream: InputStream)

    protected open fun after() {}

    open fun finalize() {}

    fun getCodec(): Codec<T> {
        return Identifier.CODEC.comapFlatMap({
            val value = get(it)
            if (value == null) {
                DataResult.error { "Unknown $resource: $it" }
            } else {
                DataResult.success(value)
            }
        }, ::getId)
    }
}