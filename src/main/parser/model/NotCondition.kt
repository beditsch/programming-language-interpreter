package parser.model

class NotCondition(
    private val isNegated: Boolean,
    private val expression: Expression
) : ConditionBase()
