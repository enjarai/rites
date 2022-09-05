package nl.enjarai.rites.resource

import com.mojang.brigadier.StringReader
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.command.argument.BlockArgumentParser
import net.minecraft.particle.ParticleTypes
import net.minecraft.state.property.Property
import net.minecraft.tag.TagKey
import net.minecraft.util.Identifier
import net.minecraft.util.registry.Registry
import nl.enjarai.rites.type.CircleType
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets
import java.util.function.Predicate

object CircleTypes : JsonResource<CircleType>("circle_types") {
    val tempValues = hashMapOf<Identifier, CircleTypeFile>()

    override fun processStream(identifier: Identifier, stream: InputStream) {
        val fileReader = BufferedReader(InputStreamReader(stream, StandardCharsets.UTF_8))
        val circleTypeFile = ResourceLoader.GSON.fromJson(fileReader, CircleTypeFile::class.java) ?:
        throw IllegalArgumentException("File format invalid")

        if (circleTypeFile.layout.size % 2 == 0) {
            throw IllegalArgumentException("Circle type height is an even number")
        }

        for (a in circleTypeFile.layout) {
            if (a.size != circleTypeFile.layout.size) {
                throw IllegalArgumentException("Height and width of circle type are not the same")
            }
        }

        tempValues[identifier] = circleTypeFile
        values[identifier] = circleTypeFile.convert()
    }

    override fun after() {
        for ((id, circle) in tempValues) {
            values[id]?.alternatives = circle.alternatives.map {
                values[it] ?: throw IllegalArgumentException("Invalid alternative: $it")
            }
        }
    }

    class CircleTypeFile : ResourceLoader.TypeFile<CircleType> {
        val layout = arrayOf<Array<String>>()
        val keys = hashMapOf<String, String>()
        val particle: Identifier = Registry.PARTICLE_TYPE.getId(ParticleTypes.SOUL_FIRE_FLAME)!!
        val particle_settings = ParticleSettings()
        val alternatives = listOf<Identifier>()

        override fun convert(): CircleType {
            val predicates = keys.mapValues {
                val blockArgumentParser = BlockArgumentParser(StringReader(it.value), true).parse(false)

                if (blockArgumentParser.blockState == null) {
                    if (blockArgumentParser.tagId == null) {
                        throw IllegalArgumentException("Invalid block: ${it.value}")
                    }
                    return@mapValues TagPredicate(
                        blockArgumentParser.tagId!!,
                        blockArgumentParser.properties
                    )
                }
                StatePredicate(
                    blockArgumentParser.blockState!!,
                    blockArgumentParser.blockProperties.keys
                )
            }
            return CircleType(
                layout.map { row ->
                    row.map mapRow@{ block ->
                        if (block.isBlank()) return@mapRow null
                        predicates[block]
                    }
                },
                Registry.PARTICLE_TYPE.get(particle) ?:
                    throw IllegalArgumentException("Invalid particle: $particle"),
                particle_settings
            )
        }
    }

    class ParticleSettings {
        val cycles = 3
        val arm_angle = -0.05
        val arm_speed = 0.2
        val reverse_rotation = false
    }

    class StatePredicate(
        private val state: BlockState,
        private val properties: Set<Property<*>>
    ) : Predicate<BlockState> {

        override fun test(blockState: BlockState): Boolean {
            if (!blockState.isOf(state.block)) {
                return false
            }
            for (property in properties) {
                if (blockState[property] == state[property]) continue
                return false
            }
            return true
        }
    }

    class TagPredicate(
        private val tag: TagKey<Block>,
        private val properties: Map<String, String>
    ) : Predicate<BlockState> {

        override fun test(blockState: BlockState): Boolean {
            if (!blockState.isIn(tag)) {
                return false
            }
            for ((key, value) in properties) {
                val property = blockState.block.stateManager.getProperty(key) ?: return false
                val comparable = property.parse(value).orElse(null) ?: return false
                if (blockState[property] === comparable) continue
                return false
            }
            return true
        }
    }
}