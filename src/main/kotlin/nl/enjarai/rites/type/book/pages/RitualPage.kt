package nl.enjarai.rites.type.book.pages

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.text.Text
import nl.enjarai.rites.resource.Rituals
import nl.enjarai.rites.type.Ritual
import nl.enjarai.rites.type.book.GuideBookPage

class RitualPage(val ritual: Ritual) : GuideBookPage(CODEC) {
    companion object {
        val CODEC: Codec<RitualPage> = RecordCodecBuilder.create { instance ->
            instance.group(
                Rituals.getCodec().fieldOf("ritual").forGetter { it.ritual }
            ).apply(instance, ::RitualPage)
        }
    }

    override fun getLines(): List<Text> {
        return listOf(Text.literal(ritual.id.toString()))
    }
}