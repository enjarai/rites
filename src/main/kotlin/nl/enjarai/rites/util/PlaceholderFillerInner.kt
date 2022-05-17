package nl.enjarai.rites.util

import java.util.regex.Pattern

object PlaceholderFillerInner {
    private val PLACEHOLDER_PATTERN = Pattern.compile("\\$[{](?<id>[^}]+)}")

    fun fillInPlaceholders(placeholders: HashMap<String, String>, string: String): String {
        return PLACEHOLDER_PATTERN.matcher(string).replaceAll {
            placeholders[it.group()]
        }
    }
}