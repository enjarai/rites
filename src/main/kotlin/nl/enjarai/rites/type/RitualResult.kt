package nl.enjarai.rites.type

enum class RitualResult {
    SUCCESS,
    PASS,
    FAIL;

    companion object {
        fun successFromBool(boolean: Boolean): RitualResult {
            return if (boolean) SUCCESS else FAIL
        }

        fun merge(results: List<RitualResult>): RitualResult {
            results.forEach {
                if (it == FAIL) return FAIL
            }
            results.forEach {
                if (it == SUCCESS) return SUCCESS
            }
            return FAIL
        }
    }
}