package interpreter.exception

class VariableNotFoundException(varName: String) :
    Exception("Variable with identifier: $varName is not defined.")
