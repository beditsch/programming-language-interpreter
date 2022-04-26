package shared

enum class TokenType {
    // data types values
    INT_VAL,
    FLOAT_VAL,
    STRING_VAL,

    // logical operators
    EQUAL,
    NOT_EQUAL,
    LESS,
    GREATER,
    LESS_OR_EQUAL,
    GREATER_OR_EQUAL,
    NOT,
    AND,
    OR,

    // arithmetical operators
    ADD,
    SUBTRACT,
    MULTIPLY,
    DIVIDE,

    // keywords
    IF,
    ELSE,
    WHILE,
    CAST,
    TRUE,
    FALSE,
    BOOL,
    VOID,
    RETURN,
    INT,
    FLOAT,
    STRING,

    IDENTIFIER,
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

        fun getKeywordsDict(): Map<String, TokenType> {
            return mapOf(
                "if" to IF,
                "else" to ELSE,
                "while" to WHILE,
                "as" to CAST,
                "true" to TRUE,
                "false" to FALSE,
                "bool" to BOOL,
                "void" to VOID,
                "return" to RETURN,
                "int" to INT,
                "float" to FLOAT,
                "string" to STRING,
            )
        }
    }
}
