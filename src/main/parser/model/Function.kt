package parser.model

import interpreter.VisitorInterface
import parser.exception.DuplicateFunctionParameterIdentifierException

class Function(
    val funReturnType: VariableType,
    val funIdentifier: String,
    val parameters: List<Parameter>,
    val functionBlock: Block
) : ProgramNode {
    init {
        val paramsIdentifiers = parameters.map { it.parameterIdentifier }
        if (paramsIdentifiers.distinct().size != paramsIdentifiers.size) {
            val duplicateParams = paramsIdentifiers.filter { it.count() > 1 }.distinct()
            throw DuplicateFunctionParameterIdentifierException(funIdentifier, duplicateParams)
        }
    }

    override fun acceptVisitor(visitor: VisitorInterface) {
        visitor.visitFunction(this)
    }
}
