package parser.model.arithmetic

import parser.model.Expression
import parser.model.ProgramNode

class SubtractionExpression(
    val leftExpression: Expression,
    val rightExpression: Expression
) : Expression, ProgramNode
