package parser.model

import shared.Token

class AdditionExpression(
    val leftExpression: Expression,
    val operator: Token<*>?,
    val rightExpression: Expression?
) : Expression()
