package nl.enjarai.rites.util

import kotlin.math.*

class Expression(str: String) : Parser(str) {

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
            if (weat('+'.code)) { // addition
                val a = x
                val b = parseTerm()
                x = { a(it) + b(it) }
            }
            else if (weat('-'.code)) { // subtraction
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
            if (weat('*'.code)) { // addition
                val a = x
                val b = parseFactor()
                x = { a(it) * b(it) }
            }
            else if (weat('/'.code)) { // subtraction
                val a = x
                val b = parseFactor()
                x = { a(it) / b(it) }
            }
            else return x
        }
    }

    private fun parseFactor(): (Map<String, Double>) -> Double {
        if (weat('+'.code)) { // unary plus
            val a = parseFactor()
            return { +a(it) }
        }
        if (weat('-'.code)) { // unary minus
            val a = parseFactor()
            return { -a(it) }
        }
        var x: (Map<String, Double>) -> Double
        val startPos = pos
        if (weat('('.code)) { // parentheses
            x = parseExpression()
            if (!weat(')'.code)) throw RuntimeException("Missing ')'")
        } else if (ch >= '0'.code && ch <= '9'.code || ch == '.'.code) { // numbers
            while (ch >= '0'.code && ch <= '9'.code || ch == '.'.code) nextChar()
            val a = str.substring(startPos, pos).toDouble()
            x = { a }
        } else if (ch >= 'a'.code && ch <= 'z'.code) { // functions
            while (ch >= 'a'.code && ch <= 'z'.code) nextChar()
            val func: String = str.substring(startPos, pos)
            val a: (Map<String, Double>) -> Double
            if (weat('('.code)) {
                a = parseExpression()
                if (!weat(')'.code)) throw RuntimeException("Missing ')' after argument to $func")
            } else {
                throw RuntimeException("Missing '(' after function name $func")
            }
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
                "ceil" -> { it -> ceil(a(it)) }
                "floor" -> { it -> floor(a(it)) }
                "round" -> { it -> round(a(it)) }
                else -> throw RuntimeException("Unknown function: $func")
            }
        } else if (weat('$'.code)) {
            while (isVariableChar()) nextChar()
            val variable: String = str.substring(startPos + 1, pos)
            x = { it[variable] ?: throw RuntimeException("Unknown variable: $variable") }
        } else {
            throw RuntimeException("Unexpected: " + ch.toChar())
        }
        if (weat('^'.code)) { // exponentiation
            val a = x
            val b = parseFactor()
            x = { a(it).pow(b(it)) }
        }
        return x
    }
}