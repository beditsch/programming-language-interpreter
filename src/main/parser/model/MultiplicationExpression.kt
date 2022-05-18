package parser.model

import shared.Token

class MultiplicationExpression(
    val leftFactor: Factor,
    val operator: Token<*>?,
    val rightFactor: Factor?
) : Expression()
