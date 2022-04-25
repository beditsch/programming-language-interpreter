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
}
