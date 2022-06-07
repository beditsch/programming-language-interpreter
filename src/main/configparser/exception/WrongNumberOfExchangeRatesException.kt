package configparser.exception

class WrongNumberOfExchangeRatesException(provided: Int, expected: Int, currencyId: String) :
    Exception("Wrong number of exchange rates provided for currency $currencyId. Expected: $expected, Provided: $provided.")
