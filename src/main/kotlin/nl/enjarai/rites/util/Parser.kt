package nl.enjarai.rites.util

abstract class Parser(protected val str: String) {
    protected var pos = -1
    protected var ch: Int = 0

    protected fun nextChar() {
        ch = if (++pos < str.length) str[pos].code else -1
    }

    protected fun weat(charToEat: Int): Boolean {
        while (ch == ' '.code) nextChar()
        return eat(charToEat)
    }

    protected fun eat(charToEat: Int): Boolean {
        if (ch == charToEat) {
            nextChar()
            return true
        }
        return false
    }

    protected fun isVariableChar(): Boolean {
        return (ch >= 'a'.code && ch <= 'z'.code) || (ch >= 'A'.code && ch <= 'Z'.code) || ch == '-'.code || ch == '_'.code || ch == ':'.code
    }
}