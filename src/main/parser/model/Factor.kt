package parser.model

import interpreter.VisitorInterface

class Factor(
    val functionCall: FunctionCall?,
    val expression: Expression?,
    val identifier: String?,
    val literal: Any?
) : Expression, ProgramNode {
    override fun acceptVisitor(visitor: VisitorInterface) {
        visitor.visitFactor(this)
    }
}
