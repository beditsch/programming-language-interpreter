package parser.model

import shared.Token

class Condition(
    val leftCond: Expression,
    val operator: Token<*>?,
    val rightCond: Expression?
) : Expression
