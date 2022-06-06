package parser.model

import parser.exception.ParsingException
import shared.TokenType

enum class Type {
    INT,
    FLOAT,
    STRING,
    BOOL,
    CURRENCY,
    VOID;

    companion object {
        fun mapLexerTokenTypeToParserType(tokenType: TokenType): Type {
            return when (tokenType) {
                TokenType.INT -> INT
                TokenType.FLOAT -> FLOAT
                TokenType.STRING -> STRING
                TokenType.BOOL -> BOOL
                TokenType.CURRENCY_ID -> CURRENCY
                TokenType.VOID -> VOID
                else -> throw ParsingException("Unrecognized token type ${tokenType.name}.")
            }
        }
    }
}
