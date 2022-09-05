package nl.enjarai.rites.util

import kotlin.math.*

class Expression(private val str: String) {
    private var pos = -1
    private var ch: Int = 0

    private fun nextChar() {
        ch = if (++pos < str.length) str[pos].code else -1
    }

    private fun eat(charToEat: Int): Boolean {
        while (ch == ' '.code) nextChar()
        if (ch == charToEat) {
            nextChar()
            return true
        }
        return false
    }

    fun build(): (Map<String, Double>) -> Double {
        nextChar()
        val x = parseExpression()
        if (pos < str.length) throw RuntimeException("Unexpected: " + ch.toChar())
        return x
    }

    // Grammar:
    // expression = term | expression `+` term | expression `-` term
    // term = factor | term `*` factor | term `/` factor
    // factor = `+` factor | `-` factor | `(` expression `)` | number
    //        | functionName `(` expression `)` | functionName factor
    //        | factor `^` factor
    private fun parseExpression(): (Map<String, Double>) -> Double {
        var x = parseTerm()
        while (true) {
            if (eat('+'.code)) { // addition
                val a = x
                val b = parseTerm()
                x = { a(it) + b(it) }
            }
            else if (eat('-'.code)) { // subtraction
                val a = x
                val b = parseTerm()
                x = { a(it) - b(it) }
            }
            else return x
        }
    }

    private fun parseTerm(): (Map<String, Double>) -> Double {
        var x = parseFactor()
        while (true) {
            if (eat('*'.code)) { // addition
                val a = x
                val b = parseFactor()
                x = { a(it) * b(it) }
            }
            else if (eat('/'.code)) { // subtraction
                val a = x
                val b = parseFactor()
                x = { a(it) / b(it) }
            }
            else return x
        }
    }

    private fun parseFactor(): (Map<String, Double>) -> Double {
        if (eat('+'.code)) { // unary plus
            val a = parseFactor()
            return { +a(it) }
        }
        if (eat('-'.code)) { // unary minus
            val a = parseFactor()
            return { -a(it) }
        }
        var x: (Map<String, Double>) -> Double
        val startPos = pos
        if (eat('('.code)) { // parentheses
            x = parseExpression()
            if (!eat(')'.code)) throw RuntimeException("Missing ')'")
        } else if (ch >= '0'.code && ch <= '9'.code || ch == '.'.code) { // numbers
            while (ch >= '0'.code && ch <= '9'.code || ch == '.'.code) nextChar()
            val a = str.substring(startPos, pos).toDouble()
            x = { a }
        } else if (ch >= 'a'.code && ch <= 'z'.code) { // functions
            while (ch >= 'a'.code && ch <= 'z'.code) nextChar()
            val func: String = str.substring(startPos, pos)
            if (eat('('.code)) {
                val a = parseExpression()
                if (!eat(')'.code)) throw RuntimeException("Missing ')' after argument to $func")
                x = when (func) {
                    "sqrt" -> { it -> sqrt(a(it)) }
                    "sin" -> { it -> sin(a(it)) }
                    "cos" -> { it -> cos(a(it)) }
                    "tan" -> { it -> tan(a(it)) }
                    "asin" -> { it -> asin(a(it)) }
                    "acos" -> { it -> acos(a(it)) }
                    "atan" -> { it -> atan(a(it)) }
                    "abs" -> { it -> abs(a(it)) }
                    "exp" -> { it -> exp(a(it)) }
                    else -> throw RuntimeException("Unknown function: $func")
                }
            } else {
                x = { it[func] ?: throw RuntimeException("Unknown variable: $func") }
            }

        } else {
            throw RuntimeException("Unexpected: " + ch.toChar())
        }
        if (eat('^'.code)) { // exponentiation
            val a = x
            val b = parseFactor()
            x = { a(it).pow(b(it)) }
        }
        return x
    }
}