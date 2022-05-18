package parser.exception

import shared.Token

class MissingExpressionException(
    private val parseFunName: String,
    private val receivedToken: Token<*>?
) : Exception(
    message = "Missing expression while performing $parseFunName. " +
        "at ${receivedToken?.position}"
)
