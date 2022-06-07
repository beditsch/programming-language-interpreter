package configparser

import configparser.exception.DuplicatedCurrencyIdException
import configparser.exception.MissingCurrencyIdsException
import configparser.exception.WrongNumberOfExchangeRatesException
import configparser.model.Config
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.WordSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import lexer.Lexer
import lexer.source.StringSource
import parser.exception.UnexpectedTokenException

class ConfigParserTest : WordSpec({
    ConfigParser::class.java.simpleName should {
        "parse config file" {
            val currencies = "PLN EUR DOGECOIN;"
            val plnRates = "PLN 1.0 0.23 0.005"
            val eurRates = "EUR 4.64 1.0 0.32"
            val dogecoinRates = "DOGECOIN 200.0 3.0 1.0"
            val input = "$currencies $plnRates $eurRates $dogecoinRates"
            val config = parseFromString(input)
            config.apply {
                currencyIds shouldContainExactly listOf("PLN", "EUR", "DOGECOIN")
                exchangeRatesMap.apply {
                    this["PLN"]?.get("PLN") shouldBe 1.0
                    this["PLN"]?.get("EUR") shouldBe 0.23
                    this["PLN"]?.get("DOGECOIN") shouldBe 0.005
                    this["EUR"]?.get("PLN") shouldBe 4.64
                    this["EUR"]?.get("EUR") shouldBe 1.0
                    this["EUR"]?.get("DOGECOIN") shouldBe 0.32
                    this["DOGECOIN"]?.get("PLN") shouldBe 200.0
                    this["DOGECOIN"]?.get("EUR") shouldBe 3.0
                    this["DOGECOIN"]?.get("DOGECOIN") shouldBe 1.0
                }
            }
        }

        "parse config file with no currencies" {
            val input = ";"
            val config = parseFromString(input)
            config.apply {
                currencyIds shouldBe emptyList()
                exchangeRatesMap.keys.size shouldBe 0
            }
        }

        "throw ${MissingCurrencyIdsException::class.java.simpleName} when currency ids block is missing" {
            val input = "10.0 20.0"
            shouldThrow<MissingCurrencyIdsException> { parseFromString(input) }
        }

        "throw ${DuplicatedCurrencyIdException::class.java.simpleName} when rates are defined twice for a given currency id" {
            val input = "PLN EUR; PLN 1.0 0.23 PLN 2.0 0.22"
            shouldThrow<DuplicatedCurrencyIdException> { parseFromString(input) }.apply {
                message shouldContain "PLN"
            }
        }

        "throw ${UnexpectedTokenException::class.java.simpleName} when config file doesn't end after rates definition" {
            val input = "PLN EUR; PLN 1.0 0.23 EUR 4.6 1.0 float"
            shouldThrow<UnexpectedTokenException> { parseFromString(input) }.apply {
                message shouldContain "ETX"
                message shouldContain "FLOAT"
            }
        }

        "throw ${WrongNumberOfExchangeRatesException::class.java.simpleName} when number of rates defined doesn't match number of currencies listed" {
            val input = "PLN EUR; PLN 1.0 0.23 3.0 EUR 4.6 1.0"
            shouldThrow<WrongNumberOfExchangeRatesException> { parseFromString(input) }.apply {
                message shouldContain "PLN"
                message shouldContain "3"
                message shouldContain "2"
            }
        }

        "throw ${DuplicatedCurrencyIdException::class.java.simpleName} when a currency id is listed twice" {
            val input = "PLN PLN EUR; PLN 1.0 0.23 EUR 4.6 1.0"
            shouldThrow<DuplicatedCurrencyIdException> { parseFromString(input) }.apply {
                message shouldContain "PLN"
            }
        }
    }
})

private fun parseFromString(inputString: String): Config {
    val source = StringSource(inputString)

    val lexer = Lexer(source)
    val parser = ConfigParser(lexer)

    return parser.parseConfig()
}
