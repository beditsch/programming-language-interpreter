package parser.model.condition

import parser.model.Expression
import parser.model.ProgramNode

class NotCondition(
    val expression: Expression
) : Expression, ProgramNode
