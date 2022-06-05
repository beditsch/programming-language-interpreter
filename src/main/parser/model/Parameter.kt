package parser.model

import shared.Token

class Parameter(
    // TODO: TokenType parserowy
    val parameterType: Token<*>?,
    val parameterIdentifier: String
)
