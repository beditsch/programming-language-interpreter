package lexer

import lexer.exception.LexerException
import lexer.source.Source
import shared.Position
import shared.Token
import shared.TokenType
import java.lang.StringBuilder
import kotlin.math.pow

class Lexer(private val source: Source) {
    private var currentChar: Char? = source.getChar()
    private var currentPosition = Position(0, 0)
    private var currentToken: Token<*>? = null

    fun getToken(): Token<*>? { return currentToken }

    // TODO
    fun getNextToken(): Token<*>? {
        skipWhites()
        if (tryBuildETX()) return currentToken
        if (tryBuildString()) return currentToken
        if (tryBuildOperator()) return currentToken
        if (tryBuildKeywordOrIdentifier()) return currentToken
        if (tryBuildNumber()) return currentToken
        if (tryBuildComment()) return currentToken
        throw LexerException("Cannot classify token.", currentPosition)
    }

    private fun tryBuildETX(): Boolean {
        return if (currentChar == null) {
            currentToken = Token<Any>(TokenType.ETX, currentPosition)
            true
        } else false
    }

    private fun tryBuildString(): Boolean {
        return if (currentChar != '\"') false
        else {
            var tokenValueBuilder = StringBuilder()
            getNextChar()
            while (currentChar != '\"') {
                currentChar?.apply {
                    if (this == '\\') getNextChar()
                    else tokenValueBuilder.append(this)
                } ?: throw LexerException("Unexpected ETX encountered.", currentPosition)
                getNextChar()
            }
            getNextChar()
            currentToken = Token(TokenType.STRING_VAL, currentPosition, tokenValueBuilder.toString())
            true
        }
    }

    private fun tryBuildOperator(): Boolean {
        val operatorDictionary = TokenType.getOperatorDict()
        if (!operatorDictionary.containsKey(currentChar.toString()) && currentChar != '&' && currentChar != '|') {
            return false
        } else {
            val operatorBuilder = StringBuilder()
            operatorBuilder.append(currentChar)
            getNextChar()
            operatorBuilder.append(currentChar)
            // if the operator is a two-char operator
            if (operatorDictionary.containsKey(operatorBuilder.toString())) {
                val operator = operatorBuilder.toString()
                currentToken = operatorDictionary[operator]?.let { Token<Any>(it, currentPosition) }
                    ?: throw LexerException("Unexpected character null value encountered.", currentPosition)
                getNextChar()
            } else {
                val operator = operatorBuilder.substring(0, 1)
                currentToken = operatorDictionary[operator]?.let { Token<Any>(it, currentPosition) }
                    ?: throw LexerException("Unexpected character null value encountered.", currentPosition)
            }
            return true
        }
    }

    private fun tryBuildKeywordOrIdentifier(): Boolean {
        if (currentChar?.isLetter() != true) return false
        else {
            val keywordsDictionary = TokenType.getKeywordsDict()
            val wordBuilder = StringBuilder()
            while (currentChar?.isLetterOrDigit() == true || currentChar == '_') {
                wordBuilder.append(currentChar)
                getNextChar()
            }
            val word = wordBuilder.toString()
            val tokenType = keywordsDictionary[word]
            currentToken = when (tokenType) {
                TokenType.TRUE -> Token(TokenType.TRUE, currentPosition, true)
                TokenType.FALSE -> Token(TokenType.FALSE, currentPosition, false)
                // TODO: should we distinguish currencies here or can we do it in the parser?
                null -> Token(TokenType.IDENTIFIER, currentPosition, word)
                else -> Token<Any>(tokenType, currentPosition)
            }
            return true
        }
    }

    private fun tryBuildNumber(): Boolean {
        if (currentChar?.isDigit() != true) return false
        else {
            var intPart = 0
            if (currentChar != '0') {
                while (currentChar?.isDigit() == true) {
                    intPart = intPart * 10 + currentChar!!.digitToInt()
                    getNextChar()
                }
            } else getNextChar()
            if (currentChar == '.') {
                var fractionPart = 0.0
                var decimalPlaces = 0
                getNextChar()
                while (currentChar?.isDigit() == true) {
                    fractionPart = fractionPart * 10 + currentChar!!.digitToInt()
                    decimalPlaces++
                    getNextChar()
                }
                fractionPart /= 10.0.pow(decimalPlaces)
                currentToken = Token(TokenType.FLOAT_VAL, currentPosition, intPart + fractionPart)
            } else {
                currentToken = Token(TokenType.INT_VAL, currentPosition, intPart)
            }
            return true
        }
    }

    private fun tryBuildComment(): Boolean {
        if (currentChar != '#') return false
        else {
            val commentBuilder = StringBuilder()
            getNextChar()
            while (currentChar != '\n' && currentChar != null) {
                commentBuilder.append(currentChar)
                getNextChar()
            }
            currentToken = Token(TokenType.COMMENT, currentPosition, commentBuilder.toString())
            return true
        }
    }

    private fun skipWhites() {
        while (currentChar?.isWhitespace() == true) { getNextChar() }
    }

    private fun getNextChar() {
        if (currentChar == '\n') currentPosition.moveLine()
        else currentPosition.moveColumn()
        currentChar = source.getChar()
    }
}
