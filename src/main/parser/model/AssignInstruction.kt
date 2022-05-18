package parser.model

import shared.Token

class AssignInstruction(
    private val identifier: Token<*>,
    private val assignmentExpression: Expression
) : Instruction()
