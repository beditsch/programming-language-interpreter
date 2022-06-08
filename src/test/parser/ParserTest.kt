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
import parser.exception.* // ktlint-disable no-wildcard-imports
import parser.model.* // ktlint-disable no-wildcard-imports
import parser.model.arithmetic.AdditionExpression
import parser.model.condition.EqualCondition
import parser.model.condition.GreaterCondition

class ParserTest : WordSpec({
    Parser::class.java.simpleName should {
        "parse program function regardless of return type" {
            val funIdentifier = "myFunction"
            val parametersWithType = "(int param1, float param2, EUR param3)"
            val funBlock = "{ return 3 + 5; }"
            val paramTypes = listOf(Type.INT, Type.FLOAT, Type.CURRENCY)
            val paramIdentifiers = listOf("param1", "param2", "param3")
            val paramCurrencyIds = listOf(null, null, "EUR")

            forAll(
                row("int", Type.INT),
                row("float", Type.FLOAT),
                row("string", Type.STRING),
                row("bool", Type.BOOL),
                row("EUR", Type.CURRENCY)
            ) {
                    returnType, expectedTokenType ->
                val inputString = "$returnType $funIdentifier$parametersWithType$funBlock"

                val program = parseFromString(inputString)
                program.apply {
                    functions.size shouldBe 1
                    functions[funIdentifier]?.apply {
                        this.funReturnType.type shouldBe expectedTokenType
                        this.funIdentifier shouldBe funIdentifier

                        this.parameters.apply {
                            size shouldBe 3
                            this.mapIndexed { id, param ->
                                param.parameterIdentifier shouldBe paramIdentifiers[id]
                                param.parameterType?.type shouldBe paramTypes[id]
                                param.parameterType?.currencyId shouldBe paramCurrencyIds[id]
                            }
                        }

                        this.functionBlock.instrAndStatementsList.apply {
                            size shouldBe 1
                            this[0] should beInstanceOf<ReturnInstruction>()
                            (this[0] as ReturnInstruction).returnExpression.apply {
                                this should beInstanceOf<AdditionExpression>()
                                (this as AdditionExpression).apply {
                                    (leftExpression as Factor).literal.apply {
                                        this should beInstanceOf<Int>()
                                        this shouldBe 3
                                    }
                                    (rightExpression as Factor).literal.apply {
                                        this should beInstanceOf<Int>()
                                        this shouldBe 5
                                    }
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
                message shouldContain "tryParseFunction"
            }
        }

        "throw ${UnexpectedTokenException::class.java.simpleName} when function parameter doesn't have an identifier" {
            val inputString = "void myFunc(int, float param) {return 0;}"
            shouldThrow<UnexpectedTokenException> {
                parseFromString(inputString)
            }.apply {
                message shouldContain "IDENTIFIER"
                message shouldContain "tryParseParameter"
            }
        }

        "throw ${UnexpectedTokenException::class.java.simpleName} when function parameters are not separated by comma" {
            val inputString = "void myFunc(integer param1 float param2) {return 0;}"
            shouldThrow<UnexpectedTokenException> {
                parseFromString(inputString)
            }.apply {
                message shouldContain "IDENTIFIER"
                message shouldContain "RIGHT_BRACKET"
                message shouldContain "tryParseFunction"
            }
        }

        "throw ${UnexpectedTokenException::class.java.simpleName} when function parameter has wrong type" {
            val inputString = "void myFunc(integer param1, float param2) {return 0;}"
            shouldThrow<UnexpectedTokenException> {
                parseFromString(inputString)
            }.apply {
                message shouldContain "IDENTIFIER"
                message shouldContain "RIGHT_BRACKET"
                message shouldContain "tryParseFunction"
            }
        }

        "throw ${MissingFunctionBlockException::class.java.simpleName} when function has no body" {
            val inputString = "void myFunc(int param1, float param2) void myFunc"
            shouldThrow<MissingFunctionBlockException> {
                parseFromString(inputString)
            }.apply {
                message shouldContain "myFunc"
            }
        }

        "throw ${UnexpectedTokenException::class.java.simpleName} when instruction doesn't end with a semicolon" {
            val inputString = "void myFunc(int param1, float param2) {return 0}"
            shouldThrow<UnexpectedTokenException> {
                parseFromString(inputString)
            }.apply {
                message shouldContain "SEMICOLON"
                message shouldContain "tryParseBlock"
            }
        }

        "throw ${UnexpectedTokenException::class.java.simpleName} when init instruction has no '='" {
            val inputString = "void myFunc(int param1, float param2) { int myVal }"
            shouldThrow<UnexpectedTokenException> {
                parseFromString(inputString)
            }.apply {
                message shouldContain "ASSIGN"
                message shouldContain "tryParseInitInstruction"
            }
        }

        "throw ${UnexpectedTokenException::class.java.simpleName} when condition in statement has no parentheses" {
            val inputString = "void myFunc(int param1, float param2) { if myVal>0 return 0; }"
            shouldThrow<UnexpectedTokenException> {
                parseFromString(inputString)
            }.apply {
                message shouldContain "LEFT_BRACKET"
                message shouldContain "tryParseIfStatement"
            }
        }

        "throw ${UnexpectedTokenException::class.java.simpleName} when condition in statement has no right bracket" {
            val inputString = "void myFunc(int param1, float param2) { if (myVal>0 return 0; }"
            shouldThrow<UnexpectedTokenException> {
                parseFromString(inputString)
            }.apply {
                message shouldContain "RIGHT_BRACKET"
                message shouldContain "tryParseConditionInParentheses"
            }
        }

        "throw ${UnexpectedTokenException::class.java.simpleName} when factor has left and doesn't have right bracket" {
            val inputString = "void myFunc(int param1, float param2) { int myVal = (3+5)-(3 ;}"
            shouldThrow<UnexpectedTokenException> {
                parseFromString(inputString)
            }.apply {
                message shouldContain "RIGHT_BRACKET"
                message shouldContain "tryParseConditionInParentheses"
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
                message shouldContain "RIGHT_BRACKET"
                message shouldContain "tryParseFunctionCallArguments"
            }
        }

        "throw ${DuplicateFunctionDefinitionException::class.java.simpleName} when two functions have the same identifier" {
            val fun1 = "void myFunc(int param1, float param2) { int myVal = myFunc(param1, param2);}"
            val fun2 = "void myFunc(float param1, bool param2) { int myVal = myFunc(param1, param2);}"
            val inputString = "$fun1 $fun2"
            shouldThrow<DuplicateFunctionDefinitionException> {
                parseFromString(inputString)
            }.apply {
                message shouldContain "myFunc"
            }
        }

        "throw ${MissingParameterException::class.java.simpleName} when there is no function parameter after comma" {
            val inputString = "void myFunc(int param1, float param2, ) { int myVal = myFunc(param1, param2);}"
            shouldThrow<MissingParameterException> {
                parseFromString(inputString)
            }
        }

        "throw ${UnexpectedTokenException::class.java.simpleName} when function block doesn't end with a right curly bracket" {
            val inputString = "void myFunc(int param1, float param2) { int myVal = myFunc(param1, param2);"
            shouldThrow<UnexpectedTokenException> {
                parseFromString(inputString)
            }.apply {
                message shouldContain "RIGHT_CURLY_BRACKET"
            }
        }

        "throw ${MissingInstructionException::class.java.simpleName} when if condition is not followed by instruction" {
            val inputString = "void myFunc(int param1, float param2) { if (true) }"
            shouldThrow<MissingInstructionException> {
                parseFromString(inputString)
            }
        }

        "throw ${MissingInstructionException::class.java.simpleName} when else keyword is not followed by instruction" {
            val inputString = "void myFunc(int param1, float param2) { if (false) {} else }"
            shouldThrow<MissingInstructionException> {
                parseFromString(inputString)
            }
        }

        "throw ${UnexpectedTokenException::class.java.simpleName} when while statement has no condition" {
            val inputString = "void myFunc(int param1, float param2) { while { int a = 3 }}"
            shouldThrow<UnexpectedTokenException> {
                parseFromString(inputString)
            }.apply {
                message shouldContain "LEFT_BRACKET"
            }
        }

        "throw ${MissingBlockException::class.java.simpleName} when while statement has no block" {
            val inputString = "void myFunc(int param1, float param2) { while (true) }"
            shouldThrow<MissingBlockException> {
                parseFromString(inputString)
            }.apply {
                message shouldContain "tryParseWhileStatement"
            }
        }

        "throw ${UnexpectedTokenException::class.java.simpleName} when init instruction has no variable identifier" {
            val inputString = "void myFunc(int param1, float param2) { float = 3.0 }"
            shouldThrow<UnexpectedTokenException> {
                parseFromString(inputString)
            }.apply {
                message shouldContain "IDENTIFIER"
            }
        }

        "throw ${UnexpectedTokenException::class.java.simpleName} when init instruction has assignment token" {
            val inputString = "void myFunc(int param1, float param2) { float myVar 3.0 }"
            shouldThrow<UnexpectedTokenException> {
                parseFromString(inputString)
            }.apply {
                message shouldContain "ASSIGN"
            }
        }

        "parse simple program with if statement" {
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
                funReturnType.type shouldBe Type.INT
                funIdentifier shouldBe "main"

                parameters.size shouldBe 0

                functionBlock.instrAndStatementsList.apply {
                    size shouldBe 5
                    this[2].apply {
                        this should beInstanceOf<InitInstruction>()
                        (this as InitInstruction).apply {
                            type.type shouldBe Type.CURRENCY
                            type.currencyId shouldBe "USD"
                            identifier shouldBe "salary_usd"
                            assignmentExpression should beInstanceOf<FactorWithCast>()
                            (assignmentExpression as FactorWithCast).apply {
                                this.factor should beInstanceOf<Factor>()
                                (this.factor as Factor).apply {
                                    functionCall shouldBe null
                                    expression shouldBe null
                                    identifier shouldBe "salary"
                                }

                                castTo.type shouldBe Type.CURRENCY
                                castTo.currencyId shouldBe "PLN"
                            }
                        }
                    }
                    this[4].apply {
                        this should beInstanceOf<IfStatement>()
                        (this as IfStatement).apply {
                            condition should beInstanceOf<EqualCondition>()
                            (condition as EqualCondition).apply {
                                leftCond should beInstanceOf<Factor>()
                                (leftCond as Factor).apply {
                                    identifier shouldBe "salary"
                                }
                                rightCond should beInstanceOf<Factor>()
                                (rightCond as Factor).apply {
                                    identifier shouldBe "salary_usd"
                                }
                            }
                        }
                    }
                }
            } shouldNotBe null
        }

        "parse simple program with while statement" {
            val inputString = "int main (int arg1) {\n" +
                "\t\tPLN salary = 1000.0;\n" +
                "\t\twhile (salary > 100.0) {\n" +
                "\t\t\tsalary = salary - 100.0;\n" +
                "\t\t}\n" +
                "\t}"

            val program = parseFromString(inputString)
            program.functions.size shouldBe 1
            program.functions["main"]?.apply {
                funReturnType.type shouldBe Type.INT
                funIdentifier shouldBe "main"

                parameters.size shouldBe 1

                functionBlock.instrAndStatementsList.apply {
                    size shouldBe 2
                    this[0].apply {
                        this should beInstanceOf<InitInstruction>()
                        (this as InitInstruction).apply {
                            type.type shouldBe Type.CURRENCY
                            type.currencyId shouldBe "PLN"
                            identifier shouldBe "salary"
                            assignmentExpression should beInstanceOf<Factor>()
                            (assignmentExpression as Factor).apply {
                                functionCall shouldBe null
                                expression shouldBe null
                                identifier shouldBe null
                                literal should beInstanceOf<Double>()
                                (literal as Double) shouldBe 1000.0
                            }
                        }
                    }
                    this[1].apply {
                        this should beInstanceOf<WhileStatement>()
                        (this as WhileStatement).apply {
                            condition should beInstanceOf<GreaterCondition>()
                            (condition as GreaterCondition).apply {
                                leftCond should beInstanceOf<Factor>()
                                (leftCond as Factor).apply {
                                    identifier shouldBe "salary"
                                }
                                rightCond should beInstanceOf<Factor>()
                                (rightCond as Factor).apply {
                                    (literal as Double) shouldBe 100.0
                                }
                            }
                            block.apply {
                                instrAndStatementsList.size shouldBe 1
                                instrAndStatementsList[0].apply {
                                    this should beInstanceOf<AssignInstruction>()
                                }
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

    val currencyIdSet = setOf("PLN", "USD", "EUR")
    val lexer = Lexer(source, currencyIdSet)
    val parser = Parser(lexer)

    return parser.parse()
}
