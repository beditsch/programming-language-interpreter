package parser.model

import shared.Token

class Function(
    val funReturnType: Token<*>,
    val funIdentifier: Token<*>,
    val parameters: List<Parameter>,
    val functionBlock: Block
)
