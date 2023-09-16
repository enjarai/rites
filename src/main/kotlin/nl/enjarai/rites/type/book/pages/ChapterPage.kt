package nl.enjarai.rites.type.book.pages

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.text.ClickEvent
import net.minecraft.text.Style
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import nl.enjarai.rites.resource.serialization.Codecs
import nl.enjarai.rites.type.book.GuideBookPage

class ChapterPage(override val title: Text, override val indent: Int, val paragraphs: List<Text>) : IndexedBookPage(CODEC) {
    companion object {
        val CODEC: Codec<ChapterPage> = RecordCodecBuilder.create { instance ->
            instance.group(
                Codecs.TEXT_CODEC.fieldOf("title").forGetter { it.title },
                Codec.INT.optionalFieldOf("indent", 0).forGetter { it.indent },
                Codecs.TEXT_CODEC.listOf().optionalFieldOf("paragraphs", listOf()).forGetter { it.paragraphs }
            ).apply(instance, ::ChapterPage)
        }
    }

    override fun getLines(): List<Text> {
        val lines = mutableListOf<Text>()
        val toIndexButton = Text.literal("[<<]").fillStyle(Style.EMPTY
            .withColor(Formatting.BLUE)
            .withUnderline(true)
            .withClickEvent(ClickEvent(ClickEvent.Action.CHANGE_PAGE, "2")))
            .append(Text.literal(" ").fillStyle(Style.EMPTY
                .withColor(Formatting.BLACK)
                .withUnderline(false)))
        lines.add(Text.empty().append(toIndexButton).append(title.copy()))
        lines.add(Text.of("-------------------"))
        lines.add(Text.empty())
        lines.addAll(paragraphs.flatMap { listOf(it.copy().styled(FONT_STYLE), Text.empty()) })
        return lines
    }
}