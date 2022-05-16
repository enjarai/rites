package nl.enjarai.rites.resource

import com.mojang.brigadier.StringReader
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.command.argument.BlockArgumentParser
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
    override fun processStream(identifier: Identifier, stream: InputStream) {
        val fileReader = BufferedReader(InputStreamReader(stream, StandardCharsets.UTF_8))
        val circleTypeFile = ResourceLoaders.GSON.fromJson(fileReader, CircleTypeFile::class.java) ?:
        throw IllegalArgumentException("File format invalid")

        if (circleTypeFile.layout.size % 2 == 0) {
            throw IllegalArgumentException("Circle type height is an even number")
        }

        for (a in circleTypeFile.layout) {
            if (a.size != circleTypeFile.layout.size) {
                throw IllegalArgumentException("Height and width of circle type are not the same")
            }
        }

        values[identifier] = circleTypeFile.convert()
    }

    class CircleTypeFile : ResourceLoaders.TypeFile<CircleType> {
        val layout = arrayOf<Array<String>>()
        val keys = hashMapOf<String, String>()
        val particle = "minecraft:soul_fire_flame"
        val particle_cycles = 3

        override fun convert(): CircleType {
            return CircleType(
                layout.map { row ->
                    row.map mapRow@{ block ->
                        if (block.isEmpty()) return@mapRow null

                        val stringId = keys[block]
                        val blockArgumentParser = BlockArgumentParser(StringReader(stringId), true).parse(false)

                        if (blockArgumentParser.blockState == null) {
                            if (blockArgumentParser.tagId == null) {
                                throw IllegalArgumentException("Invalid block: $stringId")
                            }
                            return@mapRow TagPredicate(
                                blockArgumentParser.tagId!!,
                                blockArgumentParser.properties
                            )
                        }
                        StatePredicate(
                            blockArgumentParser.blockState!!,
                            blockArgumentParser.blockProperties.keys
                        )
                    }
                },
                Registry.PARTICLE_TYPE.get(Identifier.tryParse(particle)) ?:
                    throw IllegalArgumentException("Invalid particle: $particle"),
                particle_cycles
            )
        }
    }

    class StatePredicate(
        private val state: BlockState,
        private val properties: Set<Property<*>>
    ) :
        Predicate<BlockState> {
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
    ) :
        Predicate<BlockState> {
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