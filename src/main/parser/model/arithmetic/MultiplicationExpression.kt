package parser.model.arithmetic

import interpreter.VisitorInterface
import parser.model.Expression
import parser.model.ProgramNode

class MultiplicationExpression(
    val leftFactor: Expression,
    val rightFactor: Expression
) : Expression, ProgramNode {
    override fun acceptVisitor(visitor: VisitorInterface) {
        visitor.visitMultiplicationExpression(this)
    }
}
