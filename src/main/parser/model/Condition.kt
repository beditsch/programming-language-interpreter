package parser.model

import shared.Token

class Condition(
    private val leftCond: ConditionBase,
    private val operator: Token<*>?,
    private val rightCond: ConditionBase?
) : ConditionBase()
