package parser.model

class InitInstruction(
    val type: VariableType,
    val identifier: String,
    val assignmentExpression: Expression
) : Statement, ProgramNode
