package parser.model

class Factor<LiteralType>(
    val isNegated: Boolean,
    val functionCall: FunctionCall?,
    val expression: Expression?,
    val identifier: String?,
    val literal: LiteralType?,
    val shouldCastTo: VariableType?
) : Expression, ProgramNode
