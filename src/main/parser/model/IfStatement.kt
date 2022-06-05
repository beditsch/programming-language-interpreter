package parser.model

class IfStatement(
    val condition: Expression,
    val instruction: Statement,
    val elseInstruction: Statement?
) : Statement, ProgramNode
