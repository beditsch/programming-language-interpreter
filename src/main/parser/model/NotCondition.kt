package parser.model

class NotCondition(
    val isNegated: Boolean,
    val expression: Expression
) : Expression, ProgramNode
