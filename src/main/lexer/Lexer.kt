package lexer

import lexer.exception.LexerException
import shared.Position
import shared.Token
import shared.TokenType
import java.io.File
import java.io.InputStreamReader
import java.lang.StringBuilder
import java.nio.charset.Charset

class Lexer(sourceFile: File) {
    // TODO: abstract source
    private var sourceFileReader: InputStreamReader = sourceFile.reader(Charset.defaultCharset())
    private var currentChar: Char? = sourceFileReader.getChar()
    private var currentPosition = Position(0, 0)
    private var currentToken: Token<*>? = null

    fun getToken(): Token<*>? { return currentToken }

    // TODO
    fun getNextToken(): Token<*>? {
        if (tryBuildETX()) return currentToken
        if (tryBuildString()) return currentToken
        return currentToken
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
            currentToken = Token(TokenType.STRING, currentPosition, tokenValueBuilder.toString())
            true
        }
    }

    private fun skipWhites() {
        while (currentChar?.isWhitespace() == true) { getNextChar() }
    }

    private fun getNextChar() {
        if (currentChar == '\n') currentPosition.moveLine()
        else currentPosition.moveColumn()
        currentChar = sourceFileReader.getChar()
    }
}

fun InputStreamReader.getChar(): Char? {
    this.read().apply {
        return if (this == -1) null
        else this.toChar()
    }
}
