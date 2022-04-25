package shared

enum class TokenType(s: String? = null) {
    // data types
    INT("int"),
    FLOAT("float"),
    STRING("string"),
    BOOL("bool"),

    // logical operators
    EQUAL("=="),
    NOT_EQUAL("!="),
    LESS("<"),
    GREATER(">"),
    LESS_OR_EQUAL("<="),
    GREATER_OR_EQUAL(">="),
    NOT("!"),
    AND("&&"),
    OR("||"),

    // arithmetical operators
    ADD("+"),
    SUBTRACT("-"),
    MULTIPLY("*"),
    DIVIDE("/"),

    ETX;

    companion object {
        fun getOperatorDict(): Map<String, TokenType> {
            return mapOf(
                "==" to EQUAL,
                "!=" to NOT_EQUAL,
                "<" to LESS,
                ">" to GREATER,
                "<=" to LESS_OR_EQUAL,
                ">=" to GREATER_OR_EQUAL,
                "!" to NOT,
                "&&" to AND,
                "||" to OR,
                "+" to ADD,
                "-" to SUBTRACT,
                "*" to MULTIPLY,
                "/" to DIVIDE
            )
        }
    }
}
