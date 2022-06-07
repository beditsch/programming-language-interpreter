package interpreter.exception

class MismatchedValueTypeException(expectedType: String, value: Any?) :
    Exception("Expected value of type $expectedType but received $value.")
