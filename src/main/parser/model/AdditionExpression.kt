package parser.model

import shared.Token

class AdditionExpression(
    private val leftExpression: Expression,
    private val operator: Token<*>?,
    private val rightExpression: Expression?
) : Expression()
