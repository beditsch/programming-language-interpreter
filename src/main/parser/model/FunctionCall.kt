package parser.model

import shared.Token

class FunctionCall(
    val identifier: Token<*>,
    val arguments: List<Expression>
) : Expression, ProgramNode, Instruction()
