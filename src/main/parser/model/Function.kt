package parser.model

import shared.Token

class Function(
    private val funReturnType: Token<*>,
    private val funIdentifier: Token<*>,
    private val parameters: List<Parameter>,
    private val functionBlock: Block
)
