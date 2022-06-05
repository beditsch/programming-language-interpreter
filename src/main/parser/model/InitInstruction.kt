package parser.model

import shared.Token

class InitInstruction(
    // TODO: TokenType parserowy
    val type: Token<*>,
    val identifier: String,
    val assignmentExpression: Expression
) : Statement, ProgramNode
