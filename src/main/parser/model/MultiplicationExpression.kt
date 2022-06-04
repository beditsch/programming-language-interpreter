package parser.model

import shared.Token

class MultiplicationExpression(
    val leftFactor: Expression,
    val operator: Token<*>?,
    val rightFactor: Expression
) : Expression, ProgramNode
