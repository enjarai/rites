package nl.enjarai.rites.resource

import net.minecraft.util.Identifier
import net.minecraft.util.registry.Registry
import nl.enjarai.rites.type.Ritual
import nl.enjarai.rites.type.ritual_effect.RitualEffect
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets

object Rituals : JsonResource<Ritual>("rituals") {
    override fun processStream(identifier: Identifier, stream: InputStream) {
        val fileReader = BufferedReader(InputStreamReader(stream, StandardCharsets.UTF_8))
        val ritualFile = ResourceLoaders.GSON.fromJson(fileReader, RitualFile::class.java) ?:
            throw IllegalArgumentException("File format invalid")

        values[identifier] = ritualFile.convert()
    }

    class RitualFile : ResourceLoaders.TypeFile<Ritual> {
        val circles = arrayOf<String>()
        val ingredients = hashMapOf<String, Int>()
        val effects = arrayOf<HashMap<String, Any>>()

        override fun convert(): Ritual {
            return Ritual(
                circles.map {
                    CircleTypes.values[Identifier.tryParse(it)] ?:
                        throw IllegalArgumentException("Invalid circle type: $it")
                },
                ingredients.mapKeys {
                    Registry.ITEM.get(Identifier.tryParse(it.key)) ?:
                        throw IllegalArgumentException("Invalid ingredient: ${it.key}")
                },
                effects.map {
                    RitualEffect.fromHashMap(it) ?:
                        throw IllegalArgumentException("Invalid ritual type: ${it["type"]}")
                }
            )
        }
    }
}