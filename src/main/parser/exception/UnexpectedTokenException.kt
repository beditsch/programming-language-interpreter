package parser.exception

import shared.Token
import shared.TokenType

class UnexpectedTokenException(
    private val parseFunName: String,
    private val expectedTokenTypes: List<TokenType>,
    private val receivedToken: Token<*>?
) : Exception(
    message = "Unexpected token found while performing $parseFunName. " +
        "Expected: ${expectedTokenTypes.map { it.name }}; Found: ${receivedToken?.tokenType?.name} " +
        "at ${receivedToken?.position}"
)
