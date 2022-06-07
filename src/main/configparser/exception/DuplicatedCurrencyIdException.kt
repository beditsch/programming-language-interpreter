package configparser.exception

class DuplicatedCurrencyIdException(currencyId: String) : Exception("Duplicated currency entry for: $currencyId.")
