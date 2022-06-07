package interpreter.exception

class UnsupportedValueTypeException(valueClassName: String, funName: String) :
    Exception("Unsupported value type: $valueClassName handled in function $funName.")
