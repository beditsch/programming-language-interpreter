package parser.model

import interpreter.VisitorInterface

class WhileStatement(
    val condition: Expression,
    val block: Block
) : Statement, ProgramNode {
    override fun acceptVisitor(visitor: VisitorInterface) {
        visitor.visitWhileStatement(this)
    }
}
