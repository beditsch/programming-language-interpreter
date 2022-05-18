package parser.model

import shared.Token

class Factor(
    private val isNegated: Boolean,
    functionCall: FunctionCall?,
    expression: Expression?,
    identifier: Token<*>?,
    literal: Token<*>?,
    shouldCastTo: Token<*>?
)
