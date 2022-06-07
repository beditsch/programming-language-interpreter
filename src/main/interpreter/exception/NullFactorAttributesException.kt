package interpreter.exception

class NullFactorAttributesException() :
    Exception("Factor has all attributes equal to null. One of them should not be.")
