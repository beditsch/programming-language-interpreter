package parser.model.arithmetic

import parser.model.Expression
import parser.model.ProgramNode

class DivisionExpression(
    val leftExpression: Expression,
    val rightExpression: Expression
) : Expression, ProgramNode
