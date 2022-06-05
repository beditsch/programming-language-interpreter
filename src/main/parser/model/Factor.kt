package parser.model

import shared.Token

class Factor(
    val isNegated: Boolean,
    val functionCall: FunctionCall?,
    val expression: Expression?,
    val identifier: String?,
    // TODO
    val literal: Token<*>?,
    // TODO
    val shouldCastTo: Token<*>?
) : Expression, ProgramNode
