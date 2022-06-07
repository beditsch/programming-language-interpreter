package interpreter.exception

class WrongNumberOfFunctionCallArgumentsException(funName: String, expectedNum: Int, passedNum: Int) :
    Exception("Number of expected arguments for function $funName: $expectedNum; number of passed arguments: $passedNum.")
