package configparser.model

class Config(
    val currencyIds: List<String>,
    val exchangeRatesMap: Map<String, Map<String, Double>>
)
