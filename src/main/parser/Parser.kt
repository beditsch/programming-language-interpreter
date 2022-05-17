package parser

import lexer.Lexer
import parser.exception.UnexpectedTokenException
import parser.model.Function
import parser.model.Parameter
import parser.model.Program
import shared.Token
import shared.TokenType
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class Parser(
    private val lexer: Lexer,
    private val functions: HashMap<String, Function> = HashMap()
) {

    fun parse(): Program {
        while (tryParseFunction());
        return Program(functions)
    }

    private fun tryParseFunction(): Boolean {
        if (!lexer.currentTokenIsFunctionReturnType()) return false
        val functionReturnType = lexer.getTokenAndMoveToNext()

        if (!lexer.currentTokenIs(TokenType.IDENTIFIER))
            throw UnexpectedTokenException(Parser::tryParseFunction.name, listOf(TokenType.IDENTIFIER), lexer.getToken())
        val functionIdentifier = lexer.getTokenAndMoveToNext()

        val parameters = parseParameters()
        return TODO()
    }

    private fun parseParameters(): List<Parameter> {
        val parameters: MutableList<Parameter> = ArrayList()
        if (!lexer.currentTokenIs(TokenType.LEFT_BRACKET))
            throw UnexpectedTokenException(Parser::parseParameters.name, listOf(TokenType.LEFT_BRACKET), lexer.getToken())
        lexer.getNextToken()

        while (!lexer.currentTokenIs(TokenType.RIGHT_BRACKET)) {
            parameters.add(parseParameter())
            if (lexer.currentTokenIs(TokenType.COMMA)) lexer.getNextToken()
        }

        return parameters
    }

    private fun parseParameter(): Parameter = lexer.run {
        // if current token is not a type then it must be an identifier
        if (!currentTokenIsType()) {
            val paramIdentifier = getTokenAndMoveToNext()
            Parameter(null, paramIdentifier)
        }
        // if it's not a currency then it must be a simple type followed by an identifier
        else if (currentTokenIsType() && !currentTokenIsCurrency()) {
            val paramType = getTokenAndMoveToNext()
            if (!currentTokenIs(TokenType.IDENTIFIER))
                throw UnexpectedTokenException(Parser::parseParameter.name, listOf(TokenType.IDENTIFIER), lexer.getToken())
            val paramIdentifier = getTokenAndMoveToNext()

            Parameter(paramType, paramIdentifier)
        }
        // current token is either a currency or an identifier
        else if (currentTokenIsCurrency()) {
            val paramTypeOrIdentifier = getTokenAndMoveToNext()
            if (currentTokenIs(TokenType.COMMA)) Parameter(null, paramTypeOrIdentifier)
            if (currentTokenIs(TokenType.IDENTIFIER)) Parameter(paramTypeOrIdentifier, getTokenAndMoveToNext())
            else throw UnexpectedTokenException(Parser::parseParameter.name, listOf(TokenType.IDENTIFIER), lexer.getToken())
        } else throw UnexpectedTokenException(
                Parser::parseParameter.name,
                listOf(TokenType.IDENTIFIER, TokenType.INT, TokenType.FLOAT, TokenType.STRING, TokenType.BOOL),
                lexer.getToken()
        )
    }
}

private fun Lexer.currentTokenIs(tokenType: TokenType): Boolean =
    this.getToken()?.tokenType == tokenType

private fun Lexer.currentTokenIs(tokenTypes: List<TokenType>): Boolean =
    tokenTypes.contains(this.getToken()?.tokenType)

private fun Lexer.currentTokenIsFunctionReturnType(): Boolean =
    currentTokenIsType() || currentTokenIs(TokenType.VOID)

private fun Lexer.currentTokenIsType(): Boolean =
    currentTokenIsCurrency() || currentTokenIs(listOf(TokenType.INT, TokenType.FLOAT, TokenType.STRING, TokenType.BOOL))

private fun Lexer.currentTokenIsCurrency(): Boolean {
    val token = this.getToken() ?: return false
    return token.tokenType == TokenType.IDENTIFIER &&
        token.value is String &&
        (token.value as String) == (token.value as String).uppercase(Locale.getDefault())
}

private fun Lexer.getTokenAndMoveToNext(): Token<*> {
    val currentToken = this.getToken() ?: throw TODO()
    this.getNextToken()
    return currentToken
}
