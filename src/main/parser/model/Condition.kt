package parser.model

import shared.Token

class Condition(
    val leftCond: ConditionBase,
    val operator: Token<*>?,
    val rightCond: ConditionBase?
) : ConditionBase()
