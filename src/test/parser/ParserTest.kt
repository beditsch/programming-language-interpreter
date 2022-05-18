package parser

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.WordSpec
import io.kotest.data.forAll
import io.kotest.data.row
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.types.beInstanceOf
import lexer.Lexer
import lexer.source.StringSource
import parser.exception.UnexpectedTokenException
import parser.model.* // ktlint-disable no-wildcard-imports
import shared.TokenType

class ParserTest : WordSpec({
    Parser::class.java.simpleName should {
        "parse program function regardless of return type" {
            val funIdentifier = "myFunction"
            val parametersWithType = "(int param1, float param2, EUR param3)"
            val funBlock = "{ return 3 + 5; }"
            val paramTypes = listOf(TokenType.INT, TokenType.FLOAT, TokenType.IDENTIFIER)
            val paramIdentifiers = listOf("param1", "param2", "param3")

            forAll(
                row("int", TokenType.INT),
                row("float", TokenType.FLOAT),
                row("string", TokenType.STRING),
                row("bool", TokenType.BOOL),
                row("EUR", TokenType.IDENTIFIER)
            ) {
                    returnType, expectedTokenType ->
                val inputString = "$returnType $funIdentifier$parametersWithType$funBlock"

                val program = parseFromString(inputString)
                program.apply {
                    functions.size shouldBe 1
                    functions[funIdentifier]?.apply {
                        this.funReturnType.tokenType shouldBe expectedTokenType
                        this.funIdentifier.value shouldBe funIdentifier

                        this.parameters.apply {
                            size shouldBe 3
                            this.mapIndexed { id, param ->
                                param.parameterIdentifier.value shouldBe paramIdentifiers[id]
                                param.parameterType?.tokenType shouldBe paramTypes[id]
                            }
                        }

                        this.functionBlock.instrAndStatementsList.apply {
                            size shouldBe 1
                            this[0] should beInstanceOf<ReturnInstruction>()
                            (this[0] as ReturnInstruction).returnExpression.apply {
                                this should beInstanceOf<AdditionExpression>()
                                (this as AdditionExpression).apply {
                                    (leftExpression as MultiplicationExpression).leftFactor.literal?.value shouldBe 3
                                    (rightExpression as MultiplicationExpression).leftFactor.literal?.value shouldBe 5
                                    operator?.tokenType shouldBe TokenType.ADD
                                }
                            }
                        } shouldNotBe null
                    }
                }
            }
        }

        "throw ${UnexpectedTokenException::class.java.simpleName} when function doesn't have an identifier" {
            val inputString = "void (int param1) {return 0;}"
            shouldThrow<UnexpectedTokenException> {
                parseFromString(inputString)
            }.apply {
                message shouldContain "IDENTIFIER"
                message shouldContain "LEFT_BRACKET"
                message shouldContain "tryParseFunction"
            }
        }

        "throw ${UnexpectedTokenException::class.java.simpleName} when function doesn't have parameters" {
            val inputString = "void myFunc {return 0;}"
            shouldThrow<UnexpectedTokenException> {
                parseFromString(inputString)
            }.apply {
                message shouldContain "LEFT_BRACKET"
                message shouldContain "parseParameters"
            }
        }

        "throw ${UnexpectedTokenException::class.java.simpleName} when function parameter doesn't have an identifier" {
            val inputString = "void myFunc(int, float param) {return 0;}"
            shouldThrow<UnexpectedTokenException> {
                parseFromString(inputString)
            }.apply {
                message shouldContain "IDENTIFIER"
                message shouldContain "parseParameter"
            }
        }

        "throw ${UnexpectedTokenException::class.java.simpleName} when function parameters are not separated by comma" {
            val inputString = "void myFunc(integer param1 float param2) {return 0;}"
            shouldThrow<UnexpectedTokenException> {
                parseFromString(inputString)
            }.apply {
                message shouldContain "COMMA"
                message shouldContain "RIGHT_BRACKET"
                message shouldContain "parseParameters"
            }
        }

        "throw ${UnexpectedTokenException::class.java.simpleName} when function parameter has wrong type" {
            val inputString = "void myFunc(integer param1, float param2) {return 0;}"
            shouldThrow<UnexpectedTokenException> {
                parseFromString(inputString)
            }.apply {
                message shouldContain "COMMA"
                message shouldContain "RIGHT_BRACKET"
                message shouldContain "parseParameters"
            }
        }

        "throw ${UnexpectedTokenException::class.java.simpleName} when function has no body" {
            val inputString = "void myFunc(int param1, float param2) void myFunc"
            shouldThrow<UnexpectedTokenException> {
                parseFromString(inputString)
            }.apply {
                message shouldContain "LEFT_CURLY_BRACKET"
                message shouldContain "parseBlock"
            }
        }

        "throw ${UnexpectedTokenException::class.java.simpleName} when instruction doesn't end with a semicolon" {
            val inputString = "void myFunc(int param1, float param2) {return 0}"
            shouldThrow<UnexpectedTokenException> {
                parseFromString(inputString)
            }.apply {
                message shouldContain "SEMICOLON"
                message shouldContain "parseBlock"
            }
        }

        "throw ${UnexpectedTokenException::class.java.simpleName} when init instruction has no '='" {
            val inputString = "void myFunc(int param1, float param2) { int myVal }"
            shouldThrow<UnexpectedTokenException> {
                parseFromString(inputString)
            }.apply {
                message shouldContain "ASSIGN"
                message shouldContain "parseRestOfInitInstruction"
            }
        }

        "throw ${UnexpectedTokenException::class.java.simpleName} when condition in statement has no parentheses" {
            val inputString = "void myFunc(int param1, float param2) { if myVal>0 return 0; }"
            shouldThrow<UnexpectedTokenException> {
                parseFromString(inputString)
            }.apply {
                message shouldContain "LEFT_BRACKET"
                message shouldContain "parseConditionInParentheses"
            }
        }

        "throw ${UnexpectedTokenException::class.java.simpleName} when condition in statement has no right bracket" {
            val inputString = "void myFunc(int param1, float param2) { if (myVal>0 return 0; }"
            shouldThrow<UnexpectedTokenException> {
                parseFromString(inputString)
            }.apply {
                message shouldContain "RIGHT_BRACKET"
                message shouldContain "parseConditionInParentheses"
            }
        }

        "throw ${UnexpectedTokenException::class.java.simpleName} when factor has left and doesn't have right bracket" {
            val inputString = "void myFunc(int param1, float param2) { int myVal = (3+5)-(3 ;}"
            shouldThrow<UnexpectedTokenException> {
                parseFromString(inputString)
            }.apply {
                message shouldContain "RIGHT_BRACKET"
                message shouldContain "parseFactor"
            }
        }

        "throw ${UnexpectedTokenException::class.java.simpleName} when cast operator is not followed by type" {
            val inputString = "void myFunc(int param1, float param2) { int myVal = 3; EUR myValEUR = myVal as not_type}"
            shouldThrow<UnexpectedTokenException> {
                parseFromString(inputString)
            }.apply {
                message shouldContain "IDENTIFIER"
                message shouldContain "INT"
                message shouldContain "FLOAT"
                message shouldContain "STRING"
                message shouldContain "BOOL"
                message shouldContain "tryParseCast"
            }
        }

        "throw ${UnexpectedTokenException::class.java.simpleName} when function call args aren't separated with comma" {
            val inputString = "void myFunc(int param1, float param2) { int myVal = myFunc(param1 param2)}"
            shouldThrow<UnexpectedTokenException> {
                parseFromString(inputString)
            }.apply {
                message shouldContain "COMMA"
                message shouldContain "tryParseFunctionCallArguments"
            }
        }

        "parse simple program" {
            val inputString = "int main () {\n" +
                "\t\tPLN salary = 1000.0;\n" +
                "\t\tUSD dividend = 12.0;\n" +
                "\t\tUSD salary_usd = salary as PLN;\n" +
                "\t\t\n" +
                "\t\tPLN budget = salary + dividend; \n" +
                "\t\t\n" +
                "\t\tif (salary == salary_usd) {\n" +
                "\t\t\tdo_sth();\n" +
                "\t\t}\n" +
                "\t}"

            val program = parseFromString(inputString)
            program.functions.size shouldBe 1
            program.functions["main"]?.apply {
                funReturnType.tokenType shouldBe TokenType.INT
                funIdentifier.value shouldBe "main"

                parameters.size shouldBe 0

                functionBlock.instrAndStatementsList.apply {
                    size shouldBe 5
                    this[2].apply {
                        this should beInstanceOf<InitInstruction>()
                        (this as InitInstruction).apply {
                            type.tokenType shouldBe TokenType.IDENTIFIER
                            type.value shouldBe "USD"
                            identifier.tokenType shouldBe TokenType.IDENTIFIER
                            identifier.value shouldBe "salary_usd"
                            assignmentExpression should beInstanceOf<MultiplicationExpression>()
                            (assignmentExpression as MultiplicationExpression).apply {
                                leftFactor.apply {
                                    isNegated shouldBe false
                                    functionCall shouldBe null
                                    expression shouldBe null
                                    identifier?.tokenType shouldBe TokenType.IDENTIFIER
                                    identifier?.value shouldBe "salary"
                                    shouldCastTo?.tokenType shouldBe TokenType.IDENTIFIER
                                    shouldCastTo?.value shouldBe "PLN"
                                }
                                operator shouldBe null
                                rightFactor shouldBe null
                            }
                        }
                    }
                    this[4].apply {
                        this should beInstanceOf<IfStatement>()
                        (this as IfStatement).apply {
                            condition should beInstanceOf<Condition>()
                            (condition as Condition).apply {
                                leftCond should beInstanceOf<NotCondition>()
                                (leftCond as NotCondition).apply {
                                    isNegated shouldBe false
                                    expression should beInstanceOf<MultiplicationExpression>()
                                    (expression as MultiplicationExpression).apply {
                                        leftFactor.identifier?.value shouldBe "salary"
                                        rightFactor shouldBe null
                                        operator shouldBe null
                                    }
                                }
                                operator?.tokenType shouldBe TokenType.EQUAL
                                rightCond should beInstanceOf<NotCondition>()
                                (rightCond as NotCondition).apply {
                                    isNegated shouldBe false
                                    expression should beInstanceOf<MultiplicationExpression>()
                                    (expression as MultiplicationExpression).apply {
                                        leftFactor.identifier?.value shouldBe "salary_usd"
                                        rightFactor shouldBe null
                                        operator shouldBe null
                                    }
                                }
                                operator?.tokenType shouldBe TokenType.EQUAL
                            }
                        }
                    }
                }
            } shouldNotBe null
        }
    }
})

private fun parseFromString(inputString: String): Program {
    val source = StringSource(inputString)

    val lexer = Lexer(source)
    val parser = Parser(lexer)

    return parser.parse()
}
