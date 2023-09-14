package nl.enjarai.rites.type.book.pages

import com.mojang.datafixers.util.Either
import com.mojang.serialization.Codec
import net.minecraft.text.Style
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import nl.enjarai.rites.resource.serialization.Codecs.TEXT_CODEC
import nl.enjarai.rites.type.book.GuideBookPage

class TextPage(val paragraphs: List<Text>) : GuideBookPage(CODEC) {
    companion object {
        val CODEC: Codec<TextPage> = Codec.either(
            TEXT_CODEC.listOf().xmap(::TextPage) { it.paragraphs },
            TEXT_CODEC.xmap({ TextPage(listOf(it)) }, { it.paragraphs[0] })
        ).xmap(
            { it.left().orElseGet { it.right().get() } },
            { if (it.paragraphs.size == 1) Either.right(it) else Either.left(it) }
        )
    }

    override fun getLines(): List<Text> {
//        return paragraphs.flatMap { listOf(it, Text.empty()) }
        return paragraphs.flatMap { listOf(it.getWithStyle(Style.EMPTY.withFont(Identifier("uniform")))[0], Text.empty()) }
    }
}