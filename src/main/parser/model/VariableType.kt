package parser.model

import shared.Token

class VariableType {
    constructor(token: Token<*>) {
        type = Type.mapLexerTokenTypeToParserType(token.tokenType)
        currencyId = if (type == Type.CURRENCY) token.value.toString() else null
    }
    constructor(typeArg: Type, currencyIdArg: String? = null) {
        type = typeArg
        currencyId = currencyIdArg
    }

    val type: Type
    val currencyId: String?

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as VariableType
        if (type != other.type || currencyId != other.currencyId) return false
        return true
    }
}
