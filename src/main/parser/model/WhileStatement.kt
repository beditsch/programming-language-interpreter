package parser.model

class WhileStatement(
    val condition: ConditionBase,
    val block: Block
) : Statement()
