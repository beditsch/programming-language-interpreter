package parser.exception

import shared.Token
import shared.TokenType

class UnexpectedTokenException(
    val parseFunName: String,
    val expectedTokenTypes: List<TokenType>,
    val receivedToken: Token<*>?
) : Exception(
    "Unexpected token found while performing $parseFunName. " +
        "Expected: ${expectedTokenTypes.map { it.name }}; Found: ${receivedToken?.tokenType?.name} " +
        "at ${receivedToken?.position}"
)
