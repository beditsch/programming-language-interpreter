package parser.model

class FunctionCall(
    val identifier: String,
    val arguments: List<Expression>
) : Expression, Statement, ProgramNode
