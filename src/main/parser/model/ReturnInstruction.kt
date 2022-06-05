package parser.model

class ReturnInstruction(
    val returnExpression: Expression
) : Statement, ProgramNode
