package configparser

import configparser.exception.DuplicatedCurrencyIdException
import configparser.exception.MissingCurrencyIdsException
import configparser.exception.UnexpectedNullTokenException
import configparser.exception.WrongNumberOfExchangeRatesException
import configparser.model.Config
import lexer.Lexer
import parser.exception.UnexpectedTokenException
import shared.Token
import shared.TokenType

class ConfigParser(
    private val lexer: Lexer
) {
    init {
        lexer.getNextToken()
    }

    fun parseConfig(): Config {
        val currencyIds = tryParseCurrencyIds() ?: throw MissingCurrencyIdsException()
        val exchangeRatesMap = mutableMapOf<String, Map<String, Double>>()

        var currencyAndExchangeRatesPair = tryParseCurrencyExchangeRates()
        while (currencyAndExchangeRatesPair != null) {
            val currencyId = currencyAndExchangeRatesPair.first
            val exchangeRatesList = currencyAndExchangeRatesPair.second

            if (exchangeRatesList.size != currencyIds.size)
                throw WrongNumberOfExchangeRatesException(exchangeRatesList.size, currencyIds.size, currencyId)
            if (exchangeRatesMap.containsKey(currencyId)) throw DuplicatedCurrencyIdException(currencyId)

            val currenciesToRatesMap = currencyIds.mapIndexed { index, currencyId ->
                currencyId to exchangeRatesList[index]
            }.toMap()
            exchangeRatesMap[currencyId] = currenciesToRatesMap
            currencyAndExchangeRatesPair = tryParseCurrencyExchangeRates()
        }

        if (!lexer.currentTokenIs(TokenType.ETX))
            throw UnexpectedTokenException(ConfigParser::parseConfig.name, listOf(TokenType.ETX), lexer.getToken())
        return Config(currencyIds, exchangeRatesMap)
    }

    private fun tryParseCurrencyIds(): List<String>? {
        if (!lexer.currentTokenIs(listOf(TokenType.IDENTIFIER, TokenType.SEMICOLON)))
            return null

        val currencyIds = mutableListOf<String>()
        while (!lexer.currentTokenIs(TokenType.SEMICOLON)) {
            val token = lexer.getToken() ?: throw UnexpectedNullTokenException()
            if (!currencyIds.contains(token.value.toString())) {
                currencyIds.add(token.value.toString())
                lexer.getNextToken()
            } else throw DuplicatedCurrencyIdException(token.value.toString())
        }
        lexer.getNextToken()
        return currencyIds
    }

    private fun tryParseCurrencyExchangeRates(): Pair<String, List<Double>>? {
        if (!lexer.currentTokenIs(TokenType.IDENTIFIER))
            return null

        val currencyId = lexer.getTokenAndMoveToNext().value.toString()
        val currencyExchangeRates = mutableListOf<Double>()
        while (lexer.currentTokenIs(TokenType.FLOAT_VAL)) {
            currencyExchangeRates.add(lexer.getTokenAndMoveToNext().value as Double)
        }
        return Pair(currencyId, currencyExchangeRates)
    }
}

private fun Lexer.currentTokenIs(tokenType: TokenType): Boolean =
    this.getToken()?.tokenType == tokenType

private fun Lexer.currentTokenIs(tokenTypes: List<TokenType>): Boolean =
    tokenTypes.contains(this.getToken()?.tokenType)

private fun Lexer.getTokenAndMoveToNext(): Token<*> {
    val currentToken = this.getToken() ?: throw UnexpectedNullTokenException()
    this.getNextToken()
    return currentToken
}
