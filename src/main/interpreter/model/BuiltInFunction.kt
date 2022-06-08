package interpreter.model

abstract class BuiltInFunction(val args: List<Any>) {
    abstract val funIdentifier: String
    abstract fun execute(): Any?
    abstract fun getNumberOfParams(): Int
}
