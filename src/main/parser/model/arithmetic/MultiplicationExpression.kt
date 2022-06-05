package parser.model.arithmetic

import parser.model.Expression
import parser.model.ProgramNode

class MultiplicationExpression(
    val leftFactor: Expression,
    val rightFactor: Expression
) : Expression, ProgramNode
