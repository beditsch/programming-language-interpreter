package parser.model

class WhileStatement(
    val condition: Expression,
    val block: Block
) : Statement()
