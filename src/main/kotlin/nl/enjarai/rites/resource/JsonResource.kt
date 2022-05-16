package nl.enjarai.rites.resource

import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener
import net.minecraft.resource.ResourceManager
import net.minecraft.util.Identifier
import nl.enjarai.rites.RitesMod
import java.io.InputStream

abstract class JsonResource<T>(
    private val resource: String
) : SimpleSynchronousResourceReloadListener {

    private val fileSuffix = ".json"
    private val fileSuffixLength = ".json".length

    val values = hashMapOf<Identifier, T>()
    private val resourceLocation = "rites/$resource"

    override fun getFabricId(): Identifier {
        return RitesMod.id(resource)
    }

    override fun reload(manager: ResourceManager) {
        values.clear()
        for (id in manager.findResources(
            resourceLocation
        ) { path: String -> path.endsWith(fileSuffix) }) {
            try {
                manager.getResource(id).inputStream.use { stream ->
                    val i: Int = resourceLocation.length + 1
                    val idPath: String = id.path
                    val shortId = Identifier(
                        id.namespace,
                        idPath.substring(i, idPath.length - fileSuffixLength)
                    )

                    processStream(shortId, stream)
                }
            } catch (e: Exception) {
                RitesMod.LOGGER.error(
                    "Error occurred while loading resource json $id",
                    e
                )
            }
        }
        RitesMod.LOGGER.info("Loaded ${values.size} $resource")
    }

    abstract fun processStream(identifier: Identifier, stream: InputStream)
}