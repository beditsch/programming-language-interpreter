package parser.model.arithmetic

import interpreter.VisitorInterface
import parser.model.Expression
import parser.model.ProgramNode

class DivisionExpression(
    val leftExpression: Expression,
    val rightExpression: Expression
) : Expression, ProgramNode {
    override fun acceptVisitor(visitor: VisitorInterface) {
        visitor.visitDivisionExpression(this)
    }
}
