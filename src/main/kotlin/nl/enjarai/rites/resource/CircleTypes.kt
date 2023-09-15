package nl.enjarai.rites.resource

import com.google.gson.JsonElement
import com.google.gson.JsonParser
import com.mojang.serialization.Codec
import com.mojang.serialization.DataResult
import com.mojang.serialization.JsonOps
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.util.Identifier
import nl.enjarai.rites.type.CircleType
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets

object CircleTypes : JsonResource<CircleType>("circle_types") {
    val CODEC: Codec<CircleType> = Identifier.CODEC.comapFlatMap({ id ->
        values[id].let { if (it != null) DataResult.success(it) else DataResult.error { "Unknown circle type: $id" } }
    }, { it.id })
    private val ALTERNATIVES_CODEC: Codec<List<CircleType>> = CODEC.listOf()
        .optionalFieldOf("alternatives", listOf()).codec()
    private val tempValues = hashMapOf<Identifier, JsonElement>()

    override fun processStream(identifier: Identifier, stream: InputStream) {
        val fileReader = BufferedReader(InputStreamReader(stream, StandardCharsets.UTF_8))
        val circleResult = CircleType.CODEC.decode(JsonOps.INSTANCE, JsonParser.parseReader(fileReader))
            .resultOrPartial { throw IllegalArgumentException("Invalid circle type: $it") }.get()
        val circleType = circleResult.first

        if (circleType.layout.size % 2 == 0) {
            throw IllegalArgumentException("Circle type height is an even number")
        }

        for (a in circleType.layout) {
            if (a.size != circleType.layout.size) {
                throw IllegalArgumentException("Height and width of circle type are not the same")
            }
        }

        tempValues[identifier] = circleResult.second
        values[identifier] = circleType
    }

    override fun after() {
        for ((id, jsonElement) in tempValues) {
            values[id]?.alternatives = ALTERNATIVES_CODEC.decode(JsonOps.INSTANCE, jsonElement)
                .resultOrPartial { throw IllegalArgumentException("Invalid alternative: $it") }.get().first
        }
        tempValues.clear()
    }

    override fun finalize() {
        for (circleType in values.values) {
            circleType.finalize()
        }
    }

    class ParticleSettings(
        val cycles: Int = 3,
        val armAngle: Double = -0.05,
        val armSpeed: Double = 0.2,
        val reverseRotation: Boolean = false,
        val options: String = ""
    ) {
        companion object {
            val CODEC: Codec<ParticleSettings> = RecordCodecBuilder.create { instance ->
                instance.group(
                    Codec.INT.optionalFieldOf("cycles", 3).forGetter { it.cycles },
                    Codec.DOUBLE.optionalFieldOf("arm_angle", -0.05).forGetter { it.armAngle },
                    Codec.DOUBLE.optionalFieldOf("arm_speed", 0.2).forGetter { it.armSpeed },
                    Codec.BOOL.optionalFieldOf("reverse_rotation", false).forGetter { it.reverseRotation },
                    Codec.STRING.optionalFieldOf("options", "").forGetter { it.options }
                ).apply(instance, ::ParticleSettings)
            }
        }
    }
}