package nl.enjarai.rites.resource

import com.google.gson.JsonParser
import com.mojang.serialization.JsonOps
import net.minecraft.util.Identifier
import nl.enjarai.rites.type.book.GuideBook
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets

object GuideBooks : JsonResource<GuideBook>("guide_books") {
    override fun processStream(identifier: Identifier, stream: InputStream) {
        val fileReader = BufferedReader(InputStreamReader(stream, StandardCharsets.UTF_8))
        val bookResult = GuideBook.CODEC.decode(JsonOps.INSTANCE, JsonParser.parseReader(fileReader))
            .resultOrPartial { throw IllegalArgumentException("Invalid ritual: $it") }.get()

        values[identifier] = bookResult.first.apply { id = identifier }
    }

    override fun getFabricDependencies(): Collection<Identifier> {
        return setOf(CircleTypes.fabricId, Rituals.fabricId)
    }
}