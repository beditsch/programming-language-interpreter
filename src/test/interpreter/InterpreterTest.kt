package interpreter

import configparser.ConfigParser
import configparser.model.Config
import interpreter.exception.MissingCurrencyExchangeRateException
import interpreter.exception.MissingFunctionDeclarationException
import interpreter.exception.VariableNotFoundException
import interpreter.model.Currency
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.WordSpec
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.types.beInstanceOf
import lexer.Lexer
import lexer.source.StringSource
import parser.Parser
import parser.model.Program

class InterpreterTest : WordSpec({
    Interpreter::class.java.simpleName should {
        "interpret program" {
            val sourceCode = "string main () {\n" +
                "\t\tPLN initial_investment = 1200.0 as PLN;\n" +
                "\t\tfloat dividend_yield = 0.012;\n" +
                "\t\tint time_periods = 3;\n" +
                "\t\tfloat growth_rate = 0.01;\n" +
                "\t\n" +
                "\t\tfloat roi = calculate_return(initial_investment, time_periods, dividend_yield, growth_rate);\n" +
                "\t\tif (roi > 0.05) {\n" +
                "\t\t\treturn \"inwestujemy\";\n" +
                "\t\t}\n" +
                "\t\telse {\n" +
                "\t\t\treturn \"szukamy nowych inwestycji\";\n" +
                "\t\t}\n" +
                "\t\n" +
                "\t}\n" +
                "\t\n" +
                "\tfloat calculate_return (initial_investment, time_periods, dividend_yield, growth_rate) {\n" +
                "\t\tint i = 0;\n" +
                "\t\n" +
                "\t\tUSD current_investment = initial_investment as USD;\n" +
                "\t\twhile (i < time_periods) {\n" +
                "\t\n" +
                "\t\t\tUSD dividend_amount = dividend_yield * current_investment;\n" +
                "\t\t\tcurrent_investment = current_investment + dividend_amount;\n" +
                "\t\n" +
                "\t\t\tUSD growth_amount = growth_rate * current_investment;\n" +
                "\t\t\tcurrent_investment = current_investment + growth_amount;\n" +
                "\t\t\ti = i + 1;\n" +
                "\t\t}\n" +
                "\t\n" +
                "\t\tfloat perc_return = (current_investment - (initial_investment as USD)) / (initial_investment as USD);\n" +
                "\t\treturn perc_return;\n" +
                "\t}"
            getResult(sourceCode) shouldBe "inwestujemy"
        }

        "interpret negated factor" {
            val sourceCode = "PLN main() {" +
                "PLN money = 1200.0 as PLN;" +
                "money = -money;" +
                "return money;" +
                "}"
            getResult(sourceCode)?.apply {
                this should beInstanceOf<Currency>()
                (this as Currency).apply {
                    amount.toDouble() shouldBe -1200.0
                    currencyId shouldBe "PLN"
                }
            } shouldNotBe null
        }

        "interpret true OR condition" {
            val sourceCode = "bool main() {" +
                "if (2 < 1 || 2 == 2) { return true; }" +
                "else { return false; }" +
                "}"
            getResult(sourceCode) shouldBe true
        }

        "interpret untrue OR condition" {
            val sourceCode = "bool main() {" +
                "if (2 < 1 || 2 != 2) { return true; }" +
                "else { return false; }" +
                "}"
            getResult(sourceCode) shouldBe false
        }

        "interpret true AND condition" {
            val sourceCode = "bool main() {" +
                "if (2 > 1 && 2 == 2) { return true; }" +
                "else { return false; }" +
                "}"
            getResult(sourceCode) shouldBe true
        }

        "interpret untrue AND condition" {
            val sourceCode = "bool main() {" +
                "if (2 > 1 && 2 != 2) { return true; }" +
                "else { return false; }" +
                "}"
            getResult(sourceCode) shouldBe false
        }

        "interpret NOT condition" {
            val sourceCode = "bool main() {" +
                "if (!(2 != 3)) { return true; }" +
                "else { return false; }" +
                "}"
            getResult(sourceCode) shouldBe false
        }

        "throw ${MissingFunctionDeclarationException::class.simpleName} when no main function is defined" {
            val sourceCode = "bool myFunc() { return true; }"
            shouldThrow<MissingFunctionDeclarationException> { getResult(sourceCode) }.apply {
                message shouldContain "main"
            }
        }

        "throw ${MissingFunctionDeclarationException::class.simpleName} when undefined function is called" {
            val sourceCode = "bool main() { return myFunc(); }"
            shouldThrow<MissingFunctionDeclarationException> { getResult(sourceCode) }.apply {
                message shouldContain "myFunc"
            }
        }

        "throw ${VariableNotFoundException::class.simpleName} when assigment instruction uses unknown variable identifier" {
            val sourceCode = "bool main() { myVal = 3; }"
            shouldThrow<VariableNotFoundException> { getResult(sourceCode) }.apply {
                message shouldContain "myVal"
            }
        }

        "throw ${MissingCurrencyExchangeRateException::class.simpleName} when casting a currency with no exchange rates defined" {
            val config = "PLN EUR USD;" +
                "PLN 1.0 0.23 0.35" +
                "EUR 4.55 1.0 1.21"
            val sourceCode = "bool main() { USD myValUSD = 3 as USD; PLN myValPLN = myValUSD as PLN; return true; }"
            shouldThrow<MissingCurrencyExchangeRateException> { getResult(sourceCode, config) }.apply {
                message shouldContain "USD"
                message shouldContain "PLN"
            }
        }
    }
}) {
    companion object {
        private val config = "PLN EUR USD;" +
            "PLN 1.0 0.23 0.35" +
            "EUR 4.55 1.0 1.21" +
            "USD 4.2 0.92 1.0"

        private fun getResult(sourceCode: String, configCode: String = config): Any? {
            val config = getConfig(configCode)
            val program = getProgram(sourceCode)
            return Interpreter(program, config.exchangeRatesMap).interpret()
        }

        private fun getConfig(configString: String = config): Config {
            val configSource = StringSource(configString)
            val configParser = ConfigParser(Lexer(configSource))
            return configParser.parseConfig()
        }

        private fun getProgram(programSourceString: String, config: Config = getConfig()): Program {
            val programSource = StringSource(programSourceString)

            val sourceParser = Parser(Lexer(programSource, config.currencyIds))
            return sourceParser.parse()
        }
    }
}
