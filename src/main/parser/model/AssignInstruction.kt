package parser.model

import interpreter.VisitorInterface

class AssignInstruction(
    val identifier: String,
    val assignmentExpression: Expression
) : Statement, ProgramNode {
    override fun acceptVisitor(visitor: VisitorInterface) {
        visitor.visitAssignInstruction(this)
    }
}
