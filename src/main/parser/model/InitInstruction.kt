package parser.model

import shared.Token

class InitInstruction(
    val type: Token<*>,
    val identifier: Token<*>,
    val assignmentExpression: Expression
) : Instruction()
