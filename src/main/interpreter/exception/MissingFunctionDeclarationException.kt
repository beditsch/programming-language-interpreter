package interpreter.exception

class MissingFunctionDeclarationException(functionName: String) :
    Exception("Missing declaration of function identified by: $functionName.")
