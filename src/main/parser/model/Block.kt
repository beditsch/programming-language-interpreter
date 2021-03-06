package parser.model

import interpreter.VisitorInterface

class Block(
    val instrAndStatementsList: List<Statement>
) : Statement, ProgramNode {
    override fun acceptVisitor(visitor: VisitorInterface) {
        visitor.visitBlock(this)
    }
}
