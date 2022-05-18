package parser.model

import shared.Token

class MultiplicationExpression(
    private val leftFactor: Factor,
    private val operator: Token<*>?,
    private val rightFactor: Factor?
) : Expression()
