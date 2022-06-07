package interpreter.exception

class UnsupportedVariableTypeException(typeName: String) :
    Exception("Variable cannot be of type $typeName.")
