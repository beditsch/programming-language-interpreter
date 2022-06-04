package parser.model

import shared.Token

class Factor(
    val isNegated: Boolean,
    val functionCall: FunctionCall?,
    val expression: Expression?,
    val identifier: Token<*>?,
    val literal: Token<*>?,
    val shouldCastTo: Token<*>?
) : Expression
