package parser.model

class Block(
    val instrAndStatementsList: List<Statement>
) : Statement, ProgramNode
