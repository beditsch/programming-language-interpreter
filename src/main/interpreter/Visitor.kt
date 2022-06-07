package interpreter

import interpreter.exception.* // ktlint-disable no-wildcard-imports
import interpreter.model.* // ktlint-disable no-wildcard-imports
import interpreter.utils.ArithmeticsHelper
import interpreter.utils.ComparisonHelper
import interpreter.utils.ValidationHelper
import parser.model.* // ktlint-disable no-wildcard-imports
import parser.model.Function
import parser.model.arithmetic.AdditionExpression
import parser.model.arithmetic.DivisionExpression
import parser.model.arithmetic.MultiplicationExpression
import parser.model.arithmetic.SubtractionExpression
import parser.model.condition.* // ktlint-disable no-wildcard-imports

class Visitor(
    val program: Program,
    val currencyMap: HashMap<String, Float>,
    val functionCallContexts: MutableList<FunctionCallContext> = mutableListOf(),
    private var lastVisitResult: VisitResult? = null
) : VisitorInterface {
    // TODO kolejna mapka z mergem funkcji z programu z funkcjami własnymi

    override fun visitFunctionCall(functionCall: FunctionCall) {
        val function = program.functions[functionCall.identifier]
            ?: throw TODO("add print function support")

        ValidationHelper.validateNumberOfFunctionCallArguments(function, functionCall)

        val argumentValues = functionCall.arguments.map { it.acceptVisitor(this); getLastVisitVal() }
        ValidationHelper.validateParameterTypes(function.parameters, argumentValues)
        val funLocalVarsMap = function.parameters.mapIndexed { idx, param ->
            param.parameterIdentifier to argumentValues[idx]
        }.toMap().toMutableMap()

        val scope = Scope(funLocalVarsMap)
        val functionCallContext = FunctionCallContext(mutableListOf(scope))
        functionCallContexts.add(functionCallContext)

        function.acceptVisitor(this)
        ValidationHelper.validateFunctionReturnValueType(function.funReturnType, lastVisitResult)
        lastVisitResult = if (function.funReturnType.type == Type.VOID) null
        else VisitResult(getLastVisitVal())

        functionCallContexts.removeLast()
    }

    override fun visitFunction(function: Function) {
        function.functionBlock.acceptVisitor(this)
    }

    override fun visitBlock(block: Block) {
        val functionCallContext = functionCallContexts.last()
        functionCallContext.scopes.add(Scope())
        block.instrAndStatementsList.forEach {
            it.acceptVisitor(this)
            if (getLastVisitWasValueReturned()) return@forEach
        }
        functionCallContext.scopes.removeLast()
    }

    override fun visitAssignInstruction(assignInstruction: AssignInstruction) {
        val functionCallContext = functionCallContexts.last()
        assignInstruction.assignmentExpression.acceptVisitor(this)
        val newVal = getLastVisitVal()
        val varIdentifier = assignInstruction.identifier
        if (!functionCallContext.tryUpdateVariable(varIdentifier, newVal))
            throw VariableNotFoundException(varIdentifier)
    }

    override fun visitFactor(factor: Factor) {
        when {
            factor.functionCall != null -> factor.functionCall.acceptVisitor(this)
            factor.expression != null -> factor.expression.acceptVisitor(this)
            factor.identifier != null -> retrieveVariable(factor.identifier)
            factor.literal != null -> retrieveLiteral(factor.literal)
            else -> throw NullFactorAttributesException()
        }
    }

    override fun visitFactorWithCast(factorWithCast: FactorWithCast) {
        factorWithCast.factor.acceptVisitor(this)
        val value = getLastVisitVal()
        // TODO: finish impl
    }

    override fun visitNegatedFactor(negatedFactor: NegatedFactor) {
        negatedFactor.factor.acceptVisitor(this)
        val negatedVal: Any = when (val value = getLastVisitVal()) {
            is Int -> -value
            is Double -> -value
            is Currency -> Currency(-value.amount, value.currencyId)
            else -> throw UnsupportedValueTypeException(value::class.java.name, Visitor::visitNegatedFactor.name)
        }
        lastVisitResult = VisitResult(negatedVal)
    }

    override fun visitReturnInstruction(returnInstruction: ReturnInstruction) {
        returnInstruction.returnExpression.acceptVisitor(this)
        val value = getLastVisitVal()
        lastVisitResult = VisitResult(value, true)
    }

    override fun visitIfStatement(ifStatement: IfStatement) {
        ifStatement.condition.acceptVisitor(this)
        val result = getLastVisitVal()
        if (result.toBoolean())
            ifStatement.instruction.acceptVisitor(this)
        else ifStatement.elseInstruction?.acceptVisitor(this)
    }

    override fun visitWhileStatement(whileStatement: WhileStatement) {
        whileStatement.condition.acceptVisitor(this)
        var condValue = getLastVisitVal()
        while (condValue.toBoolean()) {
            whileStatement.block.acceptVisitor(this)
            if (getLastVisitWasValueReturned()) break
            whileStatement.condition.acceptVisitor(this)
            condValue = getLastVisitVal()
        }
    }

    override fun visitInitInstruction(initInstruction: InitInstruction) {
        initInstruction.assignmentExpression.acceptVisitor(this)
        val value = getLastVisitVal()
        val identifier = initInstruction.identifier
        ValidationHelper.validateInitInstructionTypes(initInstruction.type, value)
        val functionCallContext = functionCallContexts.last()
        functionCallContext.addVariable(identifier, value)
    }

    override fun visitAdditionExpression(additionExpression: AdditionExpression) {
        additionExpression.leftExpression.acceptVisitor(this)
        val leftVal = getLastVisitVal()
        additionExpression.rightExpression.acceptVisitor(this)
        val rightVal = getLastVisitVal()

        val result = ArithmeticsHelper.add(leftVal, rightVal)
        lastVisitResult = VisitResult(result)
    }

    override fun visitSubtractionExpression(subtractionExpression: SubtractionExpression) {
        subtractionExpression.leftExpression.acceptVisitor(this)
        val leftVal = getLastVisitVal()
        subtractionExpression.rightExpression.acceptVisitor(this)
        val rightVal = getLastVisitVal()

        val result = ArithmeticsHelper.subtract(leftVal, rightVal)
        lastVisitResult = VisitResult(result)
    }

    override fun visitMultiplicationExpression(multiplicationExpression: MultiplicationExpression) {
        multiplicationExpression.leftFactor.acceptVisitor(this)
        val leftVal = getLastVisitVal()
        multiplicationExpression.rightFactor.acceptVisitor(this)
        val rightVal = getLastVisitVal()

        val result = ArithmeticsHelper.multiply(leftVal, rightVal)
        lastVisitResult = VisitResult(result)
    }

    override fun visitDivisionExpression(divisionExpression: DivisionExpression) {
        divisionExpression.leftExpression.acceptVisitor(this)
        val leftVal = getLastVisitVal()
        divisionExpression.rightExpression.acceptVisitor(this)
        val rightVal = getLastVisitVal()

        val result = ArithmeticsHelper.divide(leftVal, rightVal)
        lastVisitResult = VisitResult(result)
    }

    override fun visitOrCondition(orCondition: OrCondition) {
        orCondition.leftCond.acceptVisitor(this)
        val leftVal = getLastVisitVal().toBoolean()

        if (leftVal) {
            lastVisitResult = VisitResult(leftVal)
            return
        }

        orCondition.rightCond.acceptVisitor(this)
        val rightVal = getLastVisitVal().toBoolean()
        lastVisitResult = VisitResult(rightVal)
    }

    override fun visitAndCondition(andCondition: AndCondition) {
        andCondition.leftCond.acceptVisitor(this)
        val leftVal = getLastVisitVal().toBoolean()
        if (!leftVal) {
            lastVisitResult = VisitResult(leftVal)
            return
        }

        andCondition.rightCond.acceptVisitor(this)
        val rightVal = getLastVisitVal().toBoolean()
        lastVisitResult = VisitResult(rightVal)
    }

    override fun visitNotCondition(notCondition: NotCondition) {
        notCondition.expression.acceptVisitor(this)
        val value = getLastVisitVal().toBoolean()

        lastVisitResult = VisitResult(!value)
    }

    override fun visitEqualCondition(equalCondition: EqualCondition) {
        equalCondition.leftCond.acceptVisitor(this)
        val leftVal = getLastVisitVal()
        equalCondition.rightCond.acceptVisitor(this)
        val rightVal = getLastVisitVal()

        val result = ComparisonHelper.equal(leftVal, rightVal)
        lastVisitResult = VisitResult(result)
    }

    override fun visitNotEqualCondition(notEqualCondition: NotEqualCondition) {
        notEqualCondition.leftCond.acceptVisitor(this)
        val leftVal = getLastVisitVal()
        notEqualCondition.rightCond.acceptVisitor(this)
        val rightVal = getLastVisitVal()

        val result = ComparisonHelper.notEqual(leftVal, rightVal)
        lastVisitResult = VisitResult(result)
    }

    override fun visitGreaterCondition(greaterCondition: GreaterCondition) {
        greaterCondition.leftCond.acceptVisitor(this)
        val leftVal = getLastVisitVal()
        greaterCondition.rightCond.acceptVisitor(this)
        val rightVal = getLastVisitVal()

        val result = ComparisonHelper.greater(leftVal, rightVal)
        lastVisitResult = VisitResult(result)
    }

    override fun visitGreaterOrEqualCondition(greaterOrEqualCondition: GreaterOrEqualCondition) {
        greaterOrEqualCondition.leftCond.acceptVisitor(this)
        val leftVal = getLastVisitVal()
        greaterOrEqualCondition.rightCond.acceptVisitor(this)
        val rightVal = getLastVisitVal()

        val result = ComparisonHelper.greaterOrEqual(leftVal, rightVal)
        lastVisitResult = VisitResult(result)
    }

    override fun visitLessCondition(lessCondition: LessCondition) {
        lessCondition.leftCond.acceptVisitor(this)
        val leftVal = getLastVisitVal()
        lessCondition.rightCond.acceptVisitor(this)
        val rightVal = getLastVisitVal()

        val result = ComparisonHelper.less(leftVal, rightVal)
        lastVisitResult = VisitResult(result)
    }

    override fun visitLessOrEqualCondition(lessOrEqualCondition: LessOrEqualCondition) {
        lessOrEqualCondition.leftCond.acceptVisitor(this)
        val leftVal = getLastVisitVal()
        lessOrEqualCondition.rightCond.acceptVisitor(this)
        val rightVal = getLastVisitVal()

        val result = ComparisonHelper.lessOrEqual(leftVal, rightVal)
        lastVisitResult = VisitResult(result)
    }

    private fun retrieveVariable(identifier: String) {
        val functionCallContext = functionCallContexts.last()
        val value = functionCallContext.tryGetVariableValue(identifier)
            ?: throw VariableNotFoundException(identifier)
        lastVisitResult = VisitResult(value)
    }

    private fun retrieveLiteral(literal: Any) {
        lastVisitResult = VisitResult(literal)
    }

    private fun Any.toBoolean(): Boolean {
        if (this !is Boolean) throw MismatchedValueTypeException(Boolean::class.java.name, this)
        else return this
    }

    private fun getLastVisitVal(): Any {
        return lastVisitResult?.value ?: throw NullLastVisitResultException()
    }

    private fun getLastVisitWasValueReturned(): Boolean {
        return lastVisitResult?.wasValueReturned ?: throw NullLastVisitResultException()
    }
}
