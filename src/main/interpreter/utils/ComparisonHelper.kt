package interpreter.utils

import interpreter.exception.CombinationOfUnsupportedValueTypesException
import interpreter.exception.DifferentTypesComparisonException
import interpreter.model.Currency

class ComparisonHelper {
    companion object {
        fun equal(value1: Any, value2: Any): Boolean = compareTo(value1, value2) == 0
        fun notEqual(value1: Any, value2: Any): Boolean = compareTo(value1, value2) != 0
        fun greater(value1: Any, value2: Any): Boolean = compareTo(value1, value2) > 0
        fun greaterOrEqual(value1: Any, value2: Any): Boolean = compareTo(value1, value2) >= 0
        fun less(value1: Any, value2: Any): Boolean = compareTo(value1, value2) < 0
        fun lessOrEqual(value1: Any, value2: Any): Boolean = compareTo(value1, value2) <= 0

        private fun compareTo(value1: Any, value2: Any): Int {
            validateSameTypes(value1, value2)
            return when (value1) {
                is Int -> value1.compareTo(value2 as Int)
                is Double -> value1.compareTo(value2 as Double)
                is String -> value1.compareTo(value2 as String)
                is Currency -> value1.amount.compareTo((value2 as Currency).amount)
                is Boolean -> value1.compareTo(value2 as Boolean)
                else -> throw CombinationOfUnsupportedValueTypesException(
                    value1::class.java.name, value2::class.java.name, Companion::compareTo.name
                )
            }
        }

        private fun validateSameTypes(value1: Any, value2: Any) {
            if (value1::class.java != value2::class.java ||
                (value1 is Currency && value2 is Currency && value1.currencyId != value2.currencyId)
            )
                throw DifferentTypesComparisonException(value1, value2)
        }
    }
}
