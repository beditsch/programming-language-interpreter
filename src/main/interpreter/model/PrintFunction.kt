package interpreter.model

import interpreter.exception.WrongNumberOfFunctionCallArgumentsException

class PrintFunction(args: List<Any>) : BuiltInFunction(args) {
    override val funIdentifier: String
        get() = "print"

    override fun execute(): Any? {
        if (args.size != getNumberOfParams())
            throw WrongNumberOfFunctionCallArgumentsException("print", 1, args.size)
        print(args[0].toString())
        return null
    }

    override fun getNumberOfParams(): Int { return 1 }
}
