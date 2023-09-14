package nl.enjarai.rites.type.book.pages

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.text.HoverEvent
import net.minecraft.text.Style
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import nl.enjarai.rites.resource.Rituals
import nl.enjarai.rites.type.CircleType
import nl.enjarai.rites.type.Ritual
import nl.enjarai.rites.type.book.GuideBookPage

class RitualPage(val ritual: Ritual) : GuideBookPage(CODEC) {
    private val pattern: List<Text> = run {
        val pattern = mutableListOf<Text>()
        for (x in -MAX_SIZE..MAX_SIZE) {
            val line = Text.literal(" ")
            for (z in -MAX_SIZE..MAX_SIZE) {
                if (x == 0 && z == 0) {
                    val style = Style.EMPTY.withFormatting(Formatting.GOLD).withHoverEvent(HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.translatable("rites.tooltip.center")))
                    line.append(Text.literal("[]").fillStyle(style))
                    continue
                }

                var key: CircleType.LayoutKey? = null
                for (circle in ritual.circleTypes) {
                    val offset = MAX_SIZE - (MAX_SIZE - circle.size)
                    key = key ?: circle.layout.elementAtOrNull(z + offset)?.elementAtOrNull(x + offset)
                }
                if (key == null) {
                    line.append(Text.literal("  "))
                    continue
                }
                val color = key.color
                val tooltip = Text.translatable("rites.tooltip.block_state." + key.predicate.toString())
                val style = Style.EMPTY.withColor(color).withHoverEvent(HoverEvent(HoverEvent.Action.SHOW_TEXT, tooltip))
                line.append(Text.literal("[]").fillStyle(style))
            }
            pattern.add(line)
        }
        pattern
    }

    companion object {
        val CODEC: Codec<RitualPage> = RecordCodecBuilder.create { instance ->
            instance.group(
                Rituals.getCodec().fieldOf("ritual").forGetter { it.ritual }
            ).apply(instance, ::RitualPage)
        }
        const val MAX_SIZE = 6
    }

    override fun getLines(): List<Text> {
        return pattern
    }
}