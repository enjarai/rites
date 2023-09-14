package nl.enjarai.rites.type.book

import com.mojang.datafixers.util.Either
import com.mojang.serialization.Codec
import com.mojang.serialization.Lifecycle
import eu.pb4.placeholders.api.TextParserUtils
import net.minecraft.registry.Registry
import net.minecraft.registry.RegistryKey
import net.minecraft.registry.SimpleRegistry
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import nl.enjarai.rites.RitesMod
import nl.enjarai.rites.type.book.pages.RitualPage
import nl.enjarai.rites.type.book.pages.TextPage

abstract class GuideBookPage(val codec: Codec<out GuideBookPage>) {
    val id: Identifier? get() {
        return REGISTRY.getId(codec)
    }

    companion object {
        val REGISTRY = SimpleRegistry<Codec<out GuideBookPage>>(
            RegistryKey.ofRegistry(RitesMod.id("guide_book_pages")),
            Lifecycle.stable()
        )
        val CODEC: Codec<GuideBookPage> = Codec.either(
            TextPage.CODEC,
            REGISTRY.codec.dispatch(
                "type",
                { it.codec },
                { it }
            )
        ).xmap(
            { it.right().orElseGet { it.left().get() } },
            { if (it is TextPage) Either.left(it) else Either.right(it) }
        )

        fun registerAll() {
            Registry.register(REGISTRY, RitesMod.id("text"), TextPage.CODEC)
            Registry.register(REGISTRY, RitesMod.id("ritual"), RitualPage.CODEC)
        }
    }

    abstract fun getLines(): List<Text>
}