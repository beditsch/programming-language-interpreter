package parser.exception

class DuplicateFunctionParameterIdentifierException(
    funName: String,
    paramNames: List<String>
) : Exception("Duplicate parameter identifiers: $paramNames in the definition of function: $funName.")
