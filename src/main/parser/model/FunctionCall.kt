package parser.model

import shared.Token

class FunctionCall(
    private val identifier: Token<*>,
    private val arguments: List<Expression>
) : Instruction()
