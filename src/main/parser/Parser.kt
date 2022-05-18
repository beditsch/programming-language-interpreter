package parser

import lexer.Lexer
import parser.exception.UnexpectedTokenException
import parser.model.* // ktlint-disable no-wildcard-imports
import parser.model.Function
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

        val block = parseBlock()
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

        lexer.getNextToken()
        return parameters
    }

    private fun parseParameter(): Parameter = lexer.run {
        // if current token is not a type and it is an identifier
        if (!currentTokenIsType() && currentTokenIs(TokenType.IDENTIFIER)) {
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

    private fun parseBlock(): Boolean {
        if (!lexer.currentTokenIs(TokenType.LEFT_CURLY_BRACKET))
            throw UnexpectedTokenException(Parser::parseBlock.name, listOf(TokenType.LEFT_CURLY_BRACKET), lexer.getToken())
        lexer.getNextToken()

        return TODO()
    }

    private fun tryParseInstruction(): Instruction? {
        return TODO()
    }

    private fun tryParseStatement(): Boolean {
        return TODO()
    }

    private fun tryParseAssignInstructionOrFunctionCallOrInitInstruction(): Triple<AssignInstruction?, FunctionCall?, InitInstruction?> {
        if (!lexer.currentTokenIs(TokenType.IDENTIFIER) && !lexer.currentTokenIsType())
            return Triple(null, null, null)

        val identifierOrType = lexer.getToken() ?: throw TODO()
        // it's an assign instr, a function call or an init with currency as a type
        if (lexer.currentTokenIs(TokenType.IDENTIFIER)) {
            lexer.getNextToken()

            val functionCallArguments = tryParseFunctionCallArguments()
            if (functionCallArguments != null)
                return Triple(null, FunctionCall(identifierOrType, functionCallArguments), null)

            val assignmentExpression = tryParseAssignment()
            if (assignmentExpression != null)
                return Triple(AssignInstruction(identifierOrType, assignmentExpression), null, null)
        } else lexer.getNextToken()

        val initInstruction = parseRestOfInitInstruction(identifierOrType)
        return Triple(null, null, initInstruction)
    }

    private fun parseRestOfInitInstruction(typeToken: Token<*>): InitInstruction {
        if (!lexer.currentTokenIs(TokenType.IDENTIFIER))
            throw UnexpectedTokenException(
                Parser::parseRestOfInitInstruction.name,
                listOf(TokenType.IDENTIFIER),
                lexer.getToken()
            )
        val identifier = lexer.getTokenAndMoveToNext()
        val assignmentExpr = tryParseAssignment()
            ?: throw UnexpectedTokenException(
                Parser::parseRestOfInitInstruction.name,
                listOf(TokenType.ASSIGN),
                lexer.getToken()
            )
        return InitInstruction(typeToken, identifier, assignmentExpr)
    }

    private fun tryParseReturnInstruction(): ReturnInstruction? {
        if (!lexer.currentTokenIs(TokenType.RETURN)) return null
        lexer.getNextToken()

        return ReturnInstruction(parseExpression())
    }

    private fun tryParseAssignment(): Expression? {
        if (!lexer.currentTokenIs(TokenType.ASSIGN))
            return null
        lexer.getNextToken()

        return parseExpression()
    }

    private fun parseExpression(): Expression {
        var leftExpression: Expression = parseMultiplicationExpression()

        var operator: Token<*>?
        var rightExpression: Expression?
        while (lexer.currentTokenIs(listOf(TokenType.ADD, TokenType.SUBTRACT))) {
            operator = lexer.getTokenAndMoveToNext()
            rightExpression = parseMultiplicationExpression()
            val expression = AdditionExpression(leftExpression, operator, rightExpression)
            leftExpression = expression
        }

        return leftExpression
    }

    private fun parseMultiplicationExpression(): MultiplicationExpression {
        var leftFactor = parseFactor()

        var operator: Token<*>?
        var rightFactor: Factor?
        while (lexer.currentTokenIs(listOf(TokenType.MULTIPLY, TokenType.DIVIDE))) {
            operator = lexer.getTokenAndMoveToNext()
            rightFactor = parseFactor()
            val expression = MultiplicationExpression(leftFactor, operator, rightFactor)
            leftFactor = Factor(false, null, expression, null, null, null)
        }

        return MultiplicationExpression(leftFactor, null, null)
    }

    private fun parseFactor(): Factor {
        val isFactorNegated = lexer.currentTokenIs(TokenType.SUBTRACT)
        if (isFactorNegated) lexer.getNextToken()

        if (lexer.currentTokenIs(TokenType.LEFT_BRACKET)) {
            lexer.getNextToken()
            val expression = parseExpression()
            if (!lexer.currentTokenIs(TokenType.RIGHT_BRACKET))
                throw UnexpectedTokenException(Parser::parseFactor.name, listOf(TokenType.RIGHT_BRACKET), lexer.getToken())
            else lexer.getNextToken()

            return Factor(isFactorNegated, null, expression, null, null, tryParseCast())
        }

        val literal = tryParseLiteral()
        if (literal != null) {
            return Factor(isFactorNegated, null, null, null, literal, tryParseCast())
        }

        val (identifier, functionCall) = parseIdentifierOrFunctionCall()
        return if (identifier != null) Factor(isFactorNegated, null, null, identifier, null, tryParseCast())
        else Factor(isFactorNegated, functionCall, null, null, null, tryParseCast())
    }

    private fun tryParseCast(): Token<*>? {
        if (lexer.currentTokenIs(TokenType.CAST)) lexer.getNextToken() else return null
        if (!lexer.currentTokenIsType())
            throw UnexpectedTokenException(
                Parser::tryParseCast.name,
                listOf(TokenType.IDENTIFIER, TokenType.INT, TokenType.FLOAT, TokenType.STRING, TokenType.BOOL),
                lexer.getToken()
            )
        return lexer.getTokenAndMoveToNext()
    }

    private fun tryParseLiteral(): Token<*>? {
        return if (lexer.currentTokenIs(
                listOf(TokenType.INT_VAL, TokenType.FLOAT_VAL, TokenType.STRING_VAL, TokenType.TRUE, TokenType.FALSE)
            )
        ) lexer.getTokenAndMoveToNext()
        else null
    }

    private fun parseIdentifierOrFunctionCall(): Pair<Token<*>?, FunctionCall?> {
        if (!lexer.currentTokenIs(TokenType.IDENTIFIER))
            throw UnexpectedTokenException(Parser::parseIdentifierOrFunctionCall.name, listOf(TokenType.IDENTIFIER), lexer.getToken())

        val identifier = lexer.getTokenAndMoveToNext()
        val functionArguments = tryParseFunctionCallArguments()

        return if (functionArguments == null) Pair(identifier, null)
        else Pair(null, FunctionCall(identifier, functionArguments))
    }

    private fun tryParseFunctionCallArguments(): List<Expression>? {
        if (!lexer.currentTokenIs(TokenType.LEFT_BRACKET))
            return null
        lexer.getNextToken()

        val arguments: MutableList<Expression> = ArrayList()
        if (!lexer.currentTokenIs(TokenType.RIGHT_BRACKET))
            arguments.add(parseExpression())

        while (!lexer.currentTokenIs(TokenType.RIGHT_BRACKET)) {
            if (!lexer.currentTokenIs(TokenType.COMMA))
                throw UnexpectedTokenException(Parser::tryParseFunctionCallArguments.name, listOf(TokenType.COMMA), lexer.getToken())
            lexer.getNextToken()

            arguments.add(parseExpression())
        }
        lexer.getNextToken()
        return arguments
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
