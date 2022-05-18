package parser.model

import shared.Token

class InitInstruction(
    private val type: Token<*>,
    private val identifier: Token<*>,
    private val assignmentExpression: Expression
) : Instruction()
