package interpreter.exception

class MissingMainFunctionDeclarationException(mainFunctionName: String) :
    Exception("Missing declaration of main function identified by: $mainFunctionName.")
