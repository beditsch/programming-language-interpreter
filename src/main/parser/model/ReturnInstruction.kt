package parser.model

import interpreter.VisitorInterface

class ReturnInstruction(
    val returnExpression: Expression
) : Statement, ProgramNode {
    override fun acceptVisitor(visitor: VisitorInterface) {
        visitor.visitReturnInstruction(this)
    }
}
