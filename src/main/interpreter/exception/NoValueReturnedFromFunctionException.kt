package interpreter.exception

class NoValueReturnedFromFunctionException(funReturnType: String) :
    Exception("Function should return type $funReturnType but no value was returned.")
