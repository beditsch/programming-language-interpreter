package lexer

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.WordSpec
import io.kotest.data.forAll
import io.kotest.data.row
import io.kotest.matchers.shouldBe
import lexer.exception.LexerException
import lexer.source.StringSource
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
                row("123", TokenType.INT_VAL, 123)
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
    }
})
