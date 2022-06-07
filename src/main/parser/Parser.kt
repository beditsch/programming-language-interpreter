package parser

import lexer.Lexer
import parser.exception.* // ktlint-disable no-wildcard-imports
import parser.model.* // ktlint-disable no-wildcard-imports
import parser.model.Function
import parser.model.arithmetic.AdditionExpression
import parser.model.arithmetic.DivisionExpression
import parser.model.arithmetic.MultiplicationExpression
import parser.model.arithmetic.SubtractionExpression
import parser.model.condition.* // ktlint-disable no-wildcard-imports
import shared.Token
import shared.TokenType
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class Parser(
    private val lexer: Lexer,
    private val functions: HashMap<String, Function> = HashMap()
) {
    companion object {
        val tokenTypeToExpressionConstructorMap = hashMapOf<TokenType, (Expression, Expression) -> Expression>(
            TokenType.NOT_EQUAL to ::NotEqualCondition,
            TokenType.EQUAL to ::EqualCondition,
            TokenType.GREATER to ::GreaterCondition,
            TokenType.GREATER_OR_EQUAL to ::GreaterOrEqualCondition,
            TokenType.LESS to ::LessCondition,
            TokenType.LESS_OR_EQUAL to ::LessOrEqualCondition,
            TokenType.ADD to ::AdditionExpression,
            TokenType.SUBTRACT to ::SubtractionExpression,
            TokenType.MULTIPLY to ::MultiplicationExpression,
            TokenType.DIVIDE to ::DivisionExpression,
        )
    }

    init {
        lexer.getNextToken()
    }

    fun parse(): Program {
        while (tryParseFunction());
        return Program(functions)
    }

    private fun tryParseFunction(): Boolean {
        if (!lexer.currentTokenIsFunctionReturnType()) return false
        val functionReturnType = lexer.getTokenAndMoveToNext()

        if (!lexer.currentTokenIs(TokenType.IDENTIFIER))
            throw UnexpectedTokenException(Parser::tryParseFunction.name, listOf(TokenType.IDENTIFIER), lexer.getToken())
        val functionIdentifierToken = lexer.getTokenAndMoveToNext()
        val functionIdentifier = functionIdentifierToken.value.toString()

        if (!lexer.consumeIfCurrentTokenIs(TokenType.LEFT_BRACKET))
            throw UnexpectedTokenException(Parser::tryParseFunction.name, listOf(TokenType.LEFT_BRACKET), lexer.getToken())

        val parameters = parseParameters()

        if (!lexer.consumeIfCurrentTokenIs(TokenType.RIGHT_BRACKET))
            throw UnexpectedTokenException(Parser::tryParseFunction.name, listOf(TokenType.RIGHT_BRACKET), lexer.getToken())

        val block = tryParseBlock() ?: throw MissingFunctionBlockException(functionIdentifier, lexer.getToken()?.position)

        if (functions.containsKey(functionIdentifier))
            throw DuplicateFunctionDefinitionException(functionIdentifier, functionIdentifierToken.position)
        functions[functionIdentifier] = Function(VariableType(functionReturnType), functionIdentifier, parameters, block)
        return true
    }

    private fun parseParameters(): List<Parameter> {
        val parameters: MutableList<Parameter> = ArrayList()

        val firstParam = tryParseParameter() ?: return parameters
        parameters.add(firstParam)

        while (lexer.consumeIfCurrentTokenIs(TokenType.COMMA)) {
            val param = tryParseParameter() ?: throw MissingParameterException(lexer.getToken()?.position)
            parameters.add(param)
        }

        return parameters
    }

    private fun tryParseParameter(): Parameter? = lexer.run {
        if (!currentTokenIsType() && !currentTokenIs(TokenType.IDENTIFIER))
            return null

        return if (currentTokenIs(TokenType.IDENTIFIER)) {
            val paramIdentifier = getTokenAndMoveToNext().value.toString()
            Parameter(null, paramIdentifier)
        } else {
            val paramType = getTokenAndMoveToNext()
            if (!currentTokenIs(TokenType.IDENTIFIER))
                throw UnexpectedTokenException(Parser::tryParseParameter.name, listOf(TokenType.IDENTIFIER), lexer.getToken())
            val paramIdentifier = getTokenAndMoveToNext().value.toString()

            Parameter(VariableType(paramType), paramIdentifier)
        }
    }

    private fun tryParseBlock(): Block? {
        if (!lexer.consumeIfCurrentTokenIs(TokenType.LEFT_CURLY_BRACKET))
            return null

        val blockComponents: MutableList<Statement> = ArrayList()
        var instruction: Statement? = tryParseInstruction()
        var statement: Statement? = if (instruction == null) tryParseStatement() else null

        while (instruction != null || statement != null) {
            if (statement != null) {
                blockComponents.add(statement)
            }
            if (instruction != null) {
                blockComponents.add(instruction)
                if (!lexer.consumeIfCurrentTokenIs(TokenType.SEMICOLON))
                    throw UnexpectedTokenException(Parser::tryParseBlock.name, listOf(TokenType.SEMICOLON), lexer.getToken())
            }

            instruction = tryParseInstruction()
            statement = if (instruction == null) tryParseStatement() else null
        }

        if (!lexer.consumeIfCurrentTokenIs(TokenType.RIGHT_CURLY_BRACKET))
            throw UnexpectedTokenException(Parser::tryParseBlock.name, listOf(TokenType.RIGHT_CURLY_BRACKET), lexer.getToken())

        return Block(blockComponents)
    }

    private fun tryParseInstruction(): Statement? {
        val assignInstrOrFunctionCallOrInitInstr = tryParseAssignInstructionOrFunctionCall()
        if (assignInstrOrFunctionCallOrInitInstr != null) return assignInstrOrFunctionCallOrInitInstr

        val returnInstruction = tryParseReturnInstruction()
        if (returnInstruction != null) return returnInstruction

        val initInstruction = tryParseInitInstruction()
        if (initInstruction != null) return initInstruction

        return tryParseBlock()
    }

    private fun tryParseStatement(): Statement? {
        val ifStatement = tryParseIfStatement()
        if (ifStatement != null) return ifStatement

        val whileStatement = tryParseWhileStatement()
        if (whileStatement != null) return whileStatement

        return null
    }

    private fun tryParseIfStatement(): IfStatement? {
        if (!lexer.consumeIfCurrentTokenIs(TokenType.IF))
            return null

        val condition = tryParseConditionInParentheses()
            ?: throw UnexpectedTokenException(Parser::tryParseIfStatement.name, listOf(TokenType.LEFT_BRACKET), lexer.getToken())

        val instruction = tryParseInstruction()
            ?: throw MissingInstructionException(Parser::tryParseIfStatement.name, lexer.getToken()?.position)

        if (!lexer.consumeIfCurrentTokenIs(TokenType.ELSE))
            return IfStatement(condition, instruction, null)

        val elseInstruction = tryParseInstruction()
            ?: throw MissingInstructionException(Parser::tryParseIfStatement.name, lexer.getToken()?.position)
        return IfStatement(condition, instruction, elseInstruction)
    }

    private fun tryParseWhileStatement(): WhileStatement? {
        if (!lexer.consumeIfCurrentTokenIs(TokenType.WHILE))
            return null

        val condition = tryParseConditionInParentheses()
            ?: throw UnexpectedTokenException(Parser::tryParseWhileStatement.name, listOf(TokenType.LEFT_BRACKET), lexer.getToken())
        val block = tryParseBlock()
            ?: throw MissingBlockException(Parser::tryParseWhileStatement.name, lexer.getToken()?.position)

        return WhileStatement(condition, block)
    }

    private fun tryParseInitInstruction(): Statement? {
        if (!lexer.currentTokenIsType())
            return null

        val type = lexer.getTokenAndMoveToNext()
        if (!lexer.currentTokenIs(TokenType.IDENTIFIER))
            throw UnexpectedTokenException(
                Parser::tryParseInitInstruction.name,
                listOf(TokenType.IDENTIFIER),
                lexer.getToken()
            )
        val identifier = lexer.getTokenAndMoveToNext().value.toString()
        val assignmentExpr = tryParseAssignment()
            ?: throw UnexpectedTokenException(
                Parser::tryParseInitInstruction.name,
                listOf(TokenType.ASSIGN),
                lexer.getToken()
            )
        return InitInstruction(VariableType(type), identifier, assignmentExpr)
    }

    private fun tryParseAssignInstructionOrFunctionCall(): Statement? {
        if (!lexer.currentTokenIs(TokenType.IDENTIFIER))
            return null

        val identifier = lexer.getTokenAndMoveToNext().value.toString()
        val functionCallArguments = tryParseFunctionCallArguments()
        if (functionCallArguments != null)
            return FunctionCall(identifier, functionCallArguments)

        val assignmentExpression = tryParseAssignment()
        if (assignmentExpression != null)
            return AssignInstruction(identifier, assignmentExpression)

        return null
    }

    private fun tryParseReturnInstruction(): ReturnInstruction? {
        if (!lexer.consumeIfCurrentTokenIs(TokenType.RETURN)) return null
        val expression = tryParseExpression()
            ?: throw MissingExpressionException(Parser::tryParseReturnInstruction.name, lexer.getToken()?.position)

        return ReturnInstruction(expression)
    }

    private fun tryParseAssignment(): Expression? {
        if (!lexer.consumeIfCurrentTokenIs(TokenType.ASSIGN)) return null

        return tryParseExpression()
            ?: throw MissingExpressionException(Parser::tryParseAssignment.name, lexer.getToken()?.position)
    }

    private fun tryParseConditionInParentheses(): Expression? {
        if (!lexer.consumeIfCurrentTokenIs(TokenType.LEFT_BRACKET))
            return null

        val condition = tryParseCondition()
            ?: throw MissingConditionException(Parser::tryParseConditionInParentheses.name, lexer.getToken()?.position)

        if (!lexer.consumeIfCurrentTokenIs(TokenType.RIGHT_BRACKET))
            throw UnexpectedTokenException(Parser::tryParseConditionInParentheses.name, listOf(TokenType.RIGHT_BRACKET), lexer.getToken())

        return condition
    }

    private fun tryParseCondition(): Expression? {
        var leftCondition = tryParseAndCondition() ?: return null

        while (lexer.consumeIfCurrentTokenIs(TokenType.OR)) {
            val rightCondition = tryParseAndCondition()
                ?: throw MissingConditionException(Parser::tryParseCondition.name, lexer.getToken()?.position)
            leftCondition = OrCondition(leftCondition, rightCondition)
        }

        return leftCondition
    }

    private fun tryParseAndCondition(): Expression? {
        var leftCondition = tryParseComparisonCondition() ?: return null

        while (lexer.consumeIfCurrentTokenIs(TokenType.AND)) {
            val rightCondition = tryParseComparisonCondition()
                ?: throw MissingConditionException(Parser::tryParseAndCondition.name, lexer.getToken()?.position)
            leftCondition = AndCondition(leftCondition, rightCondition)
        }

        return leftCondition
    }

    private fun tryParseComparisonCondition(): Expression? {
        val leftRelCondition = tryParseRelationalCondition() ?: return null
        if (!lexer.currentTokenIs(listOf(TokenType.EQUAL, TokenType.NOT_EQUAL)))
            return leftRelCondition

        val operator = lexer.getTokenAndMoveToNext()
        val rightRelCondition = tryParseRelationalCondition()
            ?: throw MissingConditionException(Parser::tryParseComparisonCondition.name, lexer.getToken()?.position)

        return tokenTypeToExpressionConstructorMap[operator.tokenType]?.let { it(leftRelCondition, rightRelCondition) }
            ?: throw MissingExpressionConstructorForTokenTypeException(operator.tokenType.toString())
    }

    private fun tryParseRelationalCondition(): Expression? {
        val notCondition = tryParseNotCondition() ?: return null
        if (!lexer.currentTokenIs(
                listOf(TokenType.GREATER, TokenType.GREATER_OR_EQUAL, TokenType.LESS, TokenType.LESS_OR_EQUAL)
            )
        ) return notCondition

        val operator = lexer.getTokenAndMoveToNext()
        val rightConditionExpression = tryParseNotCondition()
            ?: throw MissingConditionException(Parser::tryParseRelationalCondition.name, lexer.getToken()?.position)

        return tokenTypeToExpressionConstructorMap[operator.tokenType]?.let { it(notCondition, rightConditionExpression) }
            ?: throw MissingExpressionConstructorForTokenTypeException(operator.tokenType.toString())
    }

    private fun tryParseNotCondition(): Expression? {
        val isNegated = lexer.consumeIfCurrentTokenIs(TokenType.NOT)
        val expression = tryParseExpression()
            ?: if (isNegated)
                throw MissingExpressionException(Parser::tryParseNotCondition.name, lexer.getToken()?.position)
            else return null

        return if (isNegated) NotCondition(expression) else expression
    }

    private fun tryParseExpression(): Expression? {
        var leftExpression: Expression = tryParseMultiplicationExpression() ?: return null

        var operator: Token<*>?
        var rightExpression: Expression
        while (lexer.currentTokenIs(listOf(TokenType.ADD, TokenType.SUBTRACT))) {
            operator = lexer.getTokenAndMoveToNext()
            rightExpression = tryParseMultiplicationExpression()
                ?: throw MissingExpressionException(Parser::tryParseExpression.name, lexer.getToken()?.position)
            val expression = tokenTypeToExpressionConstructorMap[operator.tokenType]?.let { it(leftExpression, rightExpression) }
                ?: throw MissingExpressionConstructorForTokenTypeException(operator.tokenType.toString())
            leftExpression = expression
        }

        return leftExpression
    }

    private fun tryParseMultiplicationExpression(): Expression? {
        var leftFactor = tryParseNegatedFactorWithCast() ?: return null

        var operator: Token<*>?
        var rightFactor: Expression
        while (lexer.currentTokenIs(listOf(TokenType.MULTIPLY, TokenType.DIVIDE))) {
            operator = lexer.getTokenAndMoveToNext()
            rightFactor = tryParseNegatedFactorWithCast()
                ?: throw MissingExpressionException(Parser::tryParseMultiplicationExpression.name, lexer.getToken()?.position)
            val expression = tokenTypeToExpressionConstructorMap[operator.tokenType]?.let { it(leftFactor, rightFactor) }
                ?: throw MissingExpressionConstructorForTokenTypeException(operator.tokenType.toString())
            leftFactor = expression
        }

        return leftFactor
    }

    private fun tryParseNegatedFactorWithCast(): Expression? {
        val isFactorNegated = lexer.consumeIfCurrentTokenIs(TokenType.SUBTRACT)
        val factor = tryParseFactorWithCast()
            ?: if (isFactorNegated) throw MissingExpressionException(Parser::tryParseNegatedFactorWithCast.name, lexer.getToken()?.position)
            else return null

        return if (isFactorNegated) NegatedFactor(factor) else factor
    }

    private fun tryParseFactorWithCast(): Expression? {
        val factor = tryParseFactor() ?: return null
        val castTo = tryParseCast()?.let { VariableType(it) }

        return if (castTo != null) FactorWithCast(factor, castTo) else factor
    }

    private fun tryParseFactor(): Expression? {
        val conditionInParenth = tryParseConditionInParentheses()
        if (conditionInParenth != null)
            return Factor(null, conditionInParenth, null, null)

        val literal = tryParseLiteral()
        if (literal != null) {
            return Factor(null, null, null, literal.value)
        }

        val (identifier, functionCall) = tryParseIdentifierOrFunctionCall()
        if (identifier != null)
            return Factor(null, null, identifier, null)
        if (functionCall != null)
            return Factor(functionCall, null, null, null)

        return null
    }

    private fun tryParseCast(): Token<*>? {
        if (lexer.currentTokenIs(TokenType.CAST)) lexer.getNextToken() else return null
        if (!lexer.currentTokenIsType())
            throw UnexpectedTokenException(
                Parser::tryParseCast.name,
                listOf(TokenType.CURRENCY_ID, TokenType.INT, TokenType.FLOAT, TokenType.STRING, TokenType.BOOL),
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

    private fun tryParseIdentifierOrFunctionCall(): Pair<String?, FunctionCall?> {
        if (!lexer.currentTokenIs(TokenType.IDENTIFIER))
            return Pair(null, null)

        val identifier = lexer.getTokenAndMoveToNext().value.toString()
        val functionArguments = tryParseFunctionCallArguments()

        return if (functionArguments == null) Pair(identifier, null)
        else Pair(null, FunctionCall(identifier, functionArguments))
    }

    private fun tryParseFunctionCallArguments(): List<Expression>? {
        if (!lexer.consumeIfCurrentTokenIs(TokenType.LEFT_BRACKET))
            return null

        val arguments: MutableList<Expression> = ArrayList()
        val firstArg = tryParseExpression()
            ?: if (!lexer.consumeIfCurrentTokenIs(TokenType.RIGHT_BRACKET))
                throw UnexpectedTokenException(Parser::tryParseFunctionCallArguments.name, listOf(TokenType.RIGHT_BRACKET), lexer.getToken())
            else return arguments
        arguments.add(firstArg)

        while (lexer.consumeIfCurrentTokenIs(TokenType.COMMA)) {
            val arg = tryParseExpression()
                ?: throw MissingExpressionException(Parser::tryParseFunctionCallArguments.name, lexer.getToken()?.position)
            arguments.add(arg)
        }

        if (!lexer.consumeIfCurrentTokenIs(TokenType.RIGHT_BRACKET))
            throw UnexpectedTokenException(Parser::tryParseFunctionCallArguments.name, listOf(TokenType.RIGHT_BRACKET), lexer.getToken())
        return arguments
    }
}

private fun Lexer.currentTokenIs(tokenType: TokenType): Boolean =
    this.getToken()?.tokenType == tokenType

private fun Lexer.consumeIfCurrentTokenIs(tokenType: TokenType): Boolean {
    return if (currentTokenIs(tokenType)) {
        getNextToken()
        true
    } else false
}

private fun Lexer.currentTokenIs(tokenTypes: List<TokenType>): Boolean =
    tokenTypes.contains(this.getToken()?.tokenType)

private fun Lexer.currentTokenIsFunctionReturnType(): Boolean =
    currentTokenIsType() || currentTokenIs(TokenType.VOID)

private fun Lexer.currentTokenIsType(): Boolean =
    currentTokenIs(listOf(TokenType.INT, TokenType.FLOAT, TokenType.STRING, TokenType.BOOL, TokenType.CURRENCY_ID))

private fun Lexer.getTokenAndMoveToNext(): Token<*> {
    val currentToken = this.getToken() ?: throw ParsingException("Unexpected null token.")
    this.getNextToken()
    return currentToken
}
