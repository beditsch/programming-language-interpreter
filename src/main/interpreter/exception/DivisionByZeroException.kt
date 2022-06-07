package interpreter.exception

class DivisionByZeroException(zeroValue: Any) : Exception("Division by $zeroValue is strictly forbidden!")
