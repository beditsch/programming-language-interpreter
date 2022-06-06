package interpreter.model

class Scope(
    val localVariables: MutableMap<String, Any> = mutableMapOf()
)
