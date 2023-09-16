package nl.enjarai.rites.type.book.pages

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.text.*
import net.minecraft.util.Formatting
import nl.enjarai.rites.resource.Rituals
import nl.enjarai.rites.resource.serialization.Codecs
import nl.enjarai.rites.type.CircleType
import nl.enjarai.rites.type.Ritual
import nl.enjarai.rites.type.book.GuideBookPage

class RitualDetailsPage(val ritual: Ritual, override val title: Text, override val indent: Int) : IndexedBookPage(CODEC) {
    companion object {
        val CODEC: Codec<RitualDetailsPage> = RecordCodecBuilder.create { instance ->
            instance.group(
                Rituals.getCodec().fieldOf("ritual").forGetter { it.ritual },
                Codecs.TEXT_CODEC.fieldOf("title").forGetter { it.title },
                Codec.INT.optionalFieldOf("indent", 0).forGetter { it.indent }
            ).apply(instance, ::RitualDetailsPage)
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

        lines.add(Text.literal("Ingredients:").styled(FONT_STYLE))
        for (ingredient in ritual.ingredients) {
            val name = if (ingredient.any == true) {
                Text.of("Any Item")
            } else if (ingredient.item != null) {
                ingredient.item.name // .copy().fillStyle(Style.EMPTY
//                    .withHoverEvent(HoverEvent(HoverEvent.Action.SHOW_ITEM,
//                        HoverEvent.ItemStackContent(ingredient.item.defaultStack))))
            } else if (ingredient.tag != null) {
                Text.translatable(ingredient.tag.id.toTranslationKey("tag"))
            } else {
                Text.of("Unknown Ingredient")
            }
            lines.add(Text.literal(" - ${ingredient.amount}x ").append(name).styled(FONT_STYLE))
        }
        lines.add(Text.empty())

        lines.add(Text.literal("Circle layout on the next page.").styled(FONT_STYLE))

        return lines
    }
}