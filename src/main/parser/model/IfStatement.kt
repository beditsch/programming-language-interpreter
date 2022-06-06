package parser.model

import interpreter.VisitorInterface

class IfStatement(
    val condition: Expression,
    val instruction: Statement,
    val elseInstruction: Statement?
) : Statement, ProgramNode {
    override fun acceptVisitor(visitor: VisitorInterface) {
        visitor.visitIfStatement(this)
    }
}
