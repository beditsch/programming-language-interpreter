package interpreter.model

import java.math.BigDecimal

class Currency(
    val amount: BigDecimal,
    val currencyId: String
) {
    override fun toString(): String {
        return "(Currency: $currencyId; Amount: ${amount.toDouble()})"
    }
}
