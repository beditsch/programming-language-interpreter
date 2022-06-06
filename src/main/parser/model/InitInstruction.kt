package parser.model

import interpreter.VisitorInterface

class InitInstruction(
    val type: VariableType,
    val identifier: String,
    val assignmentExpression: Expression
) : Statement, ProgramNode {
    override fun acceptVisitor(visitor: VisitorInterface) {
        visitor.visitInitInstruction(this)
    }
}
