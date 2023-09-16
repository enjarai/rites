package nl.enjarai.rites.type.book

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import eu.pb4.sgui.api.elements.BookElementBuilder
import eu.pb4.sgui.api.gui.BookGui
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.registry.Registries
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.ClickEvent
import net.minecraft.text.Style
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import net.minecraft.util.Identifier
import nl.enjarai.rites.type.book.GuideBookPage.Companion.FONT_STYLE
import nl.enjarai.rites.type.book.pages.ChapterPage

class GuideBook(val title: String, val author: String, val pages: List<GuideBookPage>, val polymerItem: Item) {
    @Transient lateinit var id: Identifier

    companion object {
        val CODEC: Codec<GuideBook> = RecordCodecBuilder.create { instance ->
            instance.group(
                Codec.STRING.fieldOf("title").forGetter { it.title },
                Codec.STRING.fieldOf("author").forGetter { it.author },
                GuideBookPage.CODEC.listOf().fieldOf("pages").forGetter { it.pages },
                Registries.ITEM.codec.optionalFieldOf("polymer_item", Items.BOOK).forGetter { it.polymerItem }
            ).apply(instance, ::GuideBook)
        }
    }

    fun open(player: ServerPlayerEntity, stack: ItemStack) {
        val currentPage = stack.orCreateNbt.getInt("CurrentPage")
        val builder = BookElementBuilder.from(Items.WRITTEN_BOOK.defaultStack)
        builder.addPage(*arrayOf(
            Text.empty(),
            Text.empty(),
            Text.literal(title),
            Text.literal("By $author").styled { FONT_STYLE(it).withItalic(true) },
        ))
        val chapterPages = pages.filterIsInstance<ChapterPage>()
        val indexPageCount = (chapterPages.size + 2) / 14 + 1
        val indexEntries = chapterPages
            .map { page ->
                val realIndex = pages.indexOf(page)
                val text = Text.empty().append(Text.literal("${realIndex + indexPageCount + 2}").fillStyle(Style.EMPTY
                    .withColor(Formatting.BLUE)
                    .withUnderline(true)
                    .withClickEvent(ClickEvent(ClickEvent.Action.CHANGE_PAGE, "${realIndex + indexPageCount + 2}")))
                    .append(Text.literal(" ").fillStyle(Style.EMPTY
                        .withColor(Formatting.BLACK)
                        .withUnderline(false)))
                )
                for (i in 0 until page.indent) text.append(Text.literal(" "))
                text.append(page.title)
            }
        val indexPages = mutableListOf<MutableList<Text>>()
        for (i in 0 until indexPageCount) indexPages.add(mutableListOf())
        val page1 = indexPages[0]
        page1.add(Text.translatableWithFallback("rites.guidebook.index", "Index"))
        page1.add(Text.of("-------------------"))
        page1.add(Text.empty())
        indexEntries.forEachIndexed { i, line ->
            val pageI = (i + 3) / 14
            val page = indexPages[pageI]
            page.add(line)
        }
        indexPages.forEach { builder.addPage(*it.toTypedArray()) }
        for (page in pages) {
            builder.addPage(*page.getLines().toTypedArray())
        }
        builder.setTitle(title)
        builder.setAuthor(author)
        builder.signed()
        val gui = Gui(player, builder) { stack.orCreateNbt.putInt("CurrentPage", it) }
        gui.page = currentPage
        gui.open()
    }

    class Gui(player: ServerPlayerEntity, builder: BookElementBuilder, val onSetPage: (Int) -> Unit) : BookGui(player, builder) {
        override fun setPage(page: Int) {
            super.setPage(page)
            onSetPage(page)
        }
    }
}