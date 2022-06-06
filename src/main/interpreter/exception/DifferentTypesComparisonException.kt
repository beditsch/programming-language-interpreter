package interpreter.exception

class DifferentTypesComparisonException(value1: Any, value2: Any) :
    Exception("Values $value1 and $value2 are not of the same type.")
