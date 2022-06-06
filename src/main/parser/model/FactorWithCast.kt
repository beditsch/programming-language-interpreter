package parser.model

import interpreter.VisitorInterface

class FactorWithCast(
    val factor: Expression,
    val castTo: VariableType
) : Expression, ProgramNode {
    override fun acceptVisitor(visitor: VisitorInterface) {
        visitor.visitFactorWithCast(this)
    }
}
