package parser.model

import interpreter.VisitorInterface

class NegatedFactor(
    val factor: Expression
) : Expression, ProgramNode {

    override fun acceptVisitor(visitor: VisitorInterface) {
        visitor.visitNegatedFactor(this)
    }
}
