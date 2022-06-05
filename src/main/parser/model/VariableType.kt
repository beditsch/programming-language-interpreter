package parser.model

import shared.Token

class VariableType {
    constructor(token: Token<*>) {
        type = Type.mapLexerTokenTypeToParserType(token.tokenType)
        currencyId = if (type == Type.CURRENCY) token.value.toString() else null
    }
    constructor(typeArg: Type, currencyIdArg: String?) {
        type = typeArg
        currencyId = currencyIdArg
    }

    val type: Type
    val currencyId: String?
}
