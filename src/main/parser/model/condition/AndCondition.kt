package parser.model.condition

import parser.model.Expression
import parser.model.ProgramNode

class AndCondition(
    val leftCond: Expression,
    val rightCond: Expression
) : Expression, ProgramNode
