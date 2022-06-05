package parser.exception

class MissingExpressionConstructorForTokenTypeException(
    tokenTypeString: String
) : Exception("Cannot find constructor for expression with operand: $tokenTypeString")
