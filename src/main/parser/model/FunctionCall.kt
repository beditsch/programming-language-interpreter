package parser.model

import interpreter.VisitorInterface

class FunctionCall(
    val identifier: String,
    val arguments: List<Expression>
) : Expression, Statement, ProgramNode {
    override fun acceptVisitor(visitor: VisitorInterface) {
        visitor.visitFunctionCall(this)
    }
}
