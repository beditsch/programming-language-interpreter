package parser.model.arithmetic

import parser.model.Expression
import parser.model.ProgramNode

class AdditionExpression(
    val leftExpression: Expression,
    val rightExpression: Expression
) : Expression, ProgramNode
