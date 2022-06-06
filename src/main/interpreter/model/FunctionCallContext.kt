package interpreter.model

import interpreter.exception.VariableAlreadyExistsException

class FunctionCallContext(
    val scopes: MutableList<Scope> = mutableListOf()
) {
    fun tryGetVariableValue(identifier: String): Any? {
        val iter = scopes.listIterator(scopes.size)
        while (iter.hasPrevious()) {
            val scope = iter.previous()
            scope.localVariables[identifier]?.let { return it }
        }
        return null
    }

    fun tryUpdateVariable(identifier: String, newVal: Any): Boolean {
        val iter = scopes.listIterator(scopes.size)
        var isVarUpdated = false
        while (iter.hasPrevious() && !isVarUpdated) {
            val scope = iter.previous()
            if (scope.localVariables.containsKey(identifier)) {
                scope.localVariables[identifier] = newVal
                isVarUpdated = true
            }
        }
        return isVarUpdated
    }

    fun addVariable(identifier: String, value: Any) {
        val lastScope = scopes.last()
        if (lastScope.localVariables.containsKey(identifier))
            throw VariableAlreadyExistsException(identifier)
        lastScope.localVariables[identifier] = value
    }
}
