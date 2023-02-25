package nl.enjarai.rites.util

import net.minecraft.util.Identifier
import nl.enjarai.rites.type.predicate.BlockStatePredicate
import nl.enjarai.rites.type.predicate.StatePredicate
import nl.enjarai.rites.type.predicate.TagPredicate

class BlockStatePredicateParser(str: String) : Parser(str) {
    fun parse(): BlockStatePredicate {
        nextChar()
        val isTag = eat('#'.code)
        val id = parseIdentifier()
        val states = if (eat('['.code)) {
            val states = mutableMapOf<String, String>()
            while (true) {
                val key = parseWord()
                eat('='.code)
                val value = parseWord()
                states[key] = value
                if (eat(']'.code)) break
                if (!eat(','.code)) throw IllegalArgumentException("Expected ',' or ']'")
            }
            states
        } else {
            emptyMap()
        }

        return if (isTag) {
            TagPredicate(id, states)
        } else {
            StatePredicate(id, states)
        }
    }

    private fun parseIdentifier(): Identifier {
        return Identifier(parseWord())
    }

    private fun parseWord(): String {
        val start = pos
        while (isVariableChar()) nextChar()
        return str.substring(start, pos)
    }
}