package interpreter.utils

import interpreter.exception.* // ktlint-disable no-wildcard-imports
import interpreter.model.Currency
import interpreter.model.VisitResult
import parser.model.Function
import parser.model.FunctionCall
import parser.model.Parameter
import parser.model.Type
import parser.model.VariableType

class ValidationHelper {
    companion object {
        fun validateInitInstructionTypes(type: VariableType, value: Any) {
            when (type.type) {
                Type.INT -> if (value !is Int) throw MismatchedValueTypeException(type.toString(), value)
                Type.FLOAT -> if (value !is Double) throw MismatchedValueTypeException(type.toString(), value)
                Type.STRING -> if (value !is String) throw MismatchedValueTypeException(type.toString(), value)
                Type.BOOL -> if (value !is Boolean) throw MismatchedValueTypeException(type.toString(), value)
                Type.CURRENCY ->
                    if (value !is Int && value !is Float && !(value is Currency && value.currencyId == type.currencyId))
                        throw MismatchedValueTypeException(type.toString(), value)
                else -> throw UnsupportedVariableTypeException(type.toString())
            }
        }

        fun validateParameterTypes(parameters: List<Parameter>, values: List<Any>) {
            parameters.forEachIndexed { index, parameter ->
                parameter.parameterType?.let { validateInitInstructionTypes(it, values[index]) }
            }
        }

        fun validateFunctionReturnValueType(funReturnType: VariableType, visitResult: VisitResult?) {
            visitResult?.let {
                if (funReturnType.type != Type.VOID && !it.wasValueReturned)
                    throw NoValueReturnedFromFunctionException(funReturnType.toString())
            } ?: if (funReturnType.type != Type.VOID) throw NullLastVisitResultException()

            val returnedVal = visitResult?.value
            when (funReturnType.type) {
                Type.INT -> if (returnedVal !is Int) throw MismatchedValueTypeException(funReturnType.toString(), returnedVal)
                Type.FLOAT -> if (returnedVal !is Double) throw MismatchedValueTypeException(funReturnType.toString(), returnedVal)
                Type.BOOL -> if (returnedVal !is Boolean) throw MismatchedValueTypeException(funReturnType.toString(), returnedVal)
                Type.STRING -> if (returnedVal !is String) throw MismatchedValueTypeException(funReturnType.toString(), returnedVal)
                Type.CURRENCY -> if (returnedVal !is Currency || funReturnType.currencyId != returnedVal.currencyId)
                    throw MismatchedValueTypeException(funReturnType.toString(), returnedVal)
                Type.VOID -> if (visitResult?.wasValueReturned == true)
                    throw MismatchedValueTypeException(funReturnType.toString(), returnedVal)
            }
        }

        fun validateNumberOfFunctionCallArguments(function: Function, functionCall: FunctionCall) {
            if (function.parameters.size != functionCall.arguments.size)
                throw WrongNumberOfFunctionCallArgumentsException(
                    function.funIdentifier, function.parameters.size, functionCall.arguments.size
                )
        }
    }
}
