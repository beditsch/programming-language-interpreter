package parser.model

import shared.Token

class AssignInstruction(
    val identifier: Token<*>,
    val assignmentExpression: Expression
) : Instruction()
