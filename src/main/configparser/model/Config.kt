package configparser.model

class Config(
    val currencyIds: Set<String>,
    val exchangeRatesMap: Map<String, Map<String, Double>>
)
