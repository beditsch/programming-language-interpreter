package interpreter.model

class PrintFunction(args: List<Any>) : BuiltInFunction(args) {
    override val funIdentifier: String
        get() = "print"

    override fun execute(): Any? {
        args.forEach { print(it.toString()) }
        return null
    }
}
