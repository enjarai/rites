package nl.enjarai.rites.util

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.*

class StringInterpolator(str: String) : Parser(str) {
    companion object {
        // Magical number formatter
        private val df = DecimalFormat("0", DecimalFormatSymbols.getInstance(Locale.ENGLISH));
        init {
            df.maximumFractionDigits = 340
        }
    }

    fun build(): (Map<String, Double>) -> String {
        nextChar()
        val x = parse()
        if (pos < str.length) throw RuntimeException("Unexpected: " + ch.toChar())
        return x
    }

    private fun parse(): (Map<String, Double>) -> String {
        var s: (Map<String, Double>) -> String = { "" }
        while (true) {
            val startPos = pos
            while (ch != '$'.code && ch != -1) nextChar()
            val a = s
            val b = str.substring(startPos, pos)
            s = { a(it) + b }
            if (eat('$'.code)) {
                if (eat('{'.code)) {
                    val aa = s
                    val bb = parseExpression()
                    if (!eat('}'.code)) throw RuntimeException("Expected }")
                    s = { aa(it) + df.format(bb(it)) }
                } else {
                    val startVarPos = pos
                    while (isVariableChar()) nextChar()
                    val variableName = str.substring(startVarPos, pos)
                    val aa = s
                    s = { aa(it) + df.format(it[variableName]) }
                }
            } else {
                return s
            }
        }
    }

    private fun parseExpression(): (Map<String, Double>) -> Double {
        val startPos = pos
        while (ch != '}'.code && ch != -1) nextChar()
        val expression = str.substring(startPos, pos)
        return Expression(expression).build()
    }
}