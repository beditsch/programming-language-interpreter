package interpreter.utils

import interpreter.exception.MismatchedValueTypeException
import interpreter.exception.NoValueReturnedFromFunctionException
import interpreter.exception.NullLastVisitResultException
import interpreter.exception.WrongNumberOfFunctionCallArgumentsException
import interpreter.model.Currency
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.WordSpec
import io.kotest.data.forAll
import io.kotest.data.row
import parser.model.* // ktlint-disable no-wildcard-imports
import parser.model.Function
import java.math.BigDecimal

class ValidationHelperTest : WordSpec({
    ValidationHelper::class.java.simpleName should {
        "throw ${WrongNumberOfFunctionCallArgumentsException::class.simpleName} when number of function call arguments" +
            "doesn't match number of parameters" {
                val variableType = VariableType(Type.INT)
                val function = Function(variableType, "myFunc", emptyList(), Block(emptyList()))
                val functionCall = FunctionCall("myFunc", listOf(Factor(null, null, "val1", null)))
                shouldThrow<WrongNumberOfFunctionCallArgumentsException> {
                    ValidationHelper.validateNumberOfFunctionCallArguments(function, functionCall)
                }
            }

        "throw ${MismatchedValueTypeException::class.simpleName} when returned value doesn't match function return type" {
            forAll(
                row(VariableType(Type.INT), 3.0),
                row(VariableType(Type.FLOAT), 1),
                row(VariableType(Type.BOOL), 12),
                row(VariableType(Type.STRING), true),
                row(VariableType(Type.CURRENCY, "PLN"), 3.0),
                row(VariableType(Type.CURRENCY, "PLN"), Currency(BigDecimal(1), "EUR")),
            ) {
                    retType, result ->
                shouldThrow<MismatchedValueTypeException> {
                    ValidationHelper.validateFunctionReturnValueType(retType, result)
                }
            }
        }

        "throw ${NoValueReturnedFromFunctionException::class.simpleName} when function should return value but didn't" {
            val returnType = VariableType(Type.INT)
            val visitResult = 3.0
            shouldThrow<MismatchedValueTypeException> {
                ValidationHelper.validateFunctionReturnValueType(returnType, visitResult)
            }
        }

        "throw ${NullLastVisitResultException::class.simpleName} when visitResult is null" {
            val returnType = VariableType(Type.INT)
            val visitResult = null
            shouldThrow<NoValueReturnedFromFunctionException> {
                ValidationHelper.validateFunctionReturnValueType(returnType, visitResult)
            }
        }

        "throw ${MismatchedValueTypeException::class.simpleName} when value doesn't match type in init instruction" {
            forAll(
                row(VariableType(Type.INT), 3.0),
                row(VariableType(Type.FLOAT), 1),
                row(VariableType(Type.BOOL), 12),
                row(VariableType(Type.STRING), true),
                row(VariableType(Type.CURRENCY, "PLN"), "PLN"),
                row(VariableType(Type.CURRENCY, "PLN"), Currency(BigDecimal(1), "EUR")),
            ) {
                    retType, initVal ->
                shouldThrow<MismatchedValueTypeException> {
                    ValidationHelper.validateInitInstructionTypes(retType, initVal)
                }
            }
        }

        "throw ${MismatchedValueTypeException::class.simpleName} when function call argument doesn't match parameter type" {
            val parameters = listOf(Parameter(null, "param1"), Parameter(VariableType(Type.INT), "param2"))
            val vals = listOf<Any>("12345", true)
            shouldThrow<MismatchedValueTypeException> {
                ValidationHelper.validateParameterTypes(parameters, vals)
            }
        }
    }
})
