package interpreter.utils

import interpreter.exception.* // ktlint-disable no-wildcard-imports
import interpreter.model.Currency
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

        fun validateFunctionReturnValueType(funReturnType: VariableType, visitResult: Any?) {
            if (funReturnType.type != Type.VOID && visitResult == null)
                throw NoValueReturnedFromFunctionException(funReturnType.toString())

            when (funReturnType.type) {
                Type.INT -> if (visitResult !is Int) throw MismatchedValueTypeException(funReturnType.toString(), visitResult)
                Type.FLOAT -> if (visitResult !is Double) throw MismatchedValueTypeException(funReturnType.toString(), visitResult)
                Type.BOOL -> if (visitResult !is Boolean) throw MismatchedValueTypeException(funReturnType.toString(), visitResult)
                Type.STRING -> if (visitResult !is String) throw MismatchedValueTypeException(funReturnType.toString(), visitResult)
                Type.CURRENCY -> if (visitResult !is Currency || funReturnType.currencyId != visitResult.currencyId)
                    throw MismatchedValueTypeException(funReturnType.toString(), visitResult)
                Type.VOID -> if (visitResult != null)
                    throw MismatchedValueTypeException(funReturnType.toString(), visitResult)
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
