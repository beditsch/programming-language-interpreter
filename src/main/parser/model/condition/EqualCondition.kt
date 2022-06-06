package parser.model.condition

import interpreter.VisitorInterface
import parser.model.Expression
import parser.model.ProgramNode

class EqualCondition(
    val leftCond: Expression,
    val rightCond: Expression
) : Expression, ProgramNode {
    override fun acceptVisitor(visitor: VisitorInterface) {
        visitor.visitEqualCondition(this)
    }
}
