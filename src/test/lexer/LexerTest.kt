package lexer

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.WordSpec
import io.kotest.data.forAll
import io.kotest.data.row
import io.kotest.matchers.shouldBe
import lexer.exception.LexerException
import lexer.source.FileSource
import lexer.source.StringSource
import shared.Position
import shared.Token
import shared.TokenType

class LexerTest : WordSpec({
    Lexer::class.java.simpleName should {
        "throw ${LexerException::class.java.simpleName}" {
            val source = StringSource("   \" ndhjavu ")
            val lexer = Lexer(source)
            shouldThrow<LexerException> {
                lexer.getNextToken()
            }.apply {
                message shouldBe "Unexpected ETX encountered."
            }
        }

        "build tokens" {
            forAll(
                row(" /", TokenType.DIVIDE, null),
                row("!=", TokenType.NOT_EQUAL, null),
                row("&&", TokenType.AND, null),
                row("return 345", TokenType.RETURN, null),
                row("return345", TokenType.IDENTIFIER, "return345"),
                row("0.45556", TokenType.FLOAT_VAL, 0.45556),
                row("0", TokenType.INT_VAL, 0),
                row("12.445", TokenType.FLOAT_VAL, 12.445),
                row("123", TokenType.INT_VAL, 123),
                row("     #blabla bla \n", TokenType.COMMENT, "blabla bla "),
                row("==", TokenType.EQUAL, null),
                row("=", TokenType.ASSIGN, null),
            ) {
                    sourceString, tokenType, tokenValue ->
                val source = StringSource(sourceString)
                val lexer = Lexer(source)
                lexer.getNextToken()!!.apply {
                    this.tokenType shouldBe tokenType
                    this.value shouldBe tokenValue
                }
            }
        }

        "process input file" {
            val source = FileSource(System.getProperty("user.dir") + "/src/test/lexer/LexerTestInputFile.txt")
            val lexer = Lexer(source)
            val position = Position(0, 0)
            val expectedTokens: ArrayList<Token<Any>> = ArrayList()
            expectedTokens.addAll(
                listOf(
                    Token(TokenType.INT, position, null),
                    Token(TokenType.IDENTIFIER, position, "main"),
                    Token(TokenType.LEFT_BRACKET, position, null),
                    Token(TokenType.RIGHT_BRACKET, position, null),
                    Token(TokenType.LEFT_CURLY_BRACKET, position, null),
                    Token(TokenType.IDENTIFIER, position, "PLN"),
                    Token(TokenType.IDENTIFIER, position, "salary"),
                    Token(TokenType.ASSIGN, position, null),
                    Token(TokenType.FLOAT_VAL, position, 1200.0),
                    Token(TokenType.SEMICOLON, position, null),
                    Token(TokenType.IDENTIFIER, position, "PLN"),
                    Token(TokenType.IDENTIFIER, position, "tips"),
                    Token(TokenType.ASSIGN, position, null),
                    Token(TokenType.FLOAT_VAL, position, 15.0),
                    Token(TokenType.SEMICOLON, position, null),
                    Token(TokenType.IDENTIFIER, position, "EUR"),
                    Token(TokenType.IDENTIFIER, position, "dividend"),
                    Token(TokenType.ASSIGN, position, null),
                    Token(TokenType.FLOAT_VAL, position, 12.0),
                    Token(TokenType.SEMICOLON, position, null),
                    Token(TokenType.IDENTIFIER, position, "GBP"),
                    Token(TokenType.IDENTIFIER, position, "rent"),
                    Token(TokenType.ASSIGN, position, null),
                    Token(TokenType.SUBTRACT, position, null),
                    Token(TokenType.FLOAT_VAL, position, 100.0),
                    Token(TokenType.SEMICOLON, position, null),
                    Token(TokenType.IDENTIFIER, position, "USD"),
                    Token(TokenType.IDENTIFIER, position, "my_budget"),
                    Token(TokenType.ASSIGN, position, null),
                    Token(TokenType.IDENTIFIER, position, "monthly_budget"),
                    Token(TokenType.LEFT_BRACKET, position, null),
                    Token(TokenType.IDENTIFIER, position, "salary"),
                    Token(TokenType.COMMA, position, null),
                    Token(TokenType.IDENTIFIER, position, "tips"),
                    Token(TokenType.COMMA, position, null),
                    Token(TokenType.IDENTIFIER, position, "dividend"),
                    Token(TokenType.COMMA, position, null),
                    Token(TokenType.IDENTIFIER, position, "rent"),
                    Token(TokenType.RIGHT_BRACKET, position, null),
                    Token(TokenType.SEMICOLON, position, null),
                    Token(TokenType.IDENTIFIER, position, "print"),
                    Token(TokenType.LEFT_BRACKET, position, null),
                    Token(TokenType.STRING_VAL, position, "Mam do dyspozycji "),
                    Token(TokenType.ADD, position, null),
                    Token(TokenType.IDENTIFIER, position, "my_budget"),
                    Token(TokenType.ADD, position, null),
                    Token(TokenType.IDENTIFIER, position, "typeof"),
                    Token(TokenType.LEFT_BRACKET, position, null),
                    Token(TokenType.IDENTIFIER, position, "my_budget"),
                    Token(TokenType.RIGHT_BRACKET, position, null),
                    Token(TokenType.ADD, position, null),
                    Token(TokenType.STRING_VAL, position, " w tym miesiącu."),
                    Token(TokenType.RIGHT_BRACKET, position, null),
                    Token(TokenType.SEMICOLON, position, null),
                    Token(TokenType.RIGHT_CURLY_BRACKET, position, null),
                    Token(TokenType.ETX, position, null)
                )
            )
            expectedTokens.map {
                val token = lexer.getNextToken()
                it.tokenType shouldBe token?.tokenType
                it.value shouldBe token?.value
            }
        }
    }
})
