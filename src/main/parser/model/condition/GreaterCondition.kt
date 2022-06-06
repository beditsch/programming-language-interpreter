package parser.model.condition

import parser.model.Expression
import parser.model.ProgramNode

class GreaterCondition(
    val leftCond: Expression,
    val rightCond: Expression
) : Expression, ProgramNode
