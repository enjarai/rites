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
import net.minecraft.util.Identifier

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