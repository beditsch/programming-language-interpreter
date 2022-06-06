package interpreter.exception

class VariableAlreadyExistsException(identifier: String) :
    Exception("Variable with identifier $identifier has already been initiated.")
