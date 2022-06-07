package interpreter.utils

import interpreter.exception.CombinationOfUnsupportedValueTypesException
import interpreter.exception.DifferentTypesComparisonException
import interpreter.exception.DivisionByZeroException
import interpreter.model.Currency
import java.math.BigDecimal

class ArithmeticsHelper {
    companion object {
        fun add(value1: Any, value2: Any): Any {
            validateAdditionExpressionTypes(value1, value2)
            return when (value1) {
                is Int -> value1 + (value2 as Int)
                is Double -> value1 + (value2 as Double)
                is String -> value1 + (value2 as String)
                is Currency -> Currency(value1.amount + (value2 as Currency).amount, value1.currencyId)
                else -> throw CombinationOfUnsupportedValueTypesException(
                    value1::class.java.name, value2::class.java.name, Companion::add.name
                )
            }
        }

        fun subtract(value1: Any, value2: Any): Any {
            validateSubtractionExpressionTypes(value1, value2)
            return when (value1) {
                is Int -> value1 - (value2 as Int)
                is Double -> value1 - (value2 as Double)
                is Currency -> Currency(value1.amount - (value2 as Currency).amount, value1.currencyId)
                else -> throw CombinationOfUnsupportedValueTypesException(
                    value1::class.java.name, value2::class.java.name, Companion::subtract.name
                )
            }
        }

        fun multiply(value1: Any, value2: Any): Any {
            validateMultiplicationExpressionTypes(value1, value2)
            if (value1 is Currency || value2 is Currency) return multiplyCurrency(value1, value2)

            return when (value1) {
                is Int -> when (value2) {
                    is Int -> value1 * value2
                    is Double -> value1 * value2
                    else -> throw CombinationOfUnsupportedValueTypesException(
                        value1::class.java.name, value2::class.java.name, Companion::multiply.name
                    )
                }
                is Double -> when (value2) {
                    is Int -> value1 * value2
                    is Double -> value1 * value2
                    else -> throw CombinationOfUnsupportedValueTypesException(
                        value1::class.java.name, value2::class.java.name, Companion::multiply.name
                    )
                }
                else -> throw CombinationOfUnsupportedValueTypesException(
                    value1::class.java.name, value2::class.java.name, Companion::multiply.name
                )
            }
        }

        private fun multiplyCurrency(value1: Any, value2: Any): Currency {
            if (value1 !is Currency && value2 !is Currency)
                throw CombinationOfUnsupportedValueTypesException(
                    value1::class.java.name, value2::class.java.name, Companion::multiplyCurrency.name
                )

            val currency = value1 as? Currency ?: value2 as Currency
            val otherValInt = value1 as? Int ?: value2 as? Int
            val otherValDouble = value1 as? Double ?: value2 as? Double

            val resultAmount = when {
                otherValInt != null -> currency.amount * BigDecimal(otherValInt)
                otherValDouble != null -> currency.amount * BigDecimal(otherValDouble)
                else -> throw CombinationOfUnsupportedValueTypesException(
                    value1::class.java.name, value2::class.java.name, Companion::multiplyCurrency.name
                )
            }
            return Currency(resultAmount, currency.currencyId)
        }

        fun divide(value1: Any, value2: Any): Any {
            validateDivisionExpressionTypes(value1, value2)
            return when (value1) {
                is Int -> when (value2) {
                    is Int -> value1 / value2
                    is Double -> value1 / value2
                    is Currency -> Currency(value2.amount * BigDecimal(value1), value2.currencyId)
                    else -> throw CombinationOfUnsupportedValueTypesException(
                        value1::class.java.name, value2::class.java.name, Companion::divide.name
                    )
                }
                is Double -> when (value2) {
                    is Int -> value1 / value2
                    is Double -> value1 / value2
                    is Currency -> Currency(value2.amount / BigDecimal(value1), value2.currencyId)
                    else -> throw CombinationOfUnsupportedValueTypesException(
                        value1::class.java.name, value2::class.java.name, Companion::divide.name
                    )
                }
                is Currency -> when (value2) {
                    is Int -> Currency(value1.amount / BigDecimal(value2), value1.currencyId)
                    is Double -> Currency(value1.amount / BigDecimal(value2), value1.currencyId)
                    is Currency -> Currency(value1.amount / value2.amount, value1.currencyId)
                    else -> throw CombinationOfUnsupportedValueTypesException(
                        value1::class.java.name, value2::class.java.name, Companion::divide.name
                    )
                }
                else -> throw CombinationOfUnsupportedValueTypesException(
                    value1::class.java.name, value2::class.java.name, Companion::divide.name
                )
            }
        }

        private fun validateAdditionExpressionTypes(value1: Any, value2: Any) {
            if (value1::class.java != value2::class.java) throw DifferentTypesComparisonException(value1, value2)
            if (value1 is Currency && value2 is Currency && (value1.currencyId != value2.currencyId))
                throw DifferentTypesComparisonException(value1, value2)
            if (!listOf(Int::class.java, Double::class.java, String::class.java, Currency::class.java)
                .containsAll(listOf(value1::class.java, value2::class.java))
            )
                throw CombinationOfUnsupportedValueTypesException(
                    value1::class.java.name, value2::class.java.name, Companion::validateAdditionExpressionTypes.name
                )
        }

        private fun validateSubtractionExpressionTypes(value1: Any, value2: Any) {
            if (value1::class.java != value2::class.java) throw DifferentTypesComparisonException(value1, value2)
            if (value1 is Currency && value2 is Currency && (value1.currencyId != value2.currencyId))
                throw DifferentTypesComparisonException(value1, value2)
            if (!listOf(Int::class.java, Double::class.java, Currency::class.java)
                .containsAll(listOf(value1::class.java, value2::class.java))
            )
                throw CombinationOfUnsupportedValueTypesException(
                    value1::class.java.name, value2::class.java.name, Companion::validateSubtractionExpressionTypes.name
                )
        }

        private fun validateMultiplicationExpressionTypes(value1: Any, value2: Any) {
            val class1 = value1::class.java
            val class2 = value2::class.java
            if (!listOf(Int::class.java, Double::class.java, Currency::class.java).containsAll(listOf(class1, class2)))
                throw CombinationOfUnsupportedValueTypesException(
                    class1.name, class2.name, Companion::validateMultiplicationExpressionTypes.name
                )
            if (class1 == Currency::class.java && class2 == Currency::class.java)
                throw CombinationOfUnsupportedValueTypesException(
                    class1.name, class2.name, Companion::validateMultiplicationExpressionTypes.name
                )
        }

        private fun validateDivisionExpressionTypes(value1: Any, value2: Any) {
            val class1 = value1::class.java
            val class2 = value2::class.java
            if (!listOf(Int::class.java, Double::class.java, Currency::class.java).containsAll(listOf(class1, class2)))
                throw CombinationOfUnsupportedValueTypesException(
                    class1.name, class2.name, Companion::validateDivisionExpressionTypes.name
                )
            if (value1 is Currency && value2 is Currency && value1.currencyId != value2.currencyId)
                throw DifferentTypesComparisonException(value1, value2)
            if (value2 is Int && value2 == 0 ||
                value2 is Double && value2 == 0.0 ||
                value2 is Currency && value2 == BigDecimal.ZERO
            )
                throw DivisionByZeroException(value2)
        }
    }
}
