package interpreter.exception

class CombinationOfUnsupportedValueTypesException(value1ClassName: String, value2ClassName: String, funName: String) :
    Exception("Unsupported combination of value types: $value1ClassName, $value2ClassName passed to function $funName.")
