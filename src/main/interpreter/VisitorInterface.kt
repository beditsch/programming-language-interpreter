package interpreter

import parser.model.* // ktlint-disable no-wildcard-imports
import parser.model.Function
import parser.model.arithmetic.AdditionExpression
import parser.model.arithmetic.DivisionExpression
import parser.model.arithmetic.MultiplicationExpression
import parser.model.arithmetic.SubtractionExpression
import parser.model.condition.* // ktlint-disable no-wildcard-imports

interface VisitorInterface {
    fun visitFunctionCall(functionCall: FunctionCall)
    fun visitFunction(function: Function)
    fun visitBlock(block: Block)
    fun visitAssignInstruction(assignInstruction: AssignInstruction)
    fun visitFactor(factor: Factor)
    fun visitFactorWithCast(factorWithCast: FactorWithCast)
    fun visitNegatedFactor(negatedFactor: NegatedFactor)
    fun visitReturnInstruction(returnInstruction: ReturnInstruction)
    fun visitIfStatement(ifStatement: IfStatement)
    fun visitWhileStatement(whileStatement: WhileStatement)
    fun visitInitInstruction(initInstruction: InitInstruction)
    fun visitAdditionExpression(additionExpression: AdditionExpression)
    fun visitSubtractionExpression(subtractionExpression: SubtractionExpression)
    fun visitMultiplicationExpression(multiplicationExpression: MultiplicationExpression)
    fun visitDivisionExpression(divisionExpression: DivisionExpression)
    fun visitOrCondition(orCondition: OrCondition)
    fun visitAndCondition(andCondition: AndCondition)
    fun visitNotCondition(notCondition: NotCondition)
    fun visitEqualCondition(equalCondition: EqualCondition)
    fun visitNotEqualCondition(notEqualCondition: NotEqualCondition)
    fun visitGreaterCondition(greaterCondition: GreaterCondition)
    fun visitGreaterOrEqualCondition(greaterOrEqualCondition: GreaterOrEqualCondition)
    fun visitLessCondition(lessCondition: LessCondition)
    fun visitLessOrEqualCondition(lessOrEqualCondition: LessOrEqualCondition)
}
