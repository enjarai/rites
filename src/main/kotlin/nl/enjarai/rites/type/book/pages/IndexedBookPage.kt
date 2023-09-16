package nl.enjarai.rites.type.book.pages

import com.mojang.serialization.Codec
import net.minecraft.text.Text
import nl.enjarai.rites.type.book.GuideBookPage

abstract class IndexedBookPage(codec: Codec<out IndexedBookPage>) : GuideBookPage(codec) {
    abstract val title: Text
    abstract val indent: Int
}