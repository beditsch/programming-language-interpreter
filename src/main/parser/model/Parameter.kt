package parser.model

import shared.Token

class Parameter(
    val parameterType: Token<*>?,
    val parameterIdentifier: Token<*>
)
