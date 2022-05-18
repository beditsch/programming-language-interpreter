package parser.model

class WhileStatement(
    private val condition: ConditionBase,
    private val block: Block
) : Statement()
