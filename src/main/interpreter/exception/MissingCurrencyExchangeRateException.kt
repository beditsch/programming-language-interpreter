package interpreter.exception

class MissingCurrencyExchangeRateException(from: String, to: String) :
    Exception("Cannot cast from $from to $to. Missing currency exchange rate.")
