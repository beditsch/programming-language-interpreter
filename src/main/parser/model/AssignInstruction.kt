package parser.model

class AssignInstruction(
    val identifier: String,
    val assignmentExpression: Expression
) : Statement, ProgramNode
