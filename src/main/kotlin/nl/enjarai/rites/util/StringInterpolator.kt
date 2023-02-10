package nl.enjarai.rites.util

class StringInterpolator(str: String) : Parser(str) {
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
                    s = { aa(it) + bb(it).toString() }
                } else {
                    val startVarPos = pos
                    while (isVariableChar()) nextChar()
                    val variableName = str.substring(startVarPos, pos)
                    val aa = s
                    s = { aa(it) + it[variableName].toString() }
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